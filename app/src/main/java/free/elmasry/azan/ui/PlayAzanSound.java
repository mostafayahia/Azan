/*
 * Copyright (C) 2018 Yahia H. El-Tayeb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package free.elmasry.azan.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;

import free.elmasry.azan.R;
import free.elmasry.azan.alarm.ScheduleAlarmTask;
import free.elmasry.azan.shared.AzanTimeIndex;
import free.elmasry.azan.utilities.AzanAppHelperUtils;
import free.elmasry.azan.utilities.AzanAppTimeUtils;
import free.elmasry.azan.utilities.FetchDataUtils;
import free.elmasry.azan.utilities.HelperUtils;
import free.elmasry.azan.utilities.PreferenceUtils;
import free.elmasry.azan.widget.AzanWidgetService;

import static free.elmasry.azan.utilities.LocationUtils.MyLocation;

public class PlayAzanSound extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private MediaPlayer mMediaPlayer;

    private static final String AUDIO_POSITION_KEY = "audio-position-key";

    private static final String LOG_TAG = PlayAzanSound.class.getSimpleName();

    /**
     * the number of extra days we fetch from azan api and will add them in shared preferences
     */
    private static final int FETCH_EXTRA_STORE_DAYS = 3;

    private static final boolean DEBUG = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_azan_sound);

        int indexOfCurrentAzanTime = AzanAppHelperUtils.getIndexOfCurrentTime(this);
        String[] allAzanTimesIn24Format = PreferenceUtils.getAzanTimesIn24Format(this,
                AzanAppTimeUtils.convertMillisToDateString(System.currentTimeMillis()));

        if (!AzanAppHelperUtils.isValidPlayAzanTimeIndex(indexOfCurrentAzanTime)) {
            Log.e(LOG_TAG, "NOT valid play azan time index: " + indexOfCurrentAzanTime);
            throw new RuntimeException("NOT valid play azan time index: " + indexOfCurrentAzanTime);
        }

        TextView azanTimeLabelTextView = findViewById(R.id.azan_time_label_textview);
        azanTimeLabelTextView.setText(AzanAppHelperUtils.getAzanLabel(this, indexOfCurrentAzanTime));

        TextView azanTimeTextView = findViewById(R.id.azan_time_textview);
        String azanTimeIn24HourFormat = allAzanTimesIn24Format[indexOfCurrentAzanTime];

        if (getResources().getBoolean(R.bool.use_24_hour_format)) {
            azanTimeTextView.setText(AzanAppTimeUtils.getTimeWithDefaultLocale(azanTimeIn24HourFormat));
        } else {
            azanTimeTextView.setText(AzanAppTimeUtils.convertTo12HourFormat(azanTimeIn24HourFormat));
        }

        if (null == savedInstanceState) {
            ScheduleAlarmTask.scheduleTaskForNextAzanTime(this);
            AzanWidgetService.startActionDisplayAzanTime(this);
        }

        fetchExtraData();

        String azanAudioPreference = PreferenceUtils.getAzanAudioFromPreferences(this);
        if (azanAudioPreference.equals(getString(R.string.pref_audio_full_azan))) {
            if (indexOfCurrentAzanTime == AzanTimeIndex.INDEX_FAJR) {
                mMediaPlayer = MediaPlayer.create(this, R.raw.full_azan_fajr_abd_el_baset);
            } else {
                // we made the mp3 file sound louder using http://www.mp3louder.com/
                mMediaPlayer = MediaPlayer.create(this, R.raw.full_azan_abd_el_baset);
            }
        } else if (azanAudioPreference.equals(getString(R.string.pref_audio_takbeer))) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.takbeer);
        } else if (azanAudioPreference.equals(getString(R.string.pref_audio_none))) {
            final int durationInMillis = 5000;
            /*
             * in this case we will display this activity with no sound for durationInMillis
             * then we will close this activity
             */
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            };
            new Handler().postDelayed(runnable, durationInMillis);
        } else {
            throw new RuntimeException("unknown azan audio preference: " + azanAudioPreference);
        }

        if (mMediaPlayer != null) {
            // if the device is lollipop we have to setWakeMode to PARTIAL_WAKE_LOCK otherwise
            // the sound won't continue to the end (at least in case of playing full_azan_abd_el_baset)
            if (HelperUtils.isLollipop())
                mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);

            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setLooping(false);

            if (null != savedInstanceState && savedInstanceState.containsKey(AUDIO_POSITION_KEY)) {
                int position = savedInstanceState.getInt(AUDIO_POSITION_KEY);
                mMediaPlayer.seekTo(position);
            }

            mMediaPlayer.start();
        }
    }

    private void fetchExtraData() {
        final int MAX_FETCH_DAYS = 70;
        final int WAIT_DAYS_IF_REACH_MAX = 90;

        /*
         * if fetchExtraCounter > MAX_FETCH_DAYS we will wait WAIT_DAYS_IF_REACH_MAX days from last
         * fetch date not fetching any data else we will fetch extra data as normal
         * also maxNumber of fetching extra data is one time per day
         */

        if (HelperUtils.isDeviceOnline(this)) {

            /*
             * we use DEBUG to test the scenario of the behavior of reaching max numbers of fetching extra
             */
            if (DEBUG) {
                PreferenceUtils.setFetchExtraCounter(this, MAX_FETCH_DAYS + 1);
                PreferenceUtils.setFetchExtraLastDateTimeString(this, "10 Jun 2019 11:11:00");
            }

            long nowInMillis = System.currentTimeMillis();
            int fetchExtraCounter = PreferenceUtils.getFetchExtraCounter(this);
            String lastDateTimeString = PreferenceUtils.getFetchExtraLastDateTimeString(this);
            String lastDateString = AzanAppTimeUtils.getDateStringFromDateTimeString(lastDateTimeString);

            Log.d(LOG_TAG, "=========Just BEFORE fetching any extra data========");
            Log.d(LOG_TAG, "fetchExtraCounter: " + fetchExtraCounter);
            Log.d(LOG_TAG, "fetchExtraLastDateTimeString: " + lastDateTimeString);
            Log.d(LOG_TAG, "lastStoredDateString: " +
                    PreferenceUtils.getLastStoredDateStringForData(this));

            if (!DEBUG && lastDateString.equals(AzanAppTimeUtils.convertMillisToDateString(nowInMillis))) {
                return; // No point for continue (Max: one fetch extra per day)
            }

            if (fetchExtraCounter > MAX_FETCH_DAYS && !TextUtils.isEmpty(lastDateString)) {
                long lastDateInMillis = AzanAppTimeUtils.convertDateToMillis(lastDateString);
                long waitDaysInMillis = WAIT_DAYS_IF_REACH_MAX * AzanAppTimeUtils.DAY_IN_MILLIS;
                if (nowInMillis < lastDateInMillis + waitDaysInMillis) {
                    return; // No point for continue
                } else {
                    // reset fetch extra data counter
                    PreferenceUtils.setFetchExtraCounter(this, 0);
                }
            }

            MyLocation myLocation = PreferenceUtils.getUserLocation(this);

            if (myLocation != null && myLocation.isDataNotNull()) {
                new FetchExtraAzanTimes().execute(myLocation);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMediaPlayer != null)
            outState.putInt(AUDIO_POSITION_KEY, mMediaPlayer.getCurrentPosition());
    }

    @Override
    protected void onDestroy() {
        if (mMediaPlayer != null) mMediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        finish();
    }

    private class FetchExtraAzanTimes extends AsyncTask<MyLocation, Void, String[]> {

        private long startTimeInMillis;


        @Override
        protected String[] doInBackground(MyLocation... myLocations) {
            MyLocation myLocation = myLocations[0];

            Context context = PlayAzanSound.this;

            String lastStoreDateString = PreferenceUtils.getLastStoredDateStringForData(context);

            if (TextUtils.isEmpty(lastStoreDateString)) {
                return null; //Nothing to do!
            }

            startTimeInMillis = AzanAppTimeUtils.convertDateToMillis(
                    AzanAppTimeUtils.getDayAfterDateString(lastStoreDateString));

            try {
                return FetchDataUtils.getJsonResponseArray(context, myLocation,
                        FETCH_EXTRA_STORE_DAYS, startTimeInMillis);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "error in getting json response from the url");
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] jsonResponseArray) {
            if (jsonResponseArray == null) {
                Log.e(LOG_TAG, "jsonResponseArray for Azan Data is null");
                return;
            }

            Context context = PlayAzanSound.this;

            try {
                FetchDataUtils.saveAzanAppDataInPreferences(context, jsonResponseArray,
                        FETCH_EXTRA_STORE_DAYS, startTimeInMillis);

                // update meta-data regarding to last extra fetch
                long nowInMillis = System.currentTimeMillis();
                String nowDateTimeString = AzanAppTimeUtils.convertMillisToDateTimeString(nowInMillis);
                int extraFetchCounter = PreferenceUtils.getFetchExtraCounter(context);
                PreferenceUtils.setFetchExtraCounter(context, ++extraFetchCounter);
                PreferenceUtils.setFetchExtraLastDateTimeString(context, nowDateTimeString);

                Log.d(LOG_TAG, "===== fetch extra data DONE successfully :)) =====");
                Log.d(LOG_TAG, "fetchExtraCounter: " + PreferenceUtils.getFetchExtraCounter(context));
                Log.d(LOG_TAG, "fetchExtraLastDateTimeString: " + PreferenceUtils.getFetchExtraLastDateTimeString(context));
                Log.d(LOG_TAG, "lastStoredDateString: " +
                        PreferenceUtils.getLastStoredDateStringForData(context));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "can't get data from the json response");
                e.printStackTrace();
            }
        }
    }
}
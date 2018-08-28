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

package free.elmasry.azan;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import free.elmasry.azan.alarm.ScheduleAlarmTask;
import free.elmasry.azan.shared.AzanTimeIndex;
import free.elmasry.azan.utilities.AzanAppHelperUtils;
import free.elmasry.azan.utilities.AzanAppTimeUtils;
import free.elmasry.azan.utilities.HelperUtils;
import free.elmasry.azan.utilities.PreferenceUtils;
import free.elmasry.azan.widget.AzanWidgetService;

import static free.elmasry.azan.shared.AzanTimeIndex.INDEX_ASR;
import static free.elmasry.azan.shared.AzanTimeIndex.INDEX_DHUHR;
import static free.elmasry.azan.shared.AzanTimeIndex.INDEX_FAJR;
import static free.elmasry.azan.shared.AzanTimeIndex.INDEX_ISHAA;
import static free.elmasry.azan.shared.AzanTimeIndex.INDEX_MAGHRIB;

public class PlayAzanSound extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private MediaPlayer mMediaPlayer;

    private static final String AUDIO_POSITION_KEY = "audio-position-key";

    private static final String LOG_TAG = PlayAzanSound.class.getSimpleName();


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
            return; // No need for continue
        } else {
            throw new RuntimeException("unknown azan audio preference: " + azanAudioPreference);
        }

        // if the device is lollipop we have to setWakeMode to PARTIAL_WAKE_LOCK otherwise
        // the sound won't continue to the end (at least in case of playing full_azan_abd_el_baset)
        if (HelperUtils.isLollipop())
            mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);

        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setLooping(false);

        if (null != savedInstanceState && savedInstanceState.containsKey(AUDIO_POSITION_KEY)) {
            int position = savedInstanceState.getInt(AUDIO_POSITION_KEY);
            mMediaPlayer.seekTo(position);
        } else {
            ScheduleAlarmTask.scheduleTaskForNextAzanTime(this);
        }

        mMediaPlayer.start();

        AzanWidgetService.startActionDisplayAzanTime(this);
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


}

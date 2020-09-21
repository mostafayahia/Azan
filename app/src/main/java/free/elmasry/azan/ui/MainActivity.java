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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import free.elmasry.azan.R;
import free.elmasry.azan.alarm.ScheduleAlarmTask;
import free.elmasry.azan.alarm.ScheduledReceiver;
import free.elmasry.azan.notification.NotificationData;
import free.elmasry.azan.utilities.AzanAppHelperUtils;
import free.elmasry.azan.utilities.LocationUtils;
import free.elmasry.azan.utilities.AzanAppTimeUtils;
import free.elmasry.azan.utilities.AzanCalcMethodUtils;
import free.elmasry.azan.utilities.FetchDataUtils;
import free.elmasry.azan.utilities.HelperUtils;
import free.elmasry.azan.utilities.NotificationUtil;
import free.elmasry.azan.utilities.PreferenceUtils;
import free.elmasry.azan.widget.AzanWidgetService;

import static free.elmasry.azan.alarm.ScheduleAlarmTask.ACTION_PLAY_AZAN_SOUND;
import static free.elmasry.azan.utilities.LocationUtils.MyLocation;

import static free.elmasry.azan.shared.AzanTimeIndex.*;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener, LocationUtils.LocationSuccessHandler {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();


    private TextView mDateTextView;

    private TextView[] mAllAzanTimesTextViews;
    private View[] mAllAzanTimesLayouts;

    private static final int MY_PERMISSION_LOCATION_REQUEST_CODE = 305;


    private static final boolean DEBUG = false;

    private static final int STORING_TOTAL_DAYS_NUM = 21;
    private static final int MAX_DAYS_OFFSET_FOR_DISPLAY = 6;

    private String mCurrentDateDisplayed;

    private String mTodayDateString;

    private static boolean sCalcMethodPreferenceUpdated = false;
    private static boolean sCalcMethodSetDefaultState = false;

    private static final String CURRENT_DATE_DISPLAYED_KEY = "current-date-displayed-key";

    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 753;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for testing, we can set locale we want to use in this app
//        HelperUtils.setLocaleForApp(this, "ar");

        setContentView(R.layout.activity_main);

        // initialize mAllAzanTimesTextViews
        mAllAzanTimesTextViews = new TextView[ALL_TIMES_NUM];
        mAllAzanTimesTextViews[INDEX_FAJR] = findViewById(R.id.time_fajr_textview);
        mAllAzanTimesTextViews[INDEX_SHUROOQ] = findViewById(R.id.time_shurooq_textview);
        mAllAzanTimesTextViews[INDEX_DHUHR] = findViewById(R.id.time_dhuhr_textview);
        mAllAzanTimesTextViews[INDEX_ASR] = findViewById(R.id.time_asr_textview);
        mAllAzanTimesTextViews[INDEX_MAGHRIB] = findViewById(R.id.time_maghrib_textview);
        mAllAzanTimesTextViews[INDEX_ISHAA] = findViewById(R.id.time_ishaa_textview);

        // initialize mAllAzanTimesLayouts
        mAllAzanTimesLayouts = new View[ALL_TIMES_NUM];
        mAllAzanTimesLayouts[INDEX_FAJR] = findViewById(R.id.time_fajr_layout);
        mAllAzanTimesLayouts[INDEX_SHUROOQ] = findViewById(R.id.time_shurooq_layout);
        mAllAzanTimesLayouts[INDEX_DHUHR] = findViewById(R.id.time_dhuhr_layout);
        mAllAzanTimesLayouts[INDEX_ASR] = findViewById(R.id.time_asr_layout);
        mAllAzanTimesLayouts[INDEX_MAGHRIB] = findViewById(R.id.time_maghrib_layout);
        mAllAzanTimesLayouts[INDEX_ISHAA] = findViewById(R.id.time_ishaa_layout);



        mDateTextView = findViewById(R.id.date_textview);

        if (MAX_DAYS_OFFSET_FOR_DISPLAY >= STORING_TOTAL_DAYS_NUM)
            throw new RuntimeException("max days offset which the app can display can't be >= the total number of days stored by this app");



        // setting mTodayDateString
        mTodayDateString = AzanAppTimeUtils.convertMillisToDateString(System.currentTimeMillis());

        if (null != savedInstanceState) {
            String currentDateString = savedInstanceState.getString(CURRENT_DATE_DISPLAYED_KEY, null);
            if (currentDateString != null) {
                setDateAndAzanTimesViews(currentDateString);
            } else {
                // Note: currentDateString will be null if you rotate the device while fetching the data from the internet isn't finished
                init();
            }
        } else {
            // for testing we can clear all data stored in the preferences
//            PreferenceUtils.clearAllAzanTimesStoredInPreferences(this);

            init();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                requestPermission();
            }
        }

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }






    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.azan_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_update_location:
                if (HelperUtils.isDeviceOnline(this)) {
                    fetchData(true);
                } else {
                    showErrorNoConnectionLayout();
                }
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_DATE_DISPLAYED_KEY, mCurrentDateDisplayed);
    }

    @Override
    protected void onStart() {

        super.onStart();

        // if the application was onStop in 11:59 PM then become onStart in 12:00 AM
        mTodayDateString = AzanAppTimeUtils.convertMillisToDateString(System.currentTimeMillis());
        if (mCurrentDateDisplayed != null) {
            long todayDateInMillis = AzanAppTimeUtils.convertDateToMillis(mTodayDateString);
            long currentDateDisplayedInMillis = AzanAppTimeUtils.convertDateToMillis(mCurrentDateDisplayed);
            if (currentDateDisplayedInMillis < todayDateInMillis) init();
        }

        // if the application was onStop in Dhuhr time then become onStart in Asr time
        if (mCurrentDateDisplayed != null) {
            unhighlightAllTimesViews();
            highlightNextTimeView(mCurrentDateDisplayed);
        }

        if (sCalcMethodPreferenceUpdated) {
            sCalcMethodPreferenceUpdated = false;
            if (HelperUtils.isDeviceOnline(this)) {
                PreferenceUtils.clearAllAzanTimesStoredInPreferences(this);
                fetchData(true);
            } else {
                showErrorNoConnectionLayout();
            }
        }

    }


    private void init() {

        // getting max date string the app can display
        String dateString = mTodayDateString;
        for (int i = 0; i < MAX_DAYS_OFFSET_FOR_DISPLAY; i++)
            dateString = AzanAppTimeUtils.getDayAfterDateString(dateString);
        String maxDateStringForDisplay = dateString;

        // we check against maxDateStringForDisplay, to make sure the app doesn't crash when the user
        // increase the date by clicking increase day button
        String[] allAzanTimesIn24Format =
                PreferenceUtils.getAzanTimesIn24Format(this, maxDateStringForDisplay);
        if (allAzanTimesIn24Format == null) {
            if (HelperUtils.isDeviceOnline(this)) {
                fetchData(false);
            }
            else showErrorNoConnectionLayout();
        } else {
            setDateAndAzanTimesViews(mTodayDateString);
        }

        // for testing alarm at certain time
//        ScheduleAlarmTask.scheduleAlarmForStartingAzanSoundActivityAt(this, "26 Mar 2020", 22, 23);

        if (mCurrentDateDisplayed != null) {
            ScheduleAlarmTask.scheduleTaskForNextAzanTime(this);
            AzanWidgetService.startActionDisplayAzanTime(this);
        }
    }

    private void requestPermission() {
        // Check if Android M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + this.getPackageName()));
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, R.string.error_no_permission_granted_message, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    private void fetchData(boolean reloadLocation) {


//        if (DEBUG) {
//            double longitude = 29.9187387;
//            double latitude = 31.2000924;
//            MyLocation myLocation = new MyLocation();
//            myLocation.setLongitude(longitude);
//            myLocation.setLatitude(latitude);
//            new FetchAzanTimes().execute(myLocation);
//            return;
//        }

        MyLocation myLocation = PreferenceUtils.getUserLocation(this);

        if (reloadLocation || myLocation == null || !myLocation.isDataNotNull()) {
            if (!LocationUtils.locationPermissionGranted(this, MY_PERMISSION_LOCATION_REQUEST_CODE)) {
                return; // No point for continue
            }

            // get the location of the user then fetching the data from the internet
            LocationUtils.processBasedOnLocation(this, this, MY_PERMISSION_LOCATION_REQUEST_CODE);
        } else {
            onLocationSuccess(myLocation);
        }

    }

    @Override
    public void onLocationSuccess(MyLocation myLocation) {
        if (myLocation != null && myLocation.isDataNotNull()) {
            PreferenceUtils.clearAllAzanTimesStoredInPreferences(MainActivity.this);
            PreferenceUtils.setUserLocation(this, myLocation);
            new FetchAzanTimes().execute(myLocation);
        } else {
            showLocationErrDialogue(getString(R.string.location_problem_title), getString(R.string.location_problem_message));
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_LOCATION_REQUEST_CODE:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    HelperUtils.showToast(this, R.string.error_no_permission_granted_message, Toast.LENGTH_LONG);
                    finish();
                } else {
                    fetchData(true);
                }

        }
    }

    private void showLocationErrDialogue(String title, String message) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (LocationUtils.isLocationEnabled(MainActivity.this)) {
                            // we will try to open google map app hopefully this makes the android
                            // stores and caches the device location
                            HelperUtils.openApp(MainActivity.this, "com.google.android.apps.maps");
                        } else {
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                        }
                        finish();
                    }
                })
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }


    public void errorRetryButtonHandler(View view) {
        if (HelperUtils.isDeviceOnline(this)) {
            // you have to hide error_no_connection_layout before fetching the data from the internet
            // otherwise you will get a wrong display
            hideErrorNoConnectionLayout();
            fetchData(false);
        }
    }

    public void changeDayButtonHandler(View view) {

        int id = view.getId();

        switch (id) {
            case R.id.increase_day_button: {
                long maxDaysOffsetInMillis = MAX_DAYS_OFFSET_FOR_DISPLAY * AzanAppTimeUtils.DAY_IN_MILLIS;
                String maxDateStringForDisplay =
                        AzanAppTimeUtils.convertMillisToDateString(maxDaysOffsetInMillis + System.currentTimeMillis());
                if (mCurrentDateDisplayed.equals(maxDateStringForDisplay)) {
                    HelperUtils.showToast(this, R.string.reach_to_max_message);
                    break;
                }
                unhighlightAllTimesViews();
                String dateString = AzanAppTimeUtils.getDayAfterDateString(mCurrentDateDisplayed);
                setDateAndAzanTimesViews(dateString);
                break;
            }
            case R.id.decrease_day_button: {
                if (mCurrentDateDisplayed.equals(mTodayDateString)) {
                    HelperUtils.showToast(this, R.string.reach_to_min_message);
                    break;
                }
                String dateString = AzanAppTimeUtils.getDayBeforeDateString(mCurrentDateDisplayed);
                setDateAndAzanTimesViews(dateString);
                break;
            }
            default:
                throw new RuntimeException("undefined button id: " + id);
        }
    }

    public void testPlayAzanActivityButtonHandler(View view) {
        DebuggingButtonsHandlers.launchAzanSoundActivity(this);
    }

    public void testPlayEqamahActivityButtonHandler(View view) {
        DebuggingButtonsHandlers.launchEqamahSoundActivity(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String preferenceKey) {
        if (preferenceKey.equals(getString(R.string.pref_calc_method_key))) {

            if (sCalcMethodSetDefaultState) sCalcMethodSetDefaultState = false;
            else sCalcMethodPreferenceUpdated = true;

        } else if (preferenceKey.equals(getString(R.string.pref_time_format_key))) {
            setDateAndAzanTimesViews(mTodayDateString);
            AzanWidgetService.startActionDisplayAzanTime(this);
        }
    }

    public void testLaunchNotificationButtonHandler(View view) {
        DebuggingButtonsHandlers.launchNotification(this, LOG_TAG);
    }

    public void testScheduledReceiverButtonHandler(View view) {
        DebuggingButtonsHandlers.scheduledReceiver(this);
    }


    private class FetchAzanTimes extends AsyncTask<MyLocation, Void, String[]> {

        private long startTimeInMillis;

        @Override
        protected String[] doInBackground(MyLocation... params) {
            MyLocation myLocation = params[0];

            startTimeInMillis = System.currentTimeMillis();

            String methodString = PreferenceUtils.getAzanCalcMethodFromPreferences(MainActivity.this);

            if (methodString == null || methodString.length() == 0) {
                methodString = AzanCalcMethodUtils.getDefaultCalcMethod(MainActivity.this,
                        myLocation.getLongitude(), myLocation.getLatitude());
                sCalcMethodSetDefaultState = true;
                // Note the next line will cause calling to the function onSharedPreferenceChanged()
                PreferenceUtils.setAzanCalcMethodInPreferences(MainActivity.this, methodString);

            }

            try {
                return FetchDataUtils.getJsonResponseArray(MainActivity.this, myLocation,
                        STORING_TOTAL_DAYS_NUM, startTimeInMillis);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "error in getting json response from the url");
                return null;
            }
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((TextView) findViewById(R.id.downloading_wait_or_failed_text_view)).setText(R.string.please_wait_message);
            showDownloadingWaitOrFailedLayout();
        }

        @Override
        protected void onPostExecute(String[] jsonResponseArray) {

            if (jsonResponseArray == null) {
                ((TextView) findViewById(R.id.downloading_wait_or_failed_text_view)).setText(R.string.failed_to_load_data);
                return;
            }

            hideDownloadingWaitOrFailedLayout();

            try {

                FetchDataUtils.saveAzanAppDataInPreferences(MainActivity.this, jsonResponseArray,
                        STORING_TOTAL_DAYS_NUM, startTimeInMillis);

                /* for testing making sure the data we get from json is identical for what is
                 * displayed in the app
                 */

                mTodayDateString =
                        AzanAppTimeUtils.convertMillisToDateString(System.currentTimeMillis());
                setDateAndAzanTimesViews(mTodayDateString);

                ScheduleAlarmTask.scheduleTaskForNextAzanTime(MainActivity.this);
                AzanWidgetService.startActionDisplayAzanTime(MainActivity.this);

                // reset fetch extra data counter
                PreferenceUtils.setFetchExtraCounter(MainActivity.this, 0);

                // Notification
                final Context context = MainActivity.this;
                // 0. cancel the previous notification if exists
                NotificationUtil.cancelNotification(context);
                // 1. update notification data with the text of next azan time
                NotificationData.setCustomizedNotificationData(NotificationUtil.generateCustomizedNotificationData(context));
                // 2. show the new notification generated from NotificationData after updating
                NotificationUtil.generateNotification(context, LOG_TAG);

            } catch (JSONException e) {
                Log.e(LOG_TAG, "can't get data from the json response");
                e.printStackTrace();
            }
        }
    }



    private void setDateAndAzanTimesViews(String dateString) {

        String[] allAzanTimesIn24Format =
                PreferenceUtils.getAzanTimesIn24Format(this, dateString);

        if (allAzanTimesIn24Format == null)
            return; // No Azan times stored for this date, so Nothing to do!

        setAzanTimesViews(allAzanTimesIn24Format);

        unhighlightAllTimesViews();
        highlightNextTimeView(dateString);

        if (getResources().getBoolean(R.bool.use_day_name_for_date)) {
            String dayName = new SimpleDateFormat("E", Locale.getDefault())
                    .format(new Timestamp(AzanAppTimeUtils.convertDateToMillis(dateString)));
            mDateTextView.setText(dayName);
        } else {
            String localeDateString = new SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                    .format(new Timestamp(AzanAppTimeUtils.convertDateToMillis(dateString)));
            mDateTextView.setText(localeDateString);
        }

        mCurrentDateDisplayed = dateString;
    }

    private void setAzanTimesViews(String[] allAzanTimesIn24Format) {

        for (int i = 0; i < ALL_TIMES_NUM; i++) {
            String azanTimeIn24HourFormat = allAzanTimesIn24Format[i];

            if (PreferenceUtils.getTimeFormatFromPreferences(this)
                    .equals(getString(R.string.pref_time_format_24_hour))) {
                mAllAzanTimesTextViews[i].setText(AzanAppTimeUtils.getTimeWithDefaultLocale(azanTimeIn24HourFormat));
            } else {
                mAllAzanTimesTextViews[i].setText(AzanAppTimeUtils.convertTo12HourFormat(azanTimeIn24HourFormat));
            }

        }
    }

    /**
     * we highlight the next time according to a give dateString
     */
    private void highlightNextTimeView(String dateString) {
        long dateInMillis = AzanAppTimeUtils.convertDateToMillis(dateString);
        long todayInMillis = AzanAppTimeUtils.convertDateToMillis(mTodayDateString);
        long tomorrowInMillis = todayInMillis + AzanAppTimeUtils.DAY_IN_MILLIS;

        if (PreferenceUtils.getAzanTimesIn24Format(this, dateString) == null)
            return; // No Azan times stored so Nothing to do

        int indexOfCurrentTime = AzanAppHelperUtils.getIndexOfCurrentTime(this);

        if (dateInMillis >= todayInMillis + AzanAppTimeUtils.DAY_IN_MILLIS * 2)
            return; // no point for continue
        if (dateInMillis == tomorrowInMillis && indexOfCurrentTime != ALL_TIMES_NUM - 1)
            return; // no point for continue

        if (dateInMillis == tomorrowInMillis && indexOfCurrentTime == ALL_TIMES_NUM - 1) {
            // we want to highlight fajr in the next day (tomorrow) in case the current time is ishaa
            mAllAzanTimesLayouts[0].setBackgroundResource(R.color.colorPrimaryLight);

        } else if (indexOfCurrentTime == ALL_TIMES_NUM - 1) {
            /*
             * in this case we didn't highlight the azan of the next time until the time pass
             * 12:00 AM for example if the current time is ISHAA, we will leave highlighted time
             * is ISHAA until the time pass 12:00 AM
             */
            mAllAzanTimesLayouts[ALL_TIMES_NUM - 1]
                    .setBackgroundResource(R.color.colorPrimaryLight);
        } else if (indexOfCurrentTime < 0) {
            // in this case the current time between midnight and the first time
            mAllAzanTimesLayouts[0]
                    .setBackgroundResource(R.color.colorPrimaryLight);
        } else {
            int indexOfNextTime = indexOfCurrentTime + 1;

            mAllAzanTimesLayouts[indexOfNextTime]
                    .setBackgroundResource(R.color.colorPrimaryLight);
        }
    }

    // ====================== Helper Methods (Non Core Methods) =============================
    private void unhighlightAllTimesViews() {
        for (View azanTimeLayout : mAllAzanTimesLayouts) {
            azanTimeLayout.setBackgroundResource(android.R.color.transparent);
        }
    }

    private void showDownloadingWaitOrFailedLayout() {
        findViewById(R.id.downloading_wait_or_failed_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.azan_main_layout).setVisibility(View.GONE);
    }

    private void hideDownloadingWaitOrFailedLayout() {
        findViewById(R.id.downloading_wait_or_failed_layout).setVisibility(View.GONE);
        findViewById(R.id.azan_main_layout).setVisibility(View.VISIBLE);
    }

    private void hideErrorNoConnectionLayout() {
        findViewById(R.id.error_no_connection_layout).setVisibility(View.GONE);
        findViewById(R.id.azan_main_layout).setVisibility(View.VISIBLE);
    }

    private void showErrorNoConnectionLayout() {
        findViewById(R.id.error_no_connection_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.azan_main_layout).setVisibility(View.GONE);
    }




}



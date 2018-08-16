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

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import free.elmasry.azan.alarm.ScheduleAlarmTask;
import free.elmasry.azan.utilities.AladhanJsonUtils;
import free.elmasry.azan.utilities.AzanAppTimeUtils;
import free.elmasry.azan.utilities.ReverseGeoCoding;
import free.elmasry.azan.utilities.HelperUtils;
import free.elmasry.azan.utilities.NetworkUtils;
import free.elmasry.azan.utilities.PreferenceUtils;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();


    TextView mDateTextView;

    private TextView[] mAllAzanTimesTextViews;
    private View[] mAllAzanTimesLayouts;

    private FusedLocationProviderClient mFusedLocationClient;

    private static final int MY_PERMISSION_LOCATION_REQUEST_CODE = 305;


    private static final boolean DEBUG = false;


    /*
     * these variables are very important it's used in many classes and all arrays related to
     * azan times are built based on them. we assumed when writing the code for this app, any array
     * contains these times it will order ascending according to the time
     */
    public static final int ALL_TIMES_NUM = 6;
    public static final int INDEX_FAJR = 0;
    public static final int INDEX_SHUROOQ = 1;
    public static final int INDEX_DHUHR = 2;
    public static final int INDEX_ASR = 3;
    public static final int INDEX_MAGHRIB = 4;
    public static final int INDEX_ISHAA = 5;

    private static final int STORING_TOTAL_DAYS_NUM = 90;
    private static final int MAX_DAYS_OFFSET_FOR_DISPLAY = 6;

    private String mCurrentDateDisplayed;

    private String mTodayDateString;

    private static boolean sCalcMethodPreferenceUpdated = false;
    private static boolean sCalcMethodSetDefaultState = false;

    private static final String CURRENT_DATE_DISPLAYED_KEY = "current-date-displayed-key";


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


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        /*
         * for the devices its api level less than 17 and its default language is arabic the
         * direction for the layout doesn't change automatically to rtl and there is no way to
          * do it by xml
         */
        if (Build.VERSION.SDK_INT < 17 && Locale.getDefault().getLanguage().equals("ar")) {
            for (View ll : mAllAzanTimesLayouts)
                HelperUtils.changeDirectionOfLinearLayout((LinearLayout)ll);
        }


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
            case R.id.action_reloading_data:
                if (HelperUtils.isDeviceOnline(this)) {
                    fetchData();
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
                fetchData();
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
                fetchData();
            }
            else showErrorNoConnectionLayout();
        } else {
            setDateAndAzanTimesViews(mTodayDateString);
        }

        // for testing alarm at certain time
//        ScheduleAlarmTask.scheduleAlarmForStartingAzanSoundActivityAt(this, "2 jan 2018", 22, 27);

        if (mCurrentDateDisplayed != null) ScheduleAlarmTask.scheduleTaskForNextAzanTime(this);
    }


    private void fetchData() {


//        if (DEBUG) {
//            new FetchAzanTimes().execute(29.9187387, 31.2000924);
//            return;
//        }

        boolean permissionGranted =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // ask again
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_LOCATION_REQUEST_CODE);
                return; // No point for continue
            }
        }


         // get the location of the user then fetching the data from the internet
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            PreferenceUtils.clearAllAzanTimesStoredInPreferences(MainActivity.this);
                            new FetchAzanTimes().execute(longitude, latitude);
                        } else {
                            showLocationErrDialogue(getString(R.string.location_problem_title), getString(R.string.location_problem_message));
                        }
                    }
                });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_LOCATION_REQUEST_CODE:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    HelperUtils.showToast(this, R.string.error_no_permission_granted_message, Toast.LENGTH_LONG);
                    finish();
                } else {
                    fetchData();
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
                        if (HelperUtils.isLocationEnabled(MainActivity.this)) {
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
            fetchData();
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
        Intent intent = new Intent(this, PlayAzanSound.class);
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String preferenceKey) {
        if (preferenceKey.equals(getString(R.string.pref_calc_method_key))) {

            if (sCalcMethodSetDefaultState) sCalcMethodSetDefaultState = false;
            else sCalcMethodPreferenceUpdated = true;

        }
    }

    private class FetchAzanTimes extends AsyncTask<Double, Void, String[]> {

        private long startTimeInMillis;

        @Override
        protected String[] doInBackground(Double... params) {
            double longitude = params[0];
            double latitude = params[1];

            String[] jsonResponseArray = new String[STORING_TOTAL_DAYS_NUM];

            startTimeInMillis = System.currentTimeMillis();

            for (int i = 0; i < STORING_TOTAL_DAYS_NUM; i++) {
                try {
                    long dayInSeconds = TimeUnit.DAYS.toSeconds(1);
                    long dateInSeconds = startTimeInMillis / 1000 + dayInSeconds * i;

                    String methodString = PreferenceUtils.getAzanCalcMethodFromPreferences(MainActivity.this);

                    if (methodString == null || methodString.length() == 0) {
                        methodString = getDefaultCalcMethod(longitude, latitude);
                        sCalcMethodSetDefaultState = true;
                        // Note the next line will cause calling to the function onSharedPreferenceChanged()
                        PreferenceUtils.setAzanCalcMethodInPreferences(MainActivity.this, methodString);

                    }

                    URL url = NetworkUtils.buildUrl(dateInSeconds, longitude, latitude, getAzanCalcMethodInt(methodString));
                    jsonResponseArray[i] = NetworkUtils.gettingResponseFromHttpUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "error in getting json response from the url");
                    return null;
                }
            }
            return jsonResponseArray;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDownloadingWaitLayout();
        }

        @Override
        protected void onPostExecute(String[] jsonResponseArray) {

            hideDownloadingWaitLayout();

            try {

                // storing all azan times for different days from jsonResponseArray into preferences
                // file
                for (int i = 0; i < STORING_TOTAL_DAYS_NUM; i++) {

                    String dateString = AzanAppTimeUtils.convertMillisToDateString(
                            startTimeInMillis + i * AzanAppTimeUtils.DAY_IN_MILLIS
                    );

                    PreferenceUtils.setAzanTimesForDay(MainActivity.this, dateString,
                            AladhanJsonUtils.getAllAzanTimesIn24Format(jsonResponseArray[i]));

                }

                /* for testing and making sure the data we get from json is identical for what is
                 * displayed in the app
                 */

                mTodayDateString =
                        AzanAppTimeUtils.convertMillisToDateString(System.currentTimeMillis());
                setDateAndAzanTimesViews(mTodayDateString);
                ScheduleAlarmTask.scheduleTaskForNextAzanTime(MainActivity.this);

            } catch (JSONException e) {
                Log.e(LOG_TAG, "can't get data from the json response");
                e.printStackTrace();
            }
        }
    }

    /**
     * in the first time the user installs this app, we do our best to get the correct azan
     * calculation method for his country
     * @param longitude
     * @param latitude
     * @return the guessed Azan calculation method for the user
     */
    private String getDefaultCalcMethod(double longitude, double latitude) {

        String countryName = new ReverseGeoCoding(latitude, longitude).getCountry()
                .trim().toLowerCase();

        final String[] COUNTRIES_UMM_AL_QURA =
                {"Saudi Arabia", "Yemen", "Jordan", "United Arab Emirates", "Qatar", "Bahrain"};

        //Log.d(LOG_TAG, "country: " + countryName);
        final String COUNTRY_KUWAIT = "Kuwait";

        for (String c : COUNTRIES_UMM_AL_QURA)
            if (c.toLowerCase().equals(countryName))
                return this.getString(R.string.pref_calc_method_umm_al_qura);

        if (countryName.equals(COUNTRY_KUWAIT.toLowerCase()))
            return this.getString(R.string.pref_calc_method_kuwait);

        return this.getString(R.string.pref_calc_method_egyptian_general_authority);
    }

    private void setDateAndAzanTimesViews(String dateString) {

        String[] allAzanTimesIn24Format =
                PreferenceUtils.getAzanTimesIn24Format(this, dateString);
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

            if (getResources().getBoolean(R.bool.use_24_hour_format)) {
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

        int indexOfCurrentTime = AzanAppTimeUtils.getIndexOfCurrentTime(this);

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

    private void showDownloadingWaitLayout() {
        findViewById(R.id.downloading_wait_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.azan_main_layout).setVisibility(View.GONE);
    }

    private void hideDownloadingWaitLayout() {
        findViewById(R.id.downloading_wait_layout).setVisibility(View.GONE);
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

    /**
     * getting azan calculation method int that used in aladhan api
     * @param methodString the calculation method that stored in the preference file
     * @return azan calculation method int corresponding to a given method calculation
     */
    private int getAzanCalcMethodInt(String methodString) {

        if (methodString.equals(getString(R.string.pref_calc_method_egyptian_general_authority)))
            return NetworkUtils.METHOD_EGYPTIAN_GENERAL_AUTHORITY;

        if (methodString.equals(getString(R.string.pref_calc_method_umm_al_qura)))
            return NetworkUtils.METHOD_UMM_AL_QURA;

        if (methodString.equals(getString(R.string.pref_calc_method_mwl)))
            return NetworkUtils.METHOD_MUSLIM_WORLD_LEAGUE;

        if (methodString.equals(getString(R.string.pref_calc_method_kuwait)))
            return NetworkUtils.METHOD_KUWAIT;

        if (methodString.equals(getString(R.string.pref_calc_method_islamic_university_karachi)))
            return NetworkUtils.METHOD_UNIVERSITY_ISLAMIC_SCIENCE_KARACHI;

        if (methodString.equals(getString(R.string.pref_calc_method_isna)))
            return NetworkUtils.METHOD_ISLAMIC_SOCIETY_NORTH_AMERICA;

        throw new RuntimeException("unknown azan calculation method: " + methodString);

    }



}



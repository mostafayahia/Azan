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


package free.elmasry.azan.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import free.elmasry.azan.R;

import static free.elmasry.azan.utilities.LocationUtils.MyLocation;

/**
 * Created by yahia on 12/25/17.
 */

public class PreferenceUtils {

    private static final String AZAN_TIME_SEPARATOR = "ZZZ";

    private static final String AZAN_LOCATION_LONGITUDE_KEY = "azan-location-longitude-key";
    private static final String AZAN_LOCATION_LATITUDE_KEY = "azan-location-latitude-key";

    private static final String FETCH_EXTRA_COUNT_KEY = "fetch-extra-count-key";
    private static final String FETCH_EXTRA_LAST_DATE_TIME_STRING_KEY = "fetch-extra-last-date-time-string-key";

    /**
     * save in preference object azan times for a certain day
     * @param dateString it will be in the form "12 Dec 2017" or "12 dec 2017"
     * @param context the base context of the application
     */
    public static void setAzanTimesForDay(Context context, String dateString, String[] allAzanTimesIn24Format) {

        dateString = dateString.trim().toLowerCase();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        String azanTimesString = "";
        for (String azanTime : allAzanTimesIn24Format)
            azanTimesString += azanTime + AZAN_TIME_SEPARATOR;
        azanTimesString.substring(0, azanTimesString.length() - AZAN_TIME_SEPARATOR.length());

        editor.putString(dateString, azanTimesString);

        editor.apply();

    }

    /**
     *
     * @param context the base context of the application
     * @param dateString in the form like "10 Jan 2018"
     * @return azan times in 24-hour format or null if azan times for this date wasn't stored
     */
    public static String[] getAzanTimesIn24Format(Context context, String dateString) {

        dateString = dateString.trim().toLowerCase();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String azanTimesString = preferences.getString(dateString, null);

        if (azanTimesString != null && azanTimesString.length() != 0)
            return azanTimesString.split(AZAN_TIME_SEPARATOR);
        else
            return null;
    }

    /**
     * clear all the data stored in shared preferences
     * @param context the base context of the application
     */
    public static void clearAllAzanTimesStoredInPreferences(Context context) {

        String todayString = AzanAppTimeUtils.convertMillisToDateString(System.currentTimeMillis());

        String dateString = todayString;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        while (true) {
            dateString = dateString.trim().toLowerCase();

            String[] allAzanTimesIn24Format = PreferenceUtils.getAzanTimesIn24Format(context, dateString);

            if (allAzanTimesIn24Format != null) {
                editor.putString(dateString, "");
            } else {
                break;
            }

            dateString = AzanAppTimeUtils.getDayAfterDateString(dateString);
        }

        // VERY IMPORTANT WITHOUT NEXT LINE, YOU HAVE NO EFFECT
        editor.commit();
    }

    /**
     * getting last stored date for Azan times for that
     * the form will be like "13 Jun 2019"
     * @return
     */
    public static String getLastStoredDateStringForData(Context context) {
        String todayString = AzanAppTimeUtils.convertMillisToDateString(System.currentTimeMillis());

        if (PreferenceUtils.getAzanTimesIn24Format(context, todayString) == null)
            return "";

        String lastStoredDateString = todayString;

        String testDateString = AzanAppTimeUtils.getDayAfterDateString(lastStoredDateString);

        while (PreferenceUtils.getAzanTimesIn24Format(context, testDateString) != null) {
            lastStoredDateString = testDateString;
            testDateString = AzanAppTimeUtils.getDayAfterDateString(lastStoredDateString);
        }

        return lastStoredDateString;
    }

    public static String getAzanAudioFromPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_azan_audio_key),
                        context.getString(R.string.pref_audio_takbeer));
    }

    public static String getAzanCalcMethodFromPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_calc_method_key),
                        context.getString(R.string.pref_calc_method_default_value));

    }


    public static void setAzanCalcMethodInPreferences(Context context, String methodString) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(context.getString(R.string.pref_calc_method_key), methodString);
        editor.apply();
    }

    /**
     * save user location in shared preferences
     * @param context
     * @param myLocation
     */
    public static void setUserLocation(Context context, MyLocation myLocation) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(AZAN_LOCATION_LONGITUDE_KEY, myLocation.getLongitude()+"");
        editor.putString(AZAN_LOCATION_LATITUDE_KEY, myLocation.getLatitude()+"");
        editor.apply();
    }

    /**
     * get user location from shared preferences
     * @param context
     * @return
     */
    public static MyLocation getUserLocation(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String longitudeText = sharedPreferences.getString(AZAN_LOCATION_LONGITUDE_KEY, "");
        String latitudeText = sharedPreferences.getString(AZAN_LOCATION_LATITUDE_KEY, "");

        MyLocation myLocation = new MyLocation();

        if (!TextUtils.isEmpty(longitudeText) && !TextUtils.isEmpty(latitudeText)) {
            myLocation.setLongitude(Double.valueOf(longitudeText));
            myLocation.setLatitude(Double.valueOf(latitudeText));
        }

        return myLocation;
    }

    /**
     * save the number of successful fetching for extra data in shared preferences
     * @param context
     * @param count
     */
    public static void setFetchExtraCounter(Context context, int count) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(FETCH_EXTRA_COUNT_KEY, count);
        editor.apply();
    }

    /**
     * get the number of successful fetching for extra data from shared preferences
     * @param context
     * @return
     */
    public static int getFetchExtraCounter(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(FETCH_EXTRA_COUNT_KEY, 0);
    }

    /**
     * save the date time string like "13 Jun 2018 13:10:22" for the last successful fetching for data
     * in share preferences
     * @param context
     * @param lastDateTimeString
     */
    public static void setFetchExtraLastDateTimeString(Context context, String lastDateTimeString) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(FETCH_EXTRA_LAST_DATE_TIME_STRING_KEY, lastDateTimeString);
        editor.apply();

    }

    /**
     * get the date time string like "13 Jun 2018 13:10:22" for the last successful fetching for data
     * from shared preferences
     * @param context
     * @return
     */
    public static String getFetchExtraLastDateTimeString(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(FETCH_EXTRA_LAST_DATE_TIME_STRING_KEY, "");
    }

}

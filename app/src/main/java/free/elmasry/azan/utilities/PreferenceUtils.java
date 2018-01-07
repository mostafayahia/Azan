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

import free.elmasry.azan.R;

/**
 * Created by yahia on 12/25/17.
 */

public class PreferenceUtils {

    private static final String AZAN_TIME_SEPARATOR = "ZZZ";
    private static final String AZAN_CALC_METHOD_KEY = "azan-calc-method-key";

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
        editor.commit();
    }

}

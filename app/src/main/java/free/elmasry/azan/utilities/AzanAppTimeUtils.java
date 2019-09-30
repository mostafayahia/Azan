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


import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by yahia on 12/22/17.
 */

public class AzanAppTimeUtils {

    /**
     * convert time from 14:00 to 2:00
     *
     * @param time24Hour time in 24-hour format
     * @return time in 12-hour format
     */

    private static final String LOG_TAG = AzanAppTimeUtils.class.getSimpleName();

    public static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);

    /**
     * convert time from 24-hour format to 12-hour format
     *
     * @param time24Hour time in the form like "13:00"
     * @return return "01:00" if the given parameter "13:00"
     */
    public static String convertTo12HourFormat(String time24Hour) {

        long hour24Format = getHourFromTime(time24Hour);

        long hour12Format = (hour24Format > 12) ? hour24Format - 12 : hour24Format;

        long minute = getMinuteFromTime(time24Hour);

        // using String.format return the number in arabic if Arabic is the default language for the device
        return String.format("%02d:%02d", hour12Format, minute);
    }

    /**
     * getting time according to the default locale of the device
     *
     * @param hourAndMinute in the format like "06:13"
     * @return time using default locale, for example if the default locale is arabic, it'll convert
     * the numbers from english to arabic (like "۱۲:۲۳")
     */
    public static String getTimeWithDefaultLocale(String hourAndMinute) {
        return String.format("%02d:%02d",
                AzanAppTimeUtils.getHourFromTime(hourAndMinute), AzanAppTimeUtils.getMinuteFromTime(hourAndMinute));
    }

    /**
     * extract hour from the time if the time is given in the form "01:30"
     *
     * @param hourAndMinuteString the give in the form "13:00"
     * @return return 1 if the given parameter is "01:30"
     */
    public static int getHourFromTime(String hourAndMinuteString) {
        String[] hourAndMinute = hourAndMinuteString.split(":");
        return Integer.parseInt(hourAndMinute[0]);
    }

    /**
     * extract minute from the time if the time is given in the form "01:30"
     *
     * @param hourAndMinuteString the give in the form "13:00"
     * @return return 5 if the given parameter is "13:05"
     */
    public static int getMinuteFromTime(String hourAndMinuteString) {
        String[] hourAndMinute = hourAndMinuteString.split(":");
        return Integer.parseInt(hourAndMinute[1]);
    }


    /**
     * convert month from Feb or February to 2
     *
     * @param monthString month string like Mar or March or mar
     * @return number representing the month like 1 (representing January)
     */
    public static int convertMonthFromStringToInt(String monthString) {

        String[] months =
                {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        monthString = monthString.trim().toLowerCase();
        for (int i = 0; i < 12; i++) {
            if (monthString.startsWith(months[i].toLowerCase())) {
                // Note January represents 1 and December represents 12
                return i + 1;
            }
        }

        throw new RuntimeException("can't convert month from string to int, the given parameter:  " + monthString);

        // for testing this method look and run AzanTimeUtilsTest in app/src/test/java/.....

    }

    /**
     * convert month from 2 to Feb
     *
     * @param monthInt number represent the month must be between 1 and 12
     * @return String represents the month like "Jan" or "Aug"
     */
    public static String convertMonthFromIntToString(int monthInt) {

        if (monthInt > 12 || monthInt < 1)
            throw new RuntimeException("the monthInt must be between 1 and 12, the given parameter:  " + monthInt);


        String[] monthsString =
                {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};


        return monthsString[monthInt - 1];

    }


    /**
     * @param dayMonthYearString in the format like "2 Dec 2017" or "02 December 1990"
     * @return the date in Milliseconds at midnight (00:00:0.0) of your region but in UTC/GMT time
     */
    public static long convertDateToMillis(String dayMonthYearString) {

        return convertDateToMillis(dayMonthYearString, 0, 0);

        // for testing this method look and run AzanTimeUtilsTest in app/src/test/java/.....
    }


    /**
     * @param dayMonthYearString in the format like "2 Dec 2017" or "02 December 1990"
     * @param hourIn24           hour in 24-hour format like 13
     * @param minute             the minute which is value between 0 to 59
     * @return the date in Milliseconds at the clock hh:mm:0.0 of your region but in UTC/GMT time
     */
    public static long convertDateToMillis(String dayMonthYearString, int hourIn24, int minute) {

        String[] dayAndMonthAndYear = dayMonthYearString.trim().split(" ");
        int day = Integer.parseInt(dayAndMonthAndYear[0]);
        int month = AzanAppTimeUtils.convertMonthFromStringToInt(dayAndMonthAndYear[1]);
        int year = Integer.parseInt(dayAndMonthAndYear[2]);
        Calendar calendar = Calendar.getInstance();

        // month in calendar starts from 0(Jan) to 11(Dec)
        calendar.set(year, month - 1, day, hourIn24, minute, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }


    /**
     * convert date from millis to form like "02 Dec 2013"
     *
     * @param dateInMillis date in milliseconds
     * @return String in the form like "02 Jan 2010"
     */
    public static String convertMillisToDateString(long dateInMillis) {

        /*
         * DON'T CHANGE THIS DATE PATTERN OR LOCALE, IT WILL MISMATCH FROM THE DATE SAVED IN THE SHARED_PREFERENCE
         * AND THE APP MAY BE CRASH OR WON'T WORK CORRECTLY
         */

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // the given number is zero based
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        //return new SimpleDateFormat("dd MMM yyyy", new Locale("en")).format(dateInMillis);
        return ((day < 10) ? "0" : "") + day + " " + // prefixing the day with 0 if day < 10
                convertMonthFromIntToString(month) + " " +
                year;
    }

    /**
     * day after the given date string in the format like "03 Dec 2011"
     *
     * @param dateString in the form "02 Dec 2014"
     * @return "03 Dec 2014" if the given parameter is "02 Dec 2014"
     */
    public static String getDayAfterDateString(String dateString) {
        if (dateString == null || dateString.trim().isEmpty())
            throw new IllegalArgumentException("Not valid dateString, the given one: " + dateString);

        dateString = dateString.trim().toLowerCase();
        String[] dateArray = dateString.split(" +");

        final int day = Integer.parseInt(dateArray[0]);
        final String monthString = dateArray[1].substring(0, 3).toLowerCase();
        final int year = Integer.parseInt(dateArray[2]);

        final String thirtyOneDaysMonths = "jan-mar-may-jul-aug-oct-dec";
        final String thirtyDayMonths = "apr-jun-sep-nov";
        final String febMonth = "feb";
        final String decMonth = "dec";

        // set the default values of the date for day after
        int yearAfterIncrease = year;
        int dayAfterIncrease = day + 1;
        String monthStringAfterIncrease = monthString;

        // handle exceptions
        if ((day == 28 && monthString.equals(febMonth) && (year % 4) != 0) ||
                (day == 29 && monthString.equals(febMonth) && (year % 4) == 0) ||
                (day == 30 && thirtyDayMonths.contains(monthString)) ||
                (day == 31 && thirtyOneDaysMonths.contains(monthString))
        ) {
            dayAfterIncrease = 1;
            final int monthIntAfterIncrease = monthString.equals(decMonth) ? 1 :
                    convertMonthFromStringToInt(monthString) + 1;
            monthStringAfterIncrease = convertMonthFromIntToString(monthIntAfterIncrease);
        }

        // if Dec 31 then the day after will be in the new year
        if (day == 31 && monthString.equals(decMonth))
            yearAfterIncrease = year + 1;

        return // prefixing the day with "0" if day < 10
                ((dayAfterIncrease < 10) ? "0" : "") + dayAfterIncrease + " " +
                // make first letter of the month capital like Mar not mar
                monthStringAfterIncrease.substring(0, 1).toUpperCase() + monthStringAfterIncrease.substring(1) + " " +
                // year
                yearAfterIncrease;
    }

    /**
     * day before the given date string in the format like "03 Dec 2011"
     *
     * @param dateString in the form "02 Dec 2014"
     * @return "01 Dec 2014" if the given parameter is "02 Dec 2014"
     */
    public static String getDayBeforeDateString(String dateString) {
        long dateInMillis =
                AzanAppTimeUtils.convertDateToMillis(dateString) - DAY_IN_MILLIS;
        return AzanAppTimeUtils.convertMillisToDateString(dateInMillis);
    }

    /**
     * convert timeInMillis to date time string ex: 13 Jun 2018 13:10:22
     *
     * @param timeInMillis
     * @return
     */
    public static String convertMillisToDateTimeString(long timeInMillis) {
        return new SimpleDateFormat("dd MMM yyyy HH:mm:ss", new Locale("en"))
                .format(timeInMillis);
    }

    /**
     * get date string like "13 Jun 2018" from date time string like that "13 Jun 2018 15:10:22"
     *
     * @param dateTimeString
     * @return
     */
    public static String getDateStringFromDateTimeString(String dateTimeString) {
        if (TextUtils.isEmpty(dateTimeString)) return "";

        String[] array = dateTimeString.split(" +");
        return array[0] + " " + array[1] + " " + array[2];
    }


}

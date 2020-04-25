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


package free.elmasry.azan.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

import free.elmasry.azan.utilities.AzanAppHelperUtils;
import free.elmasry.azan.utilities.AzanAppTimeUtils;
import free.elmasry.azan.utilities.PreferenceUtils;

import static free.elmasry.azan.shared.AzanTimeIndex.INDEX_FAJR;
import static free.elmasry.azan.shared.AzanTimeIndex.INDEX_ISHAA;

/**
 * Created by yahia on 12/31/17.
 */

public class ScheduleAlarmTask {

    private static final String LOG_TAG = ScheduleAlarmTask.class.getSimpleName();

    public static final String ACTION_PLAY_AZAN_SOUND = "free.elmasry.azan.action.play_azan_sound";
    public static final String ACTION_PLAY_EQAMAH_SOUND = "free.elmasry.azan.action.play_eqamah_sound";

    /**
     * Schedule alaram to start AzanSoundActivity
     *
     * @param context
     * @param dateString     in the format "02 Dec 2018" or "01 jan 2010"
     * @param hourIn24Format
     * @param minute
     */
    public static void scheduleAlarmForStartingAzanSoundActivityAt(Context context, String dateString, int hourIn24Format, int minute) {

        long timeInMillis = AzanAppTimeUtils.convertDateToMillis(dateString, hourIn24Format, minute);

        Intent scheduledReceiverIntent = new Intent(context, ScheduledReceiver.class);
        scheduledReceiverIntent.setAction(ACTION_PLAY_AZAN_SOUND);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0,
                scheduledReceiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        final int ALARM_TYPE = AlarmManager.RTC_WAKEUP;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(ALARM_TYPE, timeInMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            am.setExact(ALARM_TYPE, timeInMillis, pendingIntent);
        } else {
            am.set(ALARM_TYPE, timeInMillis, pendingIntent);
        }
    }

    public static void scheduleAlarmForStartingEqamahSoundActivityAt(Context context, String dateString, int hourIn24Format, int minute) {

        long timeInMillis = AzanAppTimeUtils.convertDateToMillis(dateString, hourIn24Format, minute);

        Intent scheduledReceiverIntent = new Intent(context, ScheduledReceiver.class);
        scheduledReceiverIntent.setAction(ACTION_PLAY_EQAMAH_SOUND);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0,
                scheduledReceiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);



        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        final int ALARM_TYPE = AlarmManager.RTC_WAKEUP;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(ALARM_TYPE, timeInMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            am.setExact(ALARM_TYPE, timeInMillis, pendingIntent);
        } else {
            am.set(ALARM_TYPE, timeInMillis, pendingIntent);
        }
    }



    public static void scheduleTaskForNextAzanTime(Context context) {

        final AzanTime nextAzanTime = getNextAzanTime(context);

        if (null == nextAzanTime) return;  // No Azan times stored for today so Nothing to do

        scheduleAlarmForStartingAzanSoundActivityAt(context, nextAzanTime.dateString,
                nextAzanTime.hourIn24Format, nextAzanTime.minute);

        // for testing
//        final int testMinute = 11;
//        final Calendar c = Calendar.getInstance();
//        final int testHour = c.getTime().getHours();
//        c.set(Calendar.MINUTE, testMinute);
//        if (c.getTimeInMillis() - System.currentTimeMillis() > 1000 * 30)
//            ScheduleAlarmTask.scheduleAlarmForStartingAzanSoundActivityAt(context, nextAzanTime.dateString, testHour, testMinute);

    }

    private static AzanTime getNextAzanTime(Context context) {

        String todayDateString = AzanAppTimeUtils.convertMillisToDateString(System.currentTimeMillis());
        if (PreferenceUtils.getAzanTimesIn24Format(context, todayDateString) == null)
            return null; // No Azan times stored for today

        int indexOfCurrentTime = AzanAppHelperUtils.getIndexOfCurrentTime(context);

        int indexOfNextAzanTime;
        String[] allAzanTimesIn24Format;
        String dateString;

        if (indexOfCurrentTime >= INDEX_ISHAA) {
            String tomorrowDateString = AzanAppTimeUtils.getDayAfterDateString(todayDateString);
            allAzanTimesIn24Format = PreferenceUtils.getAzanTimesIn24Format(context, tomorrowDateString);
            indexOfNextAzanTime = INDEX_FAJR;
            dateString = tomorrowDateString;
        } else {
            if (indexOfCurrentTime < INDEX_FAJR)
                indexOfNextAzanTime = INDEX_FAJR;
            else
                indexOfNextAzanTime = indexOfCurrentTime + 1;
            allAzanTimesIn24Format = PreferenceUtils.getAzanTimesIn24Format(context, todayDateString);
            dateString = todayDateString;
        }


        String hourAndMinuteIn24 = allAzanTimesIn24Format[indexOfNextAzanTime];

        int hourIn24Format = AzanAppTimeUtils.getHourFromTime(hourAndMinuteIn24);
        int minute = AzanAppTimeUtils.getMinuteFromTime(hourAndMinuteIn24);

        AzanTime azanTime = new AzanTime(hourIn24Format, minute, dateString);

        return azanTime;
    }

    private static class AzanTime {
        final int hourIn24Format;
        final int minute;
        final String dateString;

        AzanTime(int hourIn24Format, int minute, String dateString) {
            this.hourIn24Format = hourIn24Format;
            this.minute = minute;
            this.dateString = dateString;
        }
    }

    public static void scheduleTaskForNextEqamahTime(Context context) {

        final AzanTime nextAzanTime = getNextAzanTime(context);

        if (null == nextAzanTime) return;  // No Azan times stored for today so Nothing to do

        final int EQAMA_TIME_DIFF_IN_MIN = 20;

        int hourIn24 = nextAzanTime.hourIn24Format;
        int minute = nextAzanTime.minute;

        hourIn24 = (minute + EQAMA_TIME_DIFF_IN_MIN >= 60) ? hourIn24 + 1 : hourIn24;

        // if time after eqamah exceeds 23:59, it will be set to 23:59
        if (hourIn24 > 23) {
            minute = 59;
            hourIn24 = 23;
        } else {
            minute = (minute + EQAMA_TIME_DIFF_IN_MIN) % 60;
        }

        scheduleAlarmForStartingEqamahSoundActivityAt(context, nextAzanTime.dateString,
                hourIn24, minute);

        // for testing
//        final int testMinute = 16;
//        final Calendar c = Calendar.getInstance();
//        final int testHour = c.getTime().getHours();
//        c.set(Calendar.MINUTE, testMinute);
//        if (c.getTimeInMillis() - System.currentTimeMillis() > 1000 * 30)
//            ScheduleAlarmTask.scheduleAlarmForStartingEqamahSoundActivityAt(context, nextAzanTime.dateString, testHour, testMinute);

    }

    public static void removeScheduledTaskForEqamahTime(Context context) {
        Intent scheduledReceiverIntent = new Intent(context, ScheduledReceiver.class);
        scheduledReceiverIntent.setAction(ACTION_PLAY_EQAMAH_SOUND);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0,
                scheduledReceiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);



        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }
}

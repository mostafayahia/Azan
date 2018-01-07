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
import android.util.Log;

import free.elmasry.azan.MainActivity;
import free.elmasry.azan.utilities.AzanAppTimeUtils;
import free.elmasry.azan.utilities.PreferenceUtils;

/**
 * Created by yahia on 12/31/17.
 */

public class ScheduleAlarmTask {

    private static final String LOG_TAG = ScheduleAlarmTask.class.getSimpleName();

    public static void scheduleAlarmForStartingAzanSoundActivityAt(Context context, String dateString, int hourIn24Format, int minute) {

        long timeInMillis = AzanAppTimeUtils.convertDateToMillis(dateString, hourIn24Format, minute);

        Log.d(LOG_TAG, timeInMillis + "");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0,
                new Intent(context, ScheduledReceiver.class),
                0);


        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
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

        int indexOfCurrentTime = AzanAppTimeUtils.getIndexOfCurrentTime(context);
        String todayDateString = AzanAppTimeUtils.convertMillisToDateString(System.currentTimeMillis());

        int indexOfNextAzanTime;
        String[] allAzanTimesIn24Format;
        String dateString;

        if (indexOfCurrentTime >= MainActivity.INDEX_ISHAA) {
            String tomorrowDateString = AzanAppTimeUtils.getDayAfterDateString(todayDateString);
            allAzanTimesIn24Format = PreferenceUtils.getAzanTimesIn24Format(context, tomorrowDateString);
            indexOfNextAzanTime = MainActivity.INDEX_FAJR;
            dateString = tomorrowDateString;
        } else {
            if (indexOfCurrentTime < MainActivity.INDEX_FAJR)
                indexOfNextAzanTime = MainActivity.INDEX_FAJR;
            else if (indexOfCurrentTime == MainActivity.INDEX_FAJR)
                indexOfNextAzanTime = MainActivity.INDEX_DHUHR; // not INDEX_SHUROOQ
            else
                indexOfNextAzanTime = indexOfCurrentTime + 1;
            allAzanTimesIn24Format = PreferenceUtils.getAzanTimesIn24Format(context, todayDateString);
            dateString = todayDateString;
        }

        if (!isValidAzanTimeIndex(indexOfNextAzanTime))
            throw new RuntimeException("invalid azan time index: " + indexOfNextAzanTime);

        String hourAndMinuteIn24 = allAzanTimesIn24Format[indexOfNextAzanTime];

        int hourIn24Format = AzanAppTimeUtils.getHourFromTime(hourAndMinuteIn24);
        int minute = AzanAppTimeUtils.getMinuteFromTime(hourAndMinuteIn24);

        scheduleAlarmForStartingAzanSoundActivityAt(context, dateString, hourIn24Format, minute);
    }


    private static boolean isValidAzanTimeIndex(int azanTimeIndex) {
        return azanTimeIndex == MainActivity.INDEX_FAJR ||
                azanTimeIndex == MainActivity.INDEX_DHUHR ||
                azanTimeIndex == MainActivity.INDEX_ASR ||
                azanTimeIndex == MainActivity.INDEX_MAGHRIB ||
                azanTimeIndex == MainActivity.INDEX_ISHAA;
    }
}

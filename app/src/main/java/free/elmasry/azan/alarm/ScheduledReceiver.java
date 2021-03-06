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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import free.elmasry.azan.notification.NotificationData;
import free.elmasry.azan.ui.PlayAzanSound;
import free.elmasry.azan.ui.PlayEqamahSound;
import free.elmasry.azan.utilities.AzanAppHelperUtils;
import free.elmasry.azan.utilities.AzanAppTimeUtils;
import free.elmasry.azan.utilities.NotificationUtil;
import free.elmasry.azan.utilities.PreferenceUtils;
import free.elmasry.azan.widget.AzanWidgetService;

/**
 * Created by yahia on 1/2/18.
 */

public class ScheduledReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = ScheduledReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        // if no data stored for the current azan, nothing will do
        if (PreferenceUtils.getAzanTimesIn24Format(context,
                AzanAppTimeUtils.convertMillisToDateString(System.currentTimeMillis())) == null)
            return;

        int indexOfCurrentAzanTime = AzanAppHelperUtils.getIndexOfCurrentTime(context);

        if (ScheduleAlarmTask.ACTION_PLAY_EQAMAH_SOUND.equals(intent.getAction())) {
            if (AzanAppHelperUtils.isValidPlayAzanTimeIndex(indexOfCurrentAzanTime)) {
                Intent startPlaySoundActivityIntent = new Intent(context, PlayEqamahSound.class);
                startPlaySoundActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startPlaySoundActivityIntent);
            }

        } else {
            if (AzanAppHelperUtils.isValidPlayAzanTimeIndex(indexOfCurrentAzanTime)) {
                Intent startPlaySoundActivityIntent = new Intent(context, PlayAzanSound.class);
                startPlaySoundActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startPlaySoundActivityIntent);
            } else {
                AzanWidgetService.startActionDisplayAzanTime(context);
                ScheduleAlarmTask.scheduleTaskForNextAzanTime(context);
            }
            // Notification
            Log.d(LOG_TAG, "start updating Notification and show them");
            // 0. cancel the previous notification
            NotificationUtil.cancelNotification(context);
            // 1. update notification data with the text of next azan time
            NotificationData.setCustomizedNotificationData(NotificationUtil.generateCustomizedNotificationData(context));
            // 2. show the new notification generated from NotificationData after updating
            NotificationUtil.generateNotification(context, LOG_TAG);
            Log.d(LOG_TAG, "notification is update successfully");
        }
    }
}

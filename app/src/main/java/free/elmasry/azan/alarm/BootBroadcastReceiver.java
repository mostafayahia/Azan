package free.elmasry.azan.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import free.elmasry.azan.notification.NotificationData;
import free.elmasry.azan.utilities.NotificationUtil;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = BroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // for testing
//        ScheduleAlarmTask.scheduleAlarmForStartingAzanSoundActivityAt(context, "1 Apr 2018", 10, 26);
        ScheduleAlarmTask.scheduleTaskForNextAzanTime(context);

        // Notification
        // 0. cancel the previous notification if exists
        NotificationUtil.cancelNotification(context);
        // 1. update notification data with the text of next azan time
        NotificationData.setCustomizedNotificationData(NotificationUtil.generateCustomizedNotificationData(context));
        // 2. show the new notification generated from NotificationData after updating
        NotificationUtil.generateNotification(context, LOG_TAG);
    }
}

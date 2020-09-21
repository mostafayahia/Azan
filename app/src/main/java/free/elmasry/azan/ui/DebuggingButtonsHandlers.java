package free.elmasry.azan.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import java.util.Calendar;

import free.elmasry.azan.alarm.ScheduledReceiver;
import free.elmasry.azan.utilities.AzanAppTimeUtils;
import free.elmasry.azan.utilities.HelperUtils;
import free.elmasry.azan.utilities.NotificationUtil;

import static free.elmasry.azan.alarm.ScheduleAlarmTask.ACTION_PLAY_AZAN_SOUND;

class DebuggingButtonsHandlers {
    public static void launchAzanSoundActivity(Context context) {
        Intent intent = new Intent(context, PlayAzanSound.class);
        context.startActivity(intent);
    }

    public static void launchEqamahSoundActivity(Context context) {
        Intent intent = new Intent(context, PlayEqamahSound.class);
        context.startActivity(intent);
    }

    public static void scheduledReceiver(Context context) {
        Calendar calendar = Calendar.getInstance();

        final int[] testMinutesVals = {5, 59};
        final int testMinute = (Calendar.getInstance().get(Calendar.MINUTE) >= testMinutesVals[0]) ?
                testMinutesVals[1] : testMinutesVals[0];

        String todayString = AzanAppTimeUtils.convertMillisToDateString(System.currentTimeMillis());
        long timeInMillis = AzanAppTimeUtils.convertDateToMillis(todayString,
                calendar.get(Calendar.HOUR_OF_DAY),
                testMinute
        );

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
            HelperUtils.showToast(context, "scheduling at min: " + testMinute, Toast.LENGTH_LONG);
        }
    }

    public static void launchNotification(Context context, String LOG_TAG) {
        NotificationUtil.generateNotification(context, LOG_TAG);
    }
}

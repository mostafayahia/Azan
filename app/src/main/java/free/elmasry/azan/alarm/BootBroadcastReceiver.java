package free.elmasry.azan.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // for testing
//        ScheduleAlarmTask.scheduleAlarmForStartingAzanSoundActivityAt(context, "1 Apr 2018", 10, 26);
        ScheduleAlarmTask.scheduleTaskForNextAzanTime(context);
    }
}

package free.elmasry.azan.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

public class AzanWidgetService extends IntentService {

    private static final String ACTION_DISPLAY_AZAN_TIME = "free.elmasry.azan.action.display_azan_time";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public AzanWidgetService() {
        super("AzanWidgetService");
    }

    public static void startActionDisplayAzanTime(Context context) {
        Intent intent = new Intent(context, AzanWidgetService.class);
        intent.setAction(ACTION_DISPLAY_AZAN_TIME);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(ACTION_DISPLAY_AZAN_TIME)) {
                handleActionDisplayAzanTime();
            } else {
                throw new RuntimeException("unknown action: " + action);
            }
        }
    }

    private void handleActionDisplayAzanTime() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, AzanAppWidget.class));
        AzanAppWidget.updateAppWidgets(this, appWidgetManager, appWidgetIds);
    }
}

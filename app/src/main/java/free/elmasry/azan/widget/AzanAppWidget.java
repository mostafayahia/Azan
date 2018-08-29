package free.elmasry.azan.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import free.elmasry.azan.ui.MainActivity;
import free.elmasry.azan.R;
import free.elmasry.azan.alarm.ScheduleAlarmTask;
import free.elmasry.azan.shared.AzanTimeIndex;
import free.elmasry.azan.utilities.AzanAppHelperUtils;
import free.elmasry.azan.utilities.AzanAppTimeUtils;
import free.elmasry.azan.utilities.PreferenceUtils;

/**
 * Implementation of App Widget functionality.
 */
public class AzanAppWidget extends AppWidgetProvider {

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String todayDateString = AzanAppTimeUtils.convertMillisToDateString(System.currentTimeMillis());
        String tomorrowDateString = AzanAppTimeUtils.getDayAfterDateString(todayDateString);
        int indexOfCurrentAzanTime = AzanAppHelperUtils.getIndexOfCurrentTime(context);

        int indexOfNextAzanTime;
        String[] allAzanTimesIn24Format;
        if (indexOfCurrentAzanTime == AzanTimeIndex.ALL_TIMES_NUM - 1) {
            indexOfNextAzanTime = 0;
            allAzanTimesIn24Format = PreferenceUtils.getAzanTimesIn24Format(context, tomorrowDateString);
        } else {
            indexOfNextAzanTime = indexOfCurrentAzanTime + 1;
            allAzanTimesIn24Format = PreferenceUtils.getAzanTimesIn24Format(context, todayDateString);
        }


        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.azan_app_widget);
        if (allAzanTimesIn24Format != null) {
            String azanTimeIn24HourFormat = allAzanTimesIn24Format[indexOfNextAzanTime];

            views.setTextViewText(R.id.widget_azan_time_label_text_view,
                    AzanAppHelperUtils.getAzanLabel(context, indexOfNextAzanTime));
            if (context.getResources().getBoolean(R.bool.use_24_hour_format)) {
                views.setTextViewText(R.id.widget_azan_time_text_view,
                        AzanAppTimeUtils.getTimeWithDefaultLocale(azanTimeIn24HourFormat));
            } else {
                views.setTextViewText(R.id.widget_azan_time_text_view,
                        AzanAppTimeUtils.convertTo12HourFormat(azanTimeIn24HourFormat));
            }
            // we schedule alarm task for next time only if everything is okay
            ScheduleAlarmTask.scheduleTaskForNextAzanTime(context);
        } else {
            // in this case no data stored for the given date (today or tomorrow)
            views.setTextViewText(R.id.widget_azan_time_label_text_view,
                    context.getString(R.string.widget_error));
            views.setTextViewText(R.id.widget_azan_time_text_view, "" );
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_main_layout, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAppWidgets(context, appWidgetManager, appWidgetIds);
    }

    public static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager,
                                        int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


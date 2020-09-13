/**
 * this code is taken from this repo: https://github.com/android/user-interface-samples/tree/master/Notifications
 */


package free.elmasry.azan.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.widget.TextView;

import free.elmasry.azan.R;
import free.elmasry.azan.notification.NotificationData;
import free.elmasry.azan.shared.AzanTimeIndex;

/**
 * Simplifies common {@link Notification} tasks.
 */
public class NotificationUtil {

    public static class CustomizedNotificationData {
        public final String channelName;
        public final String channelDescription;
        public final String contentText;

        public CustomizedNotificationData(String channelName, String channelDescription,
                                          String contentText) {
            this.channelName = channelName;
            this.channelDescription = channelDescription;
            this.contentText = contentText;
        }
    }

    public static String createNotificationChannel(
            Context context,
            NotificationData notificationData) {

        // NotificationChannels are required for Notifications on O (API 26) and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // The id of the channel.
            String channelId = notificationData.getChannelId();

            // The user-visible name of the channel.
            CharSequence channelName = notificationData.getChannelName();
            // The user-visible description of the channel.
            String channelDescription = notificationData.getChannelDescription();
            int channelImportance = notificationData.getChannelImportance();
            boolean channelEnableVibrate = notificationData.isChannelEnableVibrate();
            int channelLockscreenVisibility =
                    notificationData.getChannelLockscreenVisibility();

            // Initializes NotificationChannel.
            NotificationChannel notificationChannel =
                    new NotificationChannel(channelId, channelName, channelImportance);
            notificationChannel.setDescription(channelDescription);
            notificationChannel.enableVibration(channelEnableVibrate);
            notificationChannel.setLockscreenVisibility(channelLockscreenVisibility);

            // Adds NotificationChannel to system. Attempting to create an existing notification
            // channel with its original values performs no operation, so it's safe to perform the
            // below sequence.
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            return channelId;
        } else {
            // Returns null for pre-O (26) devices.
            return null;
        }
    }

    /**
     * @param context
     * @return customized notification data (channel name, description, contentText as azan time) or
     * null if no data stored for azan for the current date
     */
    public static CustomizedNotificationData getCustomizedNotificationData(Context context) {
        final String[] allAzanTimesIn24Format = PreferenceUtils.getAzanTimesIn24Format(context,
                AzanAppTimeUtils.convertMillisToDateString(System.currentTimeMillis()));

        if (allAzanTimesIn24Format == null) return null;

        int indexOfNextAzanTime = AzanAppHelperUtils.getIndexOfCurrentTime(context) + 1;

        if (indexOfNextAzanTime >= AzanTimeIndex.ALL_TIMES_NUM)
            indexOfNextAzanTime = AzanTimeIndex.ALL_TIMES_NUM - 1;

        final String azanTimeIn24HourFormat = allAzanTimesIn24Format[indexOfNextAzanTime];

        final boolean timeFormat24 = PreferenceUtils.getTimeFormatFromPreferences(context)
                .equals(context.getString(R.string.pref_time_format_24_hour));

        final String contentText = AzanAppHelperUtils.getAzanLabel(context, indexOfNextAzanTime) +
                " " + (timeFormat24 ? AzanAppTimeUtils.getTimeWithDefaultLocale(azanTimeIn24HourFormat) :
                AzanAppTimeUtils.convertTo12HourFormat(azanTimeIn24HourFormat));


        return new CustomizedNotificationData(context.getString(R.string.notification_channel_name),
                context.getString(R.string.notification_channel_description),
                contentText
        );

    }
}


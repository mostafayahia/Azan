/**
 * this code is taken from this repo: https://github.com/android/user-interface-samples/tree/master/Notifications
 */


package free.elmasry.azan.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import free.elmasry.azan.R;
import free.elmasry.azan.notification.NotificationData;
import free.elmasry.azan.shared.AzanTimeIndex;
import free.elmasry.azan.ui.MainActivity;

/**
 * Simplifies common {@link Notification} tasks.
 */
public class NotificationUtil {

    public static final int NOTIFICATION_ID = 357;

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

    private static String createNotificationChannel(
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
            notificationChannel.setShowBadge(false); // disable notification badges
            notificationChannel.setSound(null, null); // launch notification w no sound

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
    public static CustomizedNotificationData generateCustomizedNotificationData(Context context) {
        String[] allAzanTimesIn24Format = PreferenceUtils.getAzanTimesIn24Format(context,
                AzanAppTimeUtils.convertMillisToDateString(System.currentTimeMillis()));

        if (allAzanTimesIn24Format == null) return null;

        int indexOfNextAzanTime = AzanAppHelperUtils.getIndexOfCurrentTime(context) + 1;

        // if indexOfNextAzanTime refers to AFTER ISHAA time (ALL_TIMES_NUM), we will set to ISHAA time
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

    /*
     * Generates a Notification that supports both phone/tablet and wear.
     * display a basic notification.
     * this code is taken from this repo: https://github.com/android/user-interface-samples/tree/master/Notifications
     */
    public static void generateNotification(Context context, String LOG_TAG) {

        // set CustomizedNotificationData instance in NotificationData class if null
        if (null == NotificationData.getCustomizedNotificationData())
            NotificationData.setCustomizedNotificationData(
                NotificationUtil.generateCustomizedNotificationData(context));


        Log.d(LOG_TAG, "generateBigTextStyleNotification()");

        final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        // Main steps for building a notification:
        //      0. Get your data
        //      1. Create/Retrieve Notification Channel for O and beyond devices (26+)
        //      2. Set up main Intent for notification
        //      3. Build and issue the notification

        // 0. Get your data (everything unique per Notification).
        NotificationData notificationData = NotificationData.getInstance();

        if (notificationData == null) {
            Log.e(LOG_TAG, "Instance of NotificationData/CustomizedNotificationData is NULL");
            return;
        }

        // 1. Create/Retrieve Notification Channel for O and beyond devices (26+).
        String notificationChannelId =
                NotificationUtil.createNotificationChannel(context, notificationData);


        // 2. Set up main Intent for notification.
        Intent notifyIntent = new Intent(context, MainActivity.class);

        // When creating your Intent, you need to take into account the back state, i.e., what
        // happens after your Activity launches and the user presses the back button.

        // There are two options:
        //      1. Regular activity - You're starting an Activity that's part of the application's
        //      normal workflow.

        //      2. Special activity - The user only sees this Activity if it's started from a
        //      notification. In a sense, the Activity extends the notification by providing
        //      information that would be hard to display in the notification itself.

        // we will got in option 1

        // For more information, check out our dev article:
        // https://developer.android.com/training/notify-user/navigation.html

        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );



        // 3. Build and issue the notification.

        // Because we want this to be a new notification (not updating a previous notification), we
        // create a new Builder. Later, we use the same global builder to get back the notification
        // we built here for the snooze action, that is, canceling the notification and relaunching
        // it several seconds later.

        // Notification Channel Id is ignored for Android pre O (26).
        NotificationCompat.Builder notificationCompatBuilder =
                new NotificationCompat.Builder(
                        context, notificationChannelId);

        Notification notification = notificationCompatBuilder
                // Content for API <24 (7.0 and below) devices.
                .setContentTitle(notificationData.getContentText())
                .setSmallIcon(R.drawable.ic_mosque)
                .setLargeIcon(BitmapFactory.decodeResource(
                        context.getResources(),
                        R.drawable.ic_mosque))
                .setContentIntent(notifyPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                // Set primary color (important for Wear 2.0 Notifications).
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))

                // SIDE NOTE: Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
                // devices and all Wear devices. If you have more than one notification and
                // you prefer a different summary notification, set a group key and create a
                // summary notification via
                // .setGroupSummary(true)
                // .setGroup(GROUP_KEY_YOUR_NAME_HERE)

                .setCategory(Notification.CATEGORY_REMINDER)

                // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated for
                // 'importance' which is set in the NotificationChannel. The integers representing
                // 'priority' are different from 'importance', so make sure you don't mix them.
                .setPriority(notificationData.getPriority())

                // Sets lock-screen visibility for 25 and below. For 26 and above, lock screen
                // visibility is set in the NotificationChannel.
                .setVisibility(notificationData.getChannelLockscreenVisibility())

                // prevent notification from being dismissed by the user
                .setOngoing(true)

                .build();

        notificationManagerCompat.notify(NOTIFICATION_ID, notification);
    }

    public static void cancelNotification(Context context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
    }
}


/**
 * this code is taken from this repo: https://github.com/android/user-interface-samples/tree/master/Notifications
 */

package free.elmasry.azan.notification;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

import static free.elmasry.azan.utilities.NotificationUtil.CustomizedNotificationData;

public class NotificationData {

    // Standard notification values:
    private String mContentText;
    private int mPriority;

    // Notification channel values (O and above):
    private String mChannelId;
    private CharSequence mChannelName;
    private String mChannelDescription;
    private int mChannelImportance;
    private boolean mChannelEnableVibrate;
    private int mChannelLockscreenVisibility;

    private static NotificationData sInstance = null;
    private static CustomizedNotificationData sCustomizedNotificationData = null;

    public static NotificationData getInstance() {
        if (sInstance == null) {
            sInstance = getSync();
        }

        return sInstance;
    }

    private static synchronized NotificationData getSync() {
        if (sCustomizedNotificationData == null) {
            return null;
        }

        if (sInstance == null) {
            sInstance = new NotificationData();
        }

        return sInstance;
    }

    public static void setsCustomizedNotificationData(CustomizedNotificationData cnd) {
        sCustomizedNotificationData = cnd;

        // recreate sInstance to update the data inside it
        if (sInstance != null) sInstance = getSync();
    }

    private NotificationData() {

        // Standard Notification values:
        // Content for API <24 (4.0 and below) devices.
        mContentText = sCustomizedNotificationData.contentText;
        mPriority = NotificationCompat.PRIORITY_DEFAULT;

        // Notification channel values (for devices targeting 26 and above):
        mChannelId = "azan_reminder_1";
        // The user-visible name of the channel.
        mChannelName = sCustomizedNotificationData.channelName;
        // The user-visible description of the channel.
        mChannelDescription = sCustomizedNotificationData.channelDescription;
        mChannelImportance = NotificationManager.IMPORTANCE_DEFAULT;
        mChannelEnableVibrate = false;
        mChannelLockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC;
    }

    // Notification Standard notification get methods:

    public String getContentText() {
        return mContentText;
    }

    public int getPriority() {
        return mPriority;
    }

    // Channel values (O and above) get methods:
    public String getChannelId() {
        return mChannelId;
    }

    public CharSequence getChannelName() {
        return mChannelName;
    }

    public String getChannelDescription() {
        return mChannelDescription;
    }

    public int getChannelImportance() {
        return mChannelImportance;
    }

    public boolean isChannelEnableVibrate() {
        return mChannelEnableVibrate;
    }

    public int getChannelLockscreenVisibility() {
        return mChannelLockscreenVisibility;
    }
}

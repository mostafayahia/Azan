package free.elmasry.azan.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import free.elmasry.azan.utilities.HelperUtils;
import free.elmasry.azan.utilities.LocationUtils;
import free.elmasry.azan.utilities.NotificationUtil;

class Dialogues {

    static void showNotificationDisabledErr(final Activity activity, final String title, final String message) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        NotificationUtil.openNotificationSettingsForApp(activity);
                        activity.finish();
                    }
                })
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    static void showLocationErr(final Activity activity, final String title, final String message) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (LocationUtils.isLocationEnabled(activity)) {
                            // we will try to open google map app hopefully this makes the android
                            // stores and caches the device location
                            HelperUtils.openApp(activity, "com.google.android.apps.maps");
                        } else {
                            activity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                        }
                        activity.finish();
                    }
                })
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }
}

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

package free.elmasry.azan.utilities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by yahia on 12/22/17.
 */

public class HelperUtils {

    private static Toast mToast;

    /**
     * show toast message with short duration
     * @param context the base context of the application
     * @param text the message you want to display
     */
    public static void showToast(Context context, String text) {
        showToast(context, text, Toast.LENGTH_SHORT);
    }

    /**
     * show toast message with short duration
     * @param context the base context of the application
     * @param stringResId the resId for the string you want to display
     */
    public static void showToast(Context context, int stringResId) {
        showToast(context, context.getString(stringResId), Toast.LENGTH_SHORT);
    }

    /**
     * show toast message with a specific duration
      * @param context the base context of the application
     * @param stringResId the resId for the string you want to display
     * @param duration can be Toast.LENGTH_SHORT or Toast.LENGTH_LONG
     */
    public static void showToast(Context context, int stringResId, int duration) {
        showToast(context, context.getString(stringResId), duration);
    }

    /**
     * show toast message with a specific duration
     * @param context the base context of the application
     * @param text the message you want to display
     * @param duration can be Toast.LENGTH_SHORT or Toast.LENGTH_LONG
     */
    public static void showToast(Context context, String text, int duration) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, text, duration);
        mToast.show();
    }

    /**
     * check if the device is connected to the internet or not
     * @return true if the device is connected to the internet, false otherwise
     */
    public static boolean isDeviceOnline(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * setting locale regardless the locale of the device
     * @param context context of the application
     * @param languageCode like "en", "ar",........
     */

    public static void setLocaleForApp(Context context, String languageCode) {
        //https://stackoverflow.com/questions/4985805/set-locale-programmatically
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = context.getResources().getConfiguration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }

    /**
     * determine the device is Lollipop version or not
     * @return true of api level of the device is Lollipop
     */
    public static boolean isLollipop() {
        int apiLevel = Build.VERSION.SDK_INT;
        return apiLevel == 21 || apiLevel == 22;
    }


    /**
     * determine if the location is enabled in the system settings of the device or not
     * @param context the base context for the application
     * @return true if the location is enabled in the system settings of the device
     */
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    /** Open another app.
     * @param context current Context, like Activity, App, or Service
     * @param packageName the full package name of the app to open
     * @return true if likely successful, false if unsuccessful
     */
    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
                //throw new ActivityNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    /**
     * toggle the direction of the linear layout (from rtl to ltr and vice versa)
     * @param linearLayout the linearLayout that you want to change its direction
     */
    public static void changeDirectionOfLinearLayout(LinearLayout linearLayout) {
        ArrayList<View> views = new ArrayList<View>();
        for(int x = 0; x < linearLayout.getChildCount(); x++) {
            views.add(linearLayout.getChildAt(x));
        }
        linearLayout.removeAllViews();
        for(int x = views.size() - 1; x >= 0; x--) {
            linearLayout.addView(views.get(x));
        }
    }
}

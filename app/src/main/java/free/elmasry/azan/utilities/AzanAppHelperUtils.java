package free.elmasry.azan.utilities;

import android.content.Context;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import free.elmasry.azan.MainActivity;
import free.elmasry.azan.R;

import static free.elmasry.azan.shared.AzanTimeIndex.*;

public class AzanAppHelperUtils {

    /**
     * get time index of current time, example if we are at asr time, it will return INDEX_ASR
     *
     * @param context the base context of the application
     * @return index of current time or -1 if the current time between midnight and first
     * time
     */
    public static int getIndexOfCurrentTime(Context context) {

        long nowInMillis = System.currentTimeMillis();

        String todayString = AzanAppTimeUtils.convertMillisToDateString(nowInMillis);
        String[] allAzanTimesIn24Format = PreferenceUtils.getAzanTimesIn24Format(context, todayString);

        Timestamp timestamp = new Timestamp(nowInMillis);
        DateFormat formatter = new SimpleDateFormat("HH:mm", new Locale("en"));
        String nowHourAndMinuteIn24HourFormat = formatter.format(timestamp);
        long nowHour = AzanAppTimeUtils.getHourFromTime(nowHourAndMinuteIn24HourFormat);
        long nowMinute = AzanAppTimeUtils.getMinuteFromTime(nowHourAndMinuteIn24HourFormat);
        for (int i = ALL_TIMES_NUM - 1; i >= 0; i--) {
            String azanTime24HourFormat = allAzanTimesIn24Format[i];
            long azanTimeHour = AzanAppTimeUtils.getHourFromTime(azanTime24HourFormat);
            long azanTimeMinute = AzanAppTimeUtils.getMinuteFromTime(azanTime24HourFormat);
            if (nowHour == azanTimeHour) {
                if (nowMinute >= azanTimeMinute) {
                    return i;
                }
            } else if (nowHour > azanTimeHour) {
                return i;
            }
        }
        return -1;
    }

    /**
     * getting the time label according to the index of azan time
     *
     * @param context
     * @param indexOfAzanTime
     * @return the time label according to the index of azan time
     */
    public static String getAzanLabel(Context context, int indexOfAzanTime) {

        int resId;

        switch (indexOfAzanTime) {
            case INDEX_FAJR:
                resId = R.string.label_fajr;
                break;
            case INDEX_SHUROOQ:
                resId = R.string.label_shurooq;
                break;
            case INDEX_DHUHR:
                resId = R.string.label_dhuhr;
                break;
            case INDEX_ASR:
                resId = R.string.label_asr;
                break;
            case INDEX_MAGHRIB:
                resId = R.string.label_maghrib;
                break;
            case INDEX_ISHAA:
                resId = R.string.label_ishaa;
                break;
            default:
                throw new IllegalArgumentException("invalid index of azan time, the given parameter: " + indexOfAzanTime);
        }

        return context.getString(resId);

    }

    /**
     * return true only if the azan time index is valid to play azan sound
     * for ex: if azan time index is INDEX_SHUROOQ the method will return false
     * @param azanTimeIndex
     * @return true only if the azan time index is valid to play azan sound
     */
    public static boolean isValidPlayAzanTimeIndex(int azanTimeIndex) {
        return azanTimeIndex == INDEX_FAJR ||
                azanTimeIndex == INDEX_DHUHR ||
                azanTimeIndex == INDEX_ASR ||
                azanTimeIndex == INDEX_MAGHRIB ||
                azanTimeIndex == INDEX_ISHAA;
    }


}

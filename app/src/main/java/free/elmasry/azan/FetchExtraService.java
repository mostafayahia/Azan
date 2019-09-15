package free.elmasry.azan;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

import free.elmasry.azan.utilities.AzanAppTimeUtils;
import free.elmasry.azan.utilities.FetchDataUtils;
import free.elmasry.azan.utilities.HelperUtils;
import free.elmasry.azan.utilities.LocationUtils;
import free.elmasry.azan.utilities.PreferenceUtils;

public class FetchExtraService extends IntentService {

    private static final String ACTION_FETCH_EXTRA_DATA = "free.elmasry.azan.action.fetch_extra_data";

    /**
     * the number of extra days we fetch from azan api and will add them in shared preferences
     */
    private static final int FETCH_EXTRA_STORE_DAYS = 3;
    private static final String LOG_TAG = FetchExtraService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public FetchExtraService() {
        super("FetchExtraService");
    }


    public static void startActionFetchExtraData(Context context) {
        Intent serviceIntent = new Intent(context, FetchExtraService.class);
        serviceIntent.setAction(ACTION_FETCH_EXTRA_DATA);
        context.startService(serviceIntent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if (action != null) {
            if (action.equals(ACTION_FETCH_EXTRA_DATA)) {
                handleActionFetchExtraData();
            } else {
                throw new RuntimeException("unknown action: " + action);
            }
        }

    }

    private void handleActionFetchExtraData() {
        Context context = this;

        if (!HelperUtils.isDeviceOnline(context))
            return;

        LocationUtils.MyLocation myLocation = PreferenceUtils.getUserLocation(context);

        String lastStoreDateString = PreferenceUtils.getLastStoredDateStringForData(context);

        if (TextUtils.isEmpty(lastStoreDateString)) {
            return; //Nothing to do!
        }

        long startTimeInMillis = AzanAppTimeUtils.convertDateToMillis(
                AzanAppTimeUtils.getDayAfterDateString(lastStoreDateString));

        try {
            String[] jsonResponseArray = FetchDataUtils.getJsonResponseArray(context, myLocation,
                    FETCH_EXTRA_STORE_DAYS, startTimeInMillis);

            if (jsonResponseArray == null) {
                Log.e(LOG_TAG, "jsonResponseArray for Azan Data is null");
                return;
            }

            FetchDataUtils.saveAzanAppDataInPreferences(context, jsonResponseArray,
                    FETCH_EXTRA_STORE_DAYS, startTimeInMillis);

            // update meta-data regarding to last extra fetch
            long nowInMillis = System.currentTimeMillis();
            String nowDateTimeString = AzanAppTimeUtils.convertMillisToDateTimeString(nowInMillis);
            int extraFetchCounter = PreferenceUtils.getFetchExtraCounter(context);
            PreferenceUtils.setFetchExtraCounter(context, ++extraFetchCounter);
            PreferenceUtils.setFetchExtraLastDateTimeString(context, nowDateTimeString);

            Log.d(LOG_TAG, "===== fetch extra data DONE successfully :)) =====");
            Log.d(LOG_TAG, "fetchExtraCounter: " + PreferenceUtils.getFetchExtraCounter(context));
            Log.d(LOG_TAG, "fetchExtraLastDateTimeString: " + PreferenceUtils.getFetchExtraLastDateTimeString(context));
            Log.d(LOG_TAG, "lastStoredDateString: " +
                    PreferenceUtils.getLastStoredDateStringForData(context));

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "error in getting json response from the url");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "can't get data from the json response");
            e.printStackTrace();
        }
    }
}

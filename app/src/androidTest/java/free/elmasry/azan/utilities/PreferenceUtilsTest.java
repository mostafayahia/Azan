package free.elmasry.azan.utilities;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PreferenceUtilsTest {

    private static final String LOG_TAG = PreferenceUtilsTest.class.getSimpleName();

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("free.elmasry.azan", appContext.getPackageName());
    }

    @Test
    public void testGetLastStoredDateStringForData() {
        /**
         * we just print the last date string int which the app storing data
         */
        Log.d(LOG_TAG, "=================testGetLastStoredDateStringForData()===============");
        Context appContext = InstrumentationRegistry.getTargetContext();
        Log.d(LOG_TAG, PreferenceUtils.getLastStoredDateStringForData(appContext));
    }

    @Test
    public void testGetFetchExtraLastDateTimeString() {
        /**
         * we just print the output from calling the method
         */
        Log.d(LOG_TAG, "=================testGetFetchExtraLastDateTimeString()===============");
        Context appContext = InstrumentationRegistry.getTargetContext();
        Log.d(LOG_TAG, PreferenceUtils.getFetchExtraLastDateTimeString(appContext));
    }

}

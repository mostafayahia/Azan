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
        Context appContext = InstrumentationRegistry.getTargetContext();
        Log.d("PreferenceUtilsTest", "========================" +
                PreferenceUtils.getLastStoredDateStringForData(appContext) + "======================");
    }

}

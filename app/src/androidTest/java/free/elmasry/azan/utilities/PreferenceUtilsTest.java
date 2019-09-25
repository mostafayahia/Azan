package free.elmasry.azan.utilities;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PreferenceUtilsTest {

    private static final String LOG_TAG = PreferenceUtilsTest.class.getSimpleName();
    private Context mContext;

    @Before
    public void settingContext() {
        mContext = InstrumentationRegistry.getTargetContext();
    }


    @Test
    public void useAppContext() {
        assertEquals("free.elmasry.azan", mContext.getPackageName());
    }

    @Test
    public void testGetLastStoredDateStringForData() {
        /**
         * we just print the last date string int which the app storing data
         */
        Log.d(LOG_TAG, "=================testGetLastStoredDateStringForData()===============");
        Log.d(LOG_TAG, PreferenceUtils.getLastStoredDateStringForData(mContext));
    }

    @Test
    public void testGetFetchExtraLastDateTimeString() {
        /**
         * we just print the output from calling the method
         */
        Log.d(LOG_TAG, "=================testGetFetchExtraLastDateTimeString()===============");
        Log.d(LOG_TAG, PreferenceUtils.getFetchExtraLastDateTimeString(mContext));
    }

    @Test
    public void testGetFetchExtraCounter() {
        /**
         * we just print the output from calling the method
         */
        Log.d(LOG_TAG, "=================testGetFetchExtraCounter()===============");
        Log.d(LOG_TAG, PreferenceUtils.getFetchExtraCounter(mContext)+"");
    }

    @Ignore
    @Test
    public void copySharedPrefsFileForDebugging() {
        Log.d(LOG_TAG, "=================copySharedPrefsFileForDebugging()===============");
        /*
         * the purpose: for a real device No permission to access the shared preference file and read it
         * so we use code to copy it to storage directory and can read it
         * Note: the files' paths may vary from device to the device also you need to add this permission
         * to your app to copy this file <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
         */

        String prefFilePath = "/data/data/free.elmasry.azan/shared_prefs/free.elmasry.azan_preferences.xml";
        String storageAzanDirPath = "/storage/sdcard0/azan";

        // once directory created, we don't need the next line anymore
        //boolean createdDir = new File(storageAzanDirPath).mkdir();

        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(prefFilePath);
            os = new FileOutputStream(storageAzanDirPath + "/free.elmasry.azan_preferences.xml");
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Log.d(LOG_TAG, "IOException: " + e.getMessage());
        } finally {
            try {
                os.close();
                is.close();
            } catch (IOException e) {
                Log.d(LOG_TAG, "could NOT close opened streams: " + e.getMessage());
            }
        }
    }

}

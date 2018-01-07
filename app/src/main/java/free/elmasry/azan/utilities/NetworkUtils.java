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


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by yahia on 12/22/17.
 */

public class NetworkUtils {

    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    private static final String ALADHAN_BASE_URL = "http://api.aladhan.com/timings";

    // parameter used in http url
    private static final String PARAM_LATITUDE = "latitude";
    private static final String PARAM_LONGITUDE = "longitude";
    private static final String PARAM_METHOD = "method";


    public static final int METHOD_SHIA_ITHNA_ASHARI = 0;
    public static final int METHOD_UNIVERSITY_ISLAMIC_SCIENCE_KARACHI = 1;
    public static final int METHOD_ISLAMIC_SOCIETY_NORTH_AMERICA = 2;
    public static final int METHOD_MUSLIM_WORLD_LEAGUE = 3;
    public static final int METHOD_UMM_AL_QURA = 4;
    public static final int METHOD_EGYPTIAN_GENERAL_AUTHORITY = 5;
    public static final int METHOD_INSTITUTE_OF_GEOPHYSICS_TEHRAN = 7;
    public static final int METHOD_KUWAIT = 9;
    public static final int METHOD_UOIF = 12;



    public static URL buildUrl(long dateInSeconds, double longitude, double latitude, int method) {


        //http://api.aladhan.com/timings/1505035429?latitude=31.2000924&longitude=29.9187387&method=5
        Uri uri = Uri.parse(ALADHAN_BASE_URL).buildUpon()
                .appendPath(Long.toString(dateInSeconds))
                .appendQueryParameter(PARAM_LONGITUDE, Double.toString(longitude))
                .appendQueryParameter(PARAM_LATITUDE, Double.toString(latitude))
                .appendQueryParameter(PARAM_METHOD, method+"")
                .build();

        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Error in getting url from uri: " + uri.toString());
            return null;
        }
    }


    public static String gettingResponseFromHttpUrl(URL url) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            InputStream inputStream = urlConnection.getInputStream();

            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");

            if (scanner.hasNext()) {
                return scanner.next();
            } else {
                return null;
            }

        } finally {
            urlConnection.disconnect();
        }
    }

}

package free.elmasry.azan.utilities;

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by yahia on 1/6/18.
 */

/*
 * IMPORTANT:
 * I GOT THIS CLASS FROM
 * https://stackoverflow.com/questions/16515682/get-the-particular-address-using-latitude-and-longitude/16515848#16515848
 */
public class GetReverseGeoCoding {
    private String mAddress1, mAddress2, mCity, mState, mCountry, mCounty, mPIN;
    private double mLatitude, mLongitude;
    private static final String LOG_TAG = GetReverseGeoCoding.class.getSimpleName();

    public GetReverseGeoCoding(double latitude, double longitude) {
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        init();
    }

    private void init() {
        mAddress1 = "";
        mAddress2 = "";
        mCity = "";
        mState = "";
        mCountry = "";
        mCounty = "";
        mPIN = "";

        try {

            JSONObject jsonObj = getJSONFromURL("http://maps.googleapis.com/maps/api/geocode/json?latlng=" + mLatitude + ","
                    + mLongitude + "&sensor=true");
            String status = jsonObj.getString("status");
            if (status.equalsIgnoreCase("OK")) {
                JSONArray results = jsonObj.getJSONArray("results");
                JSONObject zero = results.getJSONObject(0);
                JSONArray addressComponents = zero.getJSONArray("address_components");

                for (int i = 0; i < addressComponents.length(); i++) {
                    JSONObject zero2 = addressComponents.getJSONObject(i);
                    String longName = zero2.getString("long_name");
                    JSONArray mTypes = zero2.getJSONArray("types");
                    String type = mTypes.getString(0);


                    if (longName != null && !TextUtils.isEmpty(longName)) {
                        if (type.equalsIgnoreCase("street_number")) {
                            mAddress1 = longName + " ";
                        } else if (type.equalsIgnoreCase("route")) {
                            mAddress1 = mAddress1 + longName;
                        } else if (type.equalsIgnoreCase("sublocality")) {
                            mAddress2 = longName;
                        } else if (type.equalsIgnoreCase("locality")) {
                            // mAddress2 = mAddress2 + longName + ", ";
                            mCity = longName;
                        } else if (type.equalsIgnoreCase("administrative_area_level_2")) {
                            mCounty = longName;
                        } else if (type.equalsIgnoreCase("administrative_area_level_1")) {
                            mState = longName;
                        } else if (type.equalsIgnoreCase("country")) {
                            mCountry = longName;
                        } else if (type.equalsIgnoreCase("postal_code")) {
                            mPIN = longName;
                        }
                    }

                    // JSONArray mTypes = zero2.getJSONArray("types");
                    // String type = mTypes.getString(0);
                    // Log.e(LOG_TAG,longName);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private  JSONObject getJSONFromURL(String url) {

        // initialize
        InputStream is = null;
        String result = "";
        JSONObject jObject = null;

        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in http connection " + e.toString());
        }

        // convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObject = new JSONObject(result);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing data " + e.toString());
        }

        return jObject;
    }

    public String getAddress1() {
        return mAddress1;

    }

    public String getAddress2() {
        return mAddress2;

    }

    public String getCity() {
        return mCity;

    }

    public String getState() {
        return mState;

    }

    public String getCountry() {
        return mCountry;

    }

    public String getCounty() {
        return mCounty;

    }

    public String getPIN() {
        return mPIN;

    }


}

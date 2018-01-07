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
    private String mAddress1 = "", mAddress2 = "", mCity = "", mState = "", mCountry = "", mCounty = "", mPIN = "";
    private double mLatitude, mLongitude;
    private static final String LOG_TAG = GetReverseGeoCoding.class.getSimpleName();

    public GetReverseGeoCoding(double latitude, double longitude) {
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        init();
    }

    public void init() {
        mAddress1 = "";
        mAddress2 = "";
        mCity = "";
        mState = "";
        mCountry = "";
        mCounty = "";
        mPIN = "";

        try {

            JSONObject jsonObj = new JsonParser().getJSONFromURL("http://maps.googleapis.com/maps/api/geocode/json?latlng=" + mLatitude + ","
                    + mLongitude + "&sensor=true");
            String Status = jsonObj.getString("status");
            if (Status.equalsIgnoreCase("OK")) {
                JSONArray Results = jsonObj.getJSONArray("results");
                JSONObject zero = Results.getJSONObject(0);
                JSONArray address_components = zero.getJSONArray("address_components");

                for (int i = 0; i < address_components.length(); i++) {
                    JSONObject zero2 = address_components.getJSONObject(i);
                    String long_name = zero2.getString("long_name");
                    JSONArray mtypes = zero2.getJSONArray("types");
                    String Type = mtypes.getString(0);


                    if (TextUtils.isEmpty(long_name) == false || !long_name.equals(null) || long_name.length() > 0 || long_name != "") {
                        if (Type.equalsIgnoreCase("street_number")) {
                            mAddress1 = long_name + " ";
                        } else if (Type.equalsIgnoreCase("route")) {
                            mAddress1 = mAddress1 + long_name;
                        } else if (Type.equalsIgnoreCase("sublocality")) {
                            mAddress2 = long_name;
                        } else if (Type.equalsIgnoreCase("locality")) {
                            // mAddress2 = mAddress2 + long_name + ", ";
                            mCity = long_name;
                        } else if (Type.equalsIgnoreCase("administrative_area_level_2")) {
                            mCounty = long_name;
                        } else if (Type.equalsIgnoreCase("administrative_area_level_1")) {
                            mState = long_name;
                        } else if (Type.equalsIgnoreCase("country")) {
                            mCountry = long_name;
                        } else if (Type.equalsIgnoreCase("postal_code")) {
                            mPIN = long_name;
                        }
                    }

                    // JSONArray mtypes = zero2.getJSONArray("types");
                    // String Type = mtypes.getString(0);
                    // Log.e(Type,long_name);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

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

    private class JsonParser {
        public  JSONObject getJSONFromURL(String url) {

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
                Log.e("log_tag", "Error in http connection " + e.toString());
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
                Log.e("log_tag", "Error converting result " + e.toString());
            }

            // try parse the string to a JSON object
            try {
                jObject = new JSONObject(result);
            } catch (JSONException e) {
                Log.e("log_tag", "Error parsing data " + e.toString());
            }

            return jObject;
        }

    }
}

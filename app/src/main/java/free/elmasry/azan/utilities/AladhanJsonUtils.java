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

import org.json.JSONException;
import org.json.JSONObject;

import static free.elmasry.azan.shared.AzanTimeIndex.*;

/**
 * Created by yahia on 12/22/17.
 */

public class AladhanJsonUtils {

    private static final String ALADHAN_DATA = "data";
    private static final String ALADHAN_TIMINGS = "timings";

    private static final String ALADHAN_DATE = "date";
    private static final String ALADHAN_READABLE = "readable";

    private static final String ALADHAN_FAJR = "Fajr";
    private static final String ALADHAN_SHUROOQ = "Sunrise";
    private static final String ALADHAN_DHUHR = "Dhuhr";
    private static final String ALADHAN_ASR = "Asr";
    private static final String ALADHAN_MAGHRIB = "Maghrib";
    private static final String ALADHAN_ISHAA = "Isha";





    public static String getSimpleAzanDataFromJson(String aladhanJsonResponse) throws JSONException {

        JSONObject jsonDataObject = getAladhanJsonDataObject(aladhanJsonResponse);

        JSONObject jsonTimings = getAladhanJsonTimingsObject(jsonDataObject);

        return "Date: " + jsonDataObject.getJSONObject(ALADHAN_DATE).getString(ALADHAN_READABLE) + "\n" +
                "Fajr: " + jsonTimings.getString(ALADHAN_FAJR) + " - " +
                "Shurooq: " + jsonTimings.getString(ALADHAN_SHUROOQ) + " - " +
                "Dhuhr: " + jsonTimings.getString(ALADHAN_DHUHR) + " - " +
                "Asr: " + jsonTimings.getString(ALADHAN_ASR) + " - " +
                "Maghrib: " + jsonTimings.getString(ALADHAN_MAGHRIB) + " - " +
                "Ishaa: " + jsonTimings.getString(ALADHAN_ISHAA);

    }

    public static String[] getAllAzanTimesIn24Format(String aladhanJsonResponse) throws JSONException {


        JSONObject jsonDataObject = getAladhanJsonDataObject(aladhanJsonResponse);

        JSONObject jsonTimings = getAladhanJsonTimingsObject(jsonDataObject);

        String[] allAzanTimes = new String[ALL_TIMES_NUM];
        allAzanTimes[INDEX_FAJR] = jsonTimings.getString(ALADHAN_FAJR);
        allAzanTimes[INDEX_SHUROOQ] = jsonTimings.getString(ALADHAN_SHUROOQ);
        allAzanTimes[INDEX_DHUHR] = jsonTimings.getString(ALADHAN_DHUHR);
        allAzanTimes[INDEX_ASR] = jsonTimings.getString(ALADHAN_ASR);
        allAzanTimes[INDEX_MAGHRIB] = jsonTimings.getString(ALADHAN_MAGHRIB);
        allAzanTimes[INDEX_ISHAA] = jsonTimings.getString(ALADHAN_ISHAA);

        return allAzanTimes;
    }

    private static JSONObject getAladhanJsonDataObject(String aladhanJsonResponse) throws JSONException {

        JSONObject jsonRootObject = new JSONObject(aladhanJsonResponse);

        return jsonRootObject.getJSONObject(ALADHAN_DATA);
    }

    private static JSONObject getAladhanJsonTimingsObject(JSONObject aladhanJsonDataObject) throws JSONException {
        return aladhanJsonDataObject.getJSONObject(ALADHAN_TIMINGS);
    }

    public static String getReadableDateFromJsonResponse(String aladhanJsonResponse) throws JSONException {

        JSONObject jsonDataObject = getAladhanJsonDataObject(aladhanJsonResponse);

        return jsonDataObject.getJSONObject(ALADHAN_DATE).getString(ALADHAN_READABLE);
    }
}

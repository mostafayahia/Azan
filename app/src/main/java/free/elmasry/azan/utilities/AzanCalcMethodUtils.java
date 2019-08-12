package free.elmasry.azan.utilities;

import android.content.Context;

import free.elmasry.azan.R;

public class AzanCalcMethodUtils {

    /**
     * getting azan calculation method int that used in aladhan api, all integers stored
     * in NetworkUtils like NetworkUtils.METHOD_EGYPTIAN_GENERAL_AUTHORITY
     * @param methodString the calculation method that stored in the preference file
     * @return azan calculation method int corresponding to a given method calculation
     */
    public static int getAzanCalcMethodInt(Context context, String methodString) {

        if (methodString.equals(context.getString(R.string.pref_calc_method_egyptian_general_authority)))
            return NetworkUtils.METHOD_EGYPTIAN_GENERAL_AUTHORITY;

        if (methodString.equals(context.getString(R.string.pref_calc_method_umm_al_qura)))
            return NetworkUtils.METHOD_UMM_AL_QURA;

        if (methodString.equals(context.getString(R.string.pref_calc_method_mwl)))
            return NetworkUtils.METHOD_MUSLIM_WORLD_LEAGUE;

        if (methodString.equals(context.getString(R.string.pref_calc_method_kuwait)))
            return NetworkUtils.METHOD_KUWAIT;

        if (methodString.equals(context.getString(R.string.pref_calc_method_islamic_university_karachi)))
            return NetworkUtils.METHOD_UNIVERSITY_ISLAMIC_SCIENCE_KARACHI;

        if (methodString.equals(context.getString(R.string.pref_calc_method_isna)))
            return NetworkUtils.METHOD_ISLAMIC_SOCIETY_NORTH_AMERICA;

        throw new RuntimeException("unknown azan calculation method: " + methodString);

    }

    /**
     * in the first time the user installs this app, we do our best to get the correct azan
     * calculation method for his country
     * @param longitude
     * @param latitude
     * @return the guessed Azan calculation method for the user
     */
    public static String getDefaultCalcMethod(Context context, double longitude, double latitude) {

        String countryName = new ReverseGeoCoding(latitude, longitude).getCountry()
                .trim().toLowerCase();

        final String[] COUNTRIES_UMM_AL_QURA =
                {"Saudi Arabia", "Yemen", "Jordan", "United Arab Emirates", "Qatar", "Bahrain"};

        //Log.d(LOG_TAG, "country: " + countryName);
        final String COUNTRY_KUWAIT = "Kuwait";

        for (String c : COUNTRIES_UMM_AL_QURA)
            if (c.toLowerCase().equals(countryName))
                return context.getString(R.string.pref_calc_method_umm_al_qura);

        if (countryName.equals(COUNTRY_KUWAIT.toLowerCase()))
            return context.getString(R.string.pref_calc_method_kuwait);

        return context.getString(R.string.pref_calc_method_egyptian_general_authority);
    }
}

package com.example.litrato.activities.tools;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This Class is used to save and load preferences on the phone.
 * It also store the default values when the app is first installed.
 *
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since   2020-20-04
 */
public class PreferenceManager {

    /**
     * This value is the key string to save our parameters.
     * It's similar to a profile.
     */
    private static final String PREFERENCE_NAME = "UserInfo";

    private static final boolean DEFAULT_DARK_MODE = true;
    private static final int DEFAULT_IMPORTED_BMP_SIZE = 1000;
    private static final int DEFAULT_MINIATURE_BMP_SIZE = 100;
    private static final boolean DEFAULT_SAVE_ORIGINAL_RESOLUTION = true;
    private static final boolean DEFAULT_OPEN_HISTOGRAM_BY_DEFAULT = false;

    /**
     * Get the boolean value stored for that preference.
     * @param context the context is necessary to retrieve the parameter.
     * @param pref which preference to get
     * @return the boolean value stored for that preference, or its default value.
     */
    public static boolean getBoolean(Context context, Preference pref) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, 0);

        switch (pref) {
            case DARK_MODE: return sp.getBoolean(pref.toString(), DEFAULT_DARK_MODE);
            case SAVE_ORIGINAL_RESOLUTION: return sp.getBoolean(pref.toString(), DEFAULT_SAVE_ORIGINAL_RESOLUTION);
            case OPEN_HISTOGRAM_BY_DEFAULT: return sp.getBoolean(pref.toString(), DEFAULT_OPEN_HISTOGRAM_BY_DEFAULT);
            default: return false;
        }
    }

    /**
     * Get the integer value stored for that preference.
     * @param context the context is necessary to retrieve the parameter.
     * @param pref which preference to get.
     * @return the integer value stored for that preference, or its default value.
     */
    public static int getInt(Context context, Preference pref) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, 0);

        switch (pref) {
            case IMPORTED_BMP_SIZE: return sp.getInt(pref.toString(), DEFAULT_IMPORTED_BMP_SIZE);
            case MINIATURE_BMP_SIZE: return sp.getInt(pref.toString(), DEFAULT_MINIATURE_BMP_SIZE);
            default: return -1;
        }
    }

    /**
     * Set a boolean value for that preference.
     * @param context the context is necessary to save the parameter.
     * @param pref which preference to save.
     * @param value the boolean value to be stored for that preference.
     */
    public static void setBoolean(Context context, Preference pref, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(pref.toString(), value);
        editor.apply();
    }

    /**
     * Set a integer value for that preference.
     * @param context the context is necessary to save the parameter.
     * @param pref which preference to save.
     * @param value the integer value to be stored for that preference.
     */
    public static void setInt(Context context, Preference pref, int value) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(pref.toString(), value);
        editor.apply();
    }

}

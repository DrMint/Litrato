package com.example.litrato.activities.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREFERENCE_NAME = "UserInfo";

    private static final boolean DEFAULT_DARK_MODE = true;
    private static final int DEFAULT_IMPORTED_BMP_SIZE = 1000;
    private static final int DEFAULT_MINIATURE_BMP_SIZE = 100;
    private static final boolean DEFAULT_SAVE_ORIGINAL_RESOLUTION = true;

    public static boolean getBoolean(Context context, Preference pref) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, 0);

        switch (pref) {
            case DARK_MODE: return sp.getBoolean(pref.toString(), DEFAULT_DARK_MODE);
            case SAVE_ORIGINAL_RESOLUTION: return sp.getBoolean(pref.toString(), DEFAULT_SAVE_ORIGINAL_RESOLUTION);
            default: return false;
        }
    }

    public static int getInt(Context context, Preference pref) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, 0);

        switch (pref) {
            case IMPORTED_BMP_SIZE: return sp.getInt(pref.toString(), DEFAULT_IMPORTED_BMP_SIZE);
            case MINIATURE_BMP_SIZE: return sp.getInt(pref.toString(), DEFAULT_MINIATURE_BMP_SIZE);
            default: return -1;
        }
    }

    public static void setBoolean(Context context, Preference pref, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(pref.toString(), value);
        editor.apply();
    }

    public static void setInt(Context context, Preference pref, int value) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(pref.toString(), value);
        editor.apply();
    }

}

package com.example.gio.camerapermissionshare;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Gio on 11/8/2016.
 */

public class SavePreferences {
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;


    public static void initialize(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
    }

    public static void saveString(String key, String s) {
        editor.putString(key,s);
        commit();
    }
    public static String getString(String key){
           return sharedPreferences.getString(key,"");
    }

    private static void commit() {
        editor.commit();
    }
}

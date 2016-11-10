package com.example.gio.camerapermissionshare;

import android.os.Build;

/**
 * Created by Gio on 11/8/2016.
 */

public class Tools {
    public static int getAndroidVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static boolean atLeastMarshmallow() {
        return getAndroidVersion() >= Build.VERSION_CODES.M;
    }

    public static boolean atLeastKitKat() {
        return getAndroidVersion() >= Build.VERSION_CODES.KITKAT;
    }
}

package com.magicianguo.libshizuku;

import android.app.Application;

public class ShizukuUtils {
    private static Application sApp;
    public static void init(Application application) {
        sApp = application;
    }
    public static Application getApp() {
        return sApp;
    }
}

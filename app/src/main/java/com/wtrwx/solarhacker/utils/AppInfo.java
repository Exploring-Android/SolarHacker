package com.wtrwx.solarhacker.utils;

public class AppInfo {
    private static volatile AppInfo mInstance = null;

    private AppInfo() {

    }

    public static AppInfo getInstance() {
        if (mInstance == null) {
            synchronized (AppInfo.class) {
                if (mInstance == null) {
                    mInstance = new AppInfo();
                }
            }
        }
        return mInstance;
    }

    public boolean isXposedActive() {
        return false;
    }

}

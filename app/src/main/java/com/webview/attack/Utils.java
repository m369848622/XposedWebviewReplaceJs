package com.webview.attack;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

import java.io.IOException;
import java.io.InputStream;

public class Utils {

    public static Application getApplication() {
        return AndroidAppHelper.currentApplication();
    }


    public static Context getModuleContext() {
        try {
            return getApplication().createPackageContext("com.webview.attack", Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream getReplace() {
        try {
            return  getModuleContext().getAssets().open("index.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

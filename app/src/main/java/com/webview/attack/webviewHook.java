package com.webview.attack;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import androidx.annotation.Nullable;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class webviewHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpp) throws Throwable {
        String packageName = lpp.packageName;
        Log.i("info", "webview js load->" + packageName);


        if (packageName.equals("com.android.webview")) {
            return;
        } // 不 hook WebView 本身

        Class clazz = XposedHelpers.findClass("android.webkit.WebView", lpp.classLoader);
        if (clazz != null) {
            XposedBridge.hookAllMethods(clazz, "loadUrl", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                    Log.i("info", "webview load url->" + String.valueOf(param.args[0]));

                    super.beforeHookedMethod(param);

                }
            });

            XposedBridge.hookAllConstructors(clazz, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedHelpers.findAndHookMethod("android.webkit.WebView", lpp.classLoader, "setWebViewClient", WebViewClient.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            param.args[0] = new MyWebViewClient();
                        }
                    });
                    XposedHelpers.callStaticMethod(clazz, "setWebContentsDebuggingEnabled", true);
//                    Object webSettings = XposedHelpers.callMethod(param.thisObject, "getSettings");
//                    XposedHelpers.callMethod(webSettings, "setJavaScriptEnabled", true);
                    XposedBridge.log("WebViewHook new WebView(): " + packageName);
                    super.beforeHookedMethod(param);
                }
            });

        }
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            Log.i("info", "webview load url->" + url);
            return true;
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Log.i("info", "webview get url->" + url);
            if (url.contains("https://lifepay.11185.cn/wx/#/?type=app")) {//加载指定.js时 引导服务端加载本地Assets
                try {
                    Log.i("info", "webview replace url success->" + url);
                    return new WebResourceResponse("text/html", "utf-8", Utils.getReplace());
                } catch (Exception e) {
                    Log.i("info", "webview replace url error->" + url);
                    e.printStackTrace();
                }
            }
            return super.shouldInterceptRequest(view, url);
        }

        //        @Override
//        public void onPageFinished(WebView view, String url) {
//            // Execute your javascript below
//            String jsLine = initJS();
//            XposedBridge.log("jsLine->>"+jsLine);
//            view.loadUrl("javascript:"+jsLine);
//        }
    }

//    public String getFromAssets(String fileName) {
//        try {
//            InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open(fileName));
//            BufferedReader bufReader = new BufferedReader(inputReader);
//            String line = "";
//            String Result = "";
//            while ((line = bufReader.readLine()) != null)
//                Result += line;
//            return Result;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
}
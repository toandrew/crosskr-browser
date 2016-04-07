package com.crosskr.flint.webview.browser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

import android.support.multidex.MultiDexApplication;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import com.connectsdk.discovery.DiscoveryManager;

import dalvik.system.DexClassLoader;

public class BrowserApp extends MultiDexApplication {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        //dexTool();

        context = getApplicationContext();

        DiscoveryManager.init(getApplicationContext());
    }

    public static Context getAppContext() {
        return context;
    }

    private String appname;
    private String videoId;
    private String videoName;
    private String type;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getType() {
        return videoId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    /**
     * Copy the following code and call dexTool() after super.onCreate() in
     * Application.onCreate()
     * <p>
     * This method hacks the default PathClassLoader and load the secondary dex
     * file as it's parent.
     */
    private void dexTool() {

        File dexDir = new File(getFilesDir(), "dlibs");
        dexDir.mkdir();
        File dexFile = new File(dexDir, "libs.apk");
        File dexOpt = new File(dexDir, "opt");
        dexOpt.mkdir();
        try {
            InputStream ins = getAssets().open("libs.apk");
            if (dexFile.length() != ins.available()) {
                FileOutputStream fos = new FileOutputStream(dexFile);
                byte[] buf = new byte[4096];
                int l;
                while ((l = ins.read(buf)) != -1) {
                    fos.write(buf, 0, l);
                }
                fos.close();
            }
            ins.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ClassLoader cl = getClassLoader();
        ApplicationInfo ai = getApplicationInfo();
        String nativeLibraryDir = null;
        if (Build.VERSION.SDK_INT > 8) {
            nativeLibraryDir = ai.nativeLibraryDir;
        } else {
            nativeLibraryDir = "/data/data/" + ai.packageName + "/lib/";
        }
        DexClassLoader dcl = new DexClassLoader(dexFile.getAbsolutePath(),
                dexOpt.getAbsolutePath(), nativeLibraryDir, cl.getParent());

        try {
            Field f = ClassLoader.class.getDeclaredField("parent");
            f.setAccessible(true);
            f.set(cl, dcl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

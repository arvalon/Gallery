package ru.arvalon.gallery.system;

import android.util.Log;

import com.facebook.stetho.Stetho;
import com.squareup.picasso.Picasso;

import static ru.arvalon.gallery.HostActivity.TAG;

public class DebugApp extends App {
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG,getClass().getSimpleName()+" init Stetho");

        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build());
    }
}

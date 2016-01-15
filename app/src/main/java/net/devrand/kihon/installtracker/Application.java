package net.devrand.kihon.installtracker;

/**
 * Created by tstratto on 12/16/2015.
 */
public class Application extends android.app.Application {
    public void onCreate() {
        super.onCreate();
        com.facebook.stetho.Stetho.initializeWithDefaults(this);
    }
}

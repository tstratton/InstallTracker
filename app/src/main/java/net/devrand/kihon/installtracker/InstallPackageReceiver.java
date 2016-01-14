package net.devrand.kihon.installtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import okio.BufferedSink;
import okio.Okio;

/**
 * Created by tstratto on 1/13/2016.
 */
public class InstallPackageReceiver extends BroadcastReceiver {

    private final static String TAG = "InstallPackageReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        //printBundle(intent.getExtras());

        final String action = intent.getAction();
        final String dataString = intent.getDataString();
        String message = null;
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            message = String.format(" Package '%s' added (%s)\n", dataString,
                    intent.getBooleanExtra(Intent.EXTRA_REPLACING, false) ? "replace" : "new");
        } else if (Intent.ACTION_PACKAGE_RESTARTED.equals(action)) {
            message = String.format(" Package '%s' restarted\n", dataString);
        } else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
            message = String.format(" Package '%s' changed\n", dataString);
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            message = String.format(" Package '%s' replaced\n", dataString);
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            message = String.format(" Package '%s' removed (%s)\n", dataString,
                    intent.getBooleanExtra(Intent.EXTRA_REPLACING, false) ? "replace" : "final");
        } else if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(action)) {
            message = String.format(" Package '%s' fully removed\n", dataString);
        } else if (Intent.ACTION_PACKAGE_NEEDS_VERIFICATION.equals(action)) {
            message = String.format(" Package '%s' needs verified\n", dataString);
        } else if (Intent.ACTION_PACKAGE_VERIFIED.equals(action)) {
            message = String.format(" Package '%s' verified\n", dataString);
        } else if (Intent.ACTION_PACKAGE_FIRST_LAUNCH.equals(action)) {
            // i don't think this gets called about other packages.
            message = String.format(" Package '%s' first launch\n", dataString);
        }
        if (message == null) {
            return;
        }
        Log.d(TAG, message);

        BufferedSink sink = null;
        try {
            if (sink == null) {
                sink = Okio.buffer(Okio.appendingSink(new File(MainActivity.FILENAME)));
            }
            sink.writeUtf8(MainActivity.getLine(message));
            sink.flush();
            sink.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void printBundle(Bundle bundle) {
        int count = 0;
        if (bundle == null || bundle.keySet() == null) {
            Log.v(TAG, "bundle is empty!");
            return;
        }
        for (String key : bundle.keySet()) {
            Object obj = bundle.get(key);
            if (obj instanceof String) {
                Log.v(TAG, count + ": key '" + key + "'" + " string value='" + obj + "'");
            } else if (obj instanceof Boolean) {
                Log.v(TAG, count + ": key '" + key + "'" + " bool value='" + obj + "'");
            } else if (obj instanceof Byte) {
                Log.v(TAG, count + ": key '" + key + "'" + " byte value='" + obj + "'");
            } else if (obj instanceof Integer) {
                Log.v(TAG, count + ": key '" + key + "'" + " int value='" + obj + "'");
            } else {
                Class cls = obj.getClass();
                Log.v(TAG, count + ": key '" + key + "'" + cls.getName());
            }
            count++;
        }
    }
}

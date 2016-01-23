package net.devrand.kihon.installtracker;

import android.content.Intent;

/**
 * Created by tstratto on 1/15/2016.
 */
public enum UpdateType {
    ADDED(Intent.ACTION_PACKAGE_ADDED),
    RESTART(Intent.ACTION_PACKAGE_RESTARTED),
    CHANGED(Intent.ACTION_PACKAGE_CHANGED),
    REPLACED(Intent.ACTION_PACKAGE_REPLACED),
    REMOVED(Intent.ACTION_PACKAGE_REMOVED),
    FULLY_REMOVED(Intent.ACTION_PACKAGE_FULLY_REMOVED),
    NEEDS_VERIFY(Intent.ACTION_PACKAGE_NEEDS_VERIFICATION),
    VERIFIED(Intent.ACTION_PACKAGE_VERIFIED),
    FIRST_LAUNCH(Intent.ACTION_PACKAGE_FIRST_LAUNCH),
    FIRST_ADDED(Intent.ACTION_PACKAGE_ADDED + "_FIRST");

    private String name;

    private UpdateType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
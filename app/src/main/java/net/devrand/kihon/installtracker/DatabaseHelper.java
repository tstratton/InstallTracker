package net.devrand.kihon.installtracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by tstratto on 1/14/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "installData.db";
    private static final int SCHEMA_VERSION = 1;
    static final String ID = "_id";
    static final String PACKAGE_NAME = "package";
    static final String TYPE = "type";
    static final String TIMESTAMP = "timestamp";
    static final String TABLE_NAME = "package_updates";
    static final String RECENT_TABLE_NAME = "latest_update";

    private static DatabaseHelper singleton = null;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME,  null, SCHEMA_VERSION);
    }

    synchronized static DatabaseHelper getInstance(Context context) {
        if (singleton ==  null) {
            singleton = new DatabaseHelper(context.getApplicationContext());
        }
        return(singleton);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(String.format("CREATE TABLE '%s' ('%s' TEXT PRIMARY KEY, '%s' TEXT, '%s' DATETIME DEFAULT CURRENT_TIMESTAMP);",
                RECENT_TABLE_NAME, PACKAGE_NAME, TYPE, TIMESTAMP, PACKAGE_NAME, TYPE));
        sqLiteDatabase.execSQL(String.format("CREATE TABLE '%s' ('%s' TEXT, '%s' TEXT, '%s' DATETIME DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY ('%s', '%s'));",
                TABLE_NAME, PACKAGE_NAME, TYPE, TIMESTAMP, PACKAGE_NAME, TYPE));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        throw new RuntimeException("unsupported onUpgrade()");
    }
}

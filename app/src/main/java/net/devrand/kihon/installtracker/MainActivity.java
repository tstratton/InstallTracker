package net.devrand.kihon.installtracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import net.devrand.kihon.installtracker.event.ViewPackageEvent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Bind(R.id.text)
    TextView text;
    @Bind(R.id.read_button)
    Button readButton;
    @Bind(R.id.write_button)
    Button writeButton;
    @Bind(R.id.export_button)
    Button exportButton;
    @Bind(R.id.event_list_fragment)
    FrameLayout fragmentContainer;

    BufferedSink sink = null;

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    static final String FILENAME = "/sdcard/installLog.txt";

    static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Fragment fragment = new EventListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(fragmentContainer.getId(), fragment).commit();

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    CharSequence prefix = text.getCurrentTextColor() == Color.RED ? "" : text.getText();
                    text.setText(prefix + "Appended to the log\n");
                    text.setTextColor(Color.BLUE);

                    sink = Okio.buffer(Okio.appendingSink(new File(FILENAME)));
                    sink.writeUtf8(getLine("something happened"));

                    sink.close();
                    sink = null;
                } catch (IOException ex) {
                    text.setText("Write Error: " + ex.toString());
                    text.setTextColor(Color.RED);
                    ex.printStackTrace();
                    try {
                        if (sink != null) {
                            sink.close();
                            sink = null;
                        }
                    } catch (IOException cex) {
                    }
                }
            }
        });

        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    text.setTextColor(Color.BLACK);
                    BufferedSource source = Okio.buffer(Okio.source(new File(FILENAME)));
                    text.setText(source.readUtf8());
                    source.close();
                } catch (IOException ex) {
                    text.setText("Read Error: " + ex.toString());
                    text.setTextColor(Color.RED);
                    ex.printStackTrace();
                }
            }
        });

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ExportTask(exportButton).execute();
            }
        });
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_export:
                new ExportTask(exportButton).execute();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static final String jsonFileName = "/sdcard/installTracker-export.json";

    static class ExportTask extends AsyncTask<Void, Void, Boolean> {
        Button exportButton;
        Context context;

        ExportTask(Button button) {
            exportButton = button;
            context = button.getContext();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean success = false;
            long startTime = System.currentTimeMillis();
            try {
                BufferedSink sink = null;

                ExportItem exportItem = new ExportItem();
                Cursor result;

                exportItem.info.timestamp = MainActivity.sdf.format(new Date());
                exportItem.info.androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                exportItem.info.device = android.os.Build.DEVICE;
                exportItem.info.model = android.os.Build.MODEL;

                SQLiteOpenHelper db = DatabaseHelper.getInstance(context);
                result = db
                        .getReadableDatabase()
                        .query(DatabaseHelper.RECENT_TABLE_NAME,
                                new String[]{"ROWID AS _id",
                                        DatabaseHelper.PACKAGE_NAME,
                                        DatabaseHelper.TYPE,
                                        DatabaseHelper.TIMESTAMP},
                                null, null, null, null, DatabaseHelper.TIMESTAMP + " DESC");
                exportItem.newest = new ArrayList<>(result.getCount());

                while (result.moveToNext()) {
                    EventItem eventItem = new EventItem(result.getString(result.getColumnIndex(DatabaseHelper.PACKAGE_NAME)),
                            result.getString(result.getColumnIndex(DatabaseHelper.TYPE)),
                            result.getString(result.getColumnIndex(DatabaseHelper.TIMESTAMP)));
                    exportItem.newest.add(eventItem);
                }
                result.close();

                result = db
                        .getReadableDatabase()
                        .query(DatabaseHelper.TABLE_NAME,
                                new String[]{"ROWID AS _id",
                                        DatabaseHelper.PACKAGE_NAME,
                                        DatabaseHelper.TYPE,
                                        DatabaseHelper.TIMESTAMP},
                                null, null, null, null, DatabaseHelper.TIMESTAMP + " DESC");
                exportItem.events = new ArrayList<>(result.getCount());

                while (result.moveToNext()) {
                    EventItem eventItem = new EventItem(result.getString(result.getColumnIndex(DatabaseHelper.PACKAGE_NAME)),
                            result.getString(result.getColumnIndex(DatabaseHelper.TYPE)),
                            result.getString(result.getColumnIndex(DatabaseHelper.TIMESTAMP)));
                    exportItem.events.add(eventItem);
                }
                result.close();

                Gson gson = new Gson();
                if (sink == null) {
                    sink = Okio.buffer(Okio.sink(new File(jsonFileName)));
                }
                sink.writeUtf8(gson.toJson(exportItem));
                sink.flush();
                sink.close();
                success = true;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            long endTime = System.currentTimeMillis();
            Log.d(TAG, "Export took " + (endTime - startTime) + " ms");
            return success;
        }

        protected void onPreExecute() {
            exportButton.setText("Exporting ...");
            exportButton.setEnabled(false);
        }

        protected void onPostExecute(Boolean success) {
            exportButton.setEnabled(true);
            exportButton.setText("Export");
            if (success) {
                Toast.makeText(exportButton.getContext(), "json file written", Toast.LENGTH_SHORT).show();
            }
        }
    }

    static String getLine(String string) {
        return String.format("%s == %s\n", sdf.format(new Date(System.currentTimeMillis())), string);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onEvent(ViewPackageEvent event) {
        String packageName = event.packageName;
        Fragment fragment = new PackageEventFragment(packageName);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(fragmentContainer.getId(), fragment)
                .addToBackStack(null)
                .commit();
    }
}

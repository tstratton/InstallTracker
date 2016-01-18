package net.devrand.kihon.installtracker;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.text) TextView text;
    @Bind(R.id.read_button) Button readButton;
    @Bind(R.id.write_button) Button writeButton;
    @Bind(R.id.list) ListView listView;

    InstallPackageReceiver receiver;
    BufferedSink sink = null;

    Cursor result;

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    static final String FILENAME = "/sdcard/installLog.txt";

    static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    CharSequence prefix = text.getCurrentTextColor() == Color.RED ? "" : text.getText() ;
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

        SQLiteOpenHelper db = DatabaseHelper.getInstance(this);
        result = db
                .getReadableDatabase()
                .query(DatabaseHelper.RECENT_TABLE_NAME,
                        new String[]{"ROWID AS _id",
                                DatabaseHelper.PACKAGE_NAME,
                                DatabaseHelper.TYPE,
                                DatabaseHelper.TIMESTAMP},
                        null, null, null, null, DatabaseHelper.TIMESTAMP + " DESC");
        result.getCount();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_item,
                result, new String[]{
                DatabaseHelper.PACKAGE_NAME,
                DatabaseHelper.TYPE,
                DatabaseHelper.TIMESTAMP},
                new int[]{R.id.package_name, R.id.event_name, R.id.timestamp},
                0);
        listView.setAdapter(adapter);
    }

    static String getLine(String string) {
        return String.format("%s == %s\n", sdf.format(new Date(System.currentTimeMillis())), string);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        result.close();
    }
}

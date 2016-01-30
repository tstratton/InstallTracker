package net.devrand.kihon.installtracker;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.devrand.kihon.installtracker.event.ViewPackageEvent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
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

    @Bind(R.id.text)
    TextView text;
    @Bind(R.id.read_button)
    Button readButton;
    @Bind(R.id.write_button)
    Button writeButton;
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

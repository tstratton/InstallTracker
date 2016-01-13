package net.devrand.kihon.installtracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.ButterKnife;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class MainActivity extends AppCompatActivity {

    TextView text;
    Button readButton;
    Button writeButton;

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = ButterKnife.findById(this, R.id.text);

        readButton = ButterKnife.findById(this, R.id.read_button);
        writeButton = ButterKnife.findById(this, R.id.write_button);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    BufferedSink sink = Okio.buffer(Okio.appendingSink(new File("/sdcard/tylog.txt")));
                    sink.writeUtf8(getLine("something happened"));
                    sink.close();
                } catch (IOException ex) {
                    text.setText("Write Error: " + ex.toString());
                    ex.printStackTrace();
                }
            }
        });

        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    BufferedSource source = Okio.buffer(Okio.source(new File("/sdcard/tylog.txt")));
                    text.setText(source.readUtf8());
                } catch (IOException ex) {
                    text.setText("Read Error: " + ex.toString());
                    ex.printStackTrace();
                }
            }
        });
    }

    String getLine(String string) {
        return String.format("%s == %s\n", sdf.format(new Date(System.currentTimeMillis())), string);
    }
}

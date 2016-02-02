package net.devrand.kihon.installtracker;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import net.devrand.kihon.installtracker.event.ViewPackageEvent;

import java.text.ParseException;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by tstratto on 1/7/2016.
 */
public class EventListFragment extends Fragment {

    private static final String TAG = "EventListFragment";

    Cursor result;

    @Bind(R.id.list) ListView listView;

    public EventListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.event_list_fragment, container, false);
        ButterKnife.bind(this, root);

        SQLiteOpenHelper db = DatabaseHelper.getInstance(getContext());
        result = db
                .getReadableDatabase()
                .query(DatabaseHelper.RECENT_TABLE_NAME,
                        new String[]{"ROWID AS _id",
                                DatabaseHelper.PACKAGE_NAME,
                                DatabaseHelper.TYPE,
                                DatabaseHelper.TIMESTAMP},
                        null, null, null, null, DatabaseHelper.TIMESTAMP + " DESC");
        result.getCount();

        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(getContext(), R.layout.row_item,
                result, new String[]{
                DatabaseHelper.PACKAGE_NAME,
                DatabaseHelper.TYPE,
                DatabaseHelper.TIMESTAMP},
                new int[]{R.id.package_name, R.id.event_name, R.id.timestamp},
                0);
        adapter.setViewBinder(new EventBinder());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SQLiteCursor cursor = (SQLiteCursor) listView.getItemAtPosition(position);
                String packageName = cursor.getString(1);
                EventBus.getDefault().post(new ViewPackageEvent(packageName));
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        result.close();
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        //EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        //EventBus.getDefault().unregister(this);
        super.onStop();
    }

    class EventBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (columnIndex == 3) {
                String timestamp = cursor.getString(columnIndex);
                //System.out.println("setViewValue " + columnIndex + " " + timestamp);
                try {
                    Date time = MainActivity.sdf.parse(timestamp);
                    CharSequence timeString = DateUtils.getRelativeDateTimeString(view.getContext(), time.getTime(),
                            DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
                    ((TextView)view).setText(timeString);
                    return true;
                } catch (ParseException ex) {

                }

            }
            return false;
        }
    }
}

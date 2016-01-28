package net.devrand.kihon.installtracker;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;

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
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SQLiteCursor cursor = (SQLiteCursor) listView.getItemAtPosition(position);
                String packageName = cursor.getString(1);
                packageName = packageName.startsWith("package:") ? packageName.substring("package:".length()) : packageName;
                PackageManager pm = getActivity().getPackageManager();
                try {
                    PackageInfo info = pm.getPackageInfo(packageName, 0);
                    Toast.makeText(view.getContext(), pm.getApplicationLabel(info.applicationInfo), Toast.LENGTH_SHORT).show();
                } catch (PackageManager.NameNotFoundException ex) {
                    ;
                }

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

}

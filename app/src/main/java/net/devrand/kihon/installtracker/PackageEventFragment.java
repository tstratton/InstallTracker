package net.devrand.kihon.installtracker;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tstratto on 1/7/2016.
 */
public class PackageEventFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "PackageEventFragment";

    Cursor result;
    String packageName;

    @Bind(R.id.list)
    ListView listView;
    @Bind(R.id.appinfo)
    Button appInfo;
    @Bind(R.id.applaunch)
    Button appLaunch;

    public PackageEventFragment() {
    }

    public PackageEventFragment(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.package_event_fragment, container, false);
        ButterKnife.bind(this, root);

        SQLiteOpenHelper db = DatabaseHelper.getInstance(getContext());
        result = db
                .getReadableDatabase()
                .query(DatabaseHelper.TABLE_NAME,
                        new String[]{"ROWID AS _id",
                                DatabaseHelper.PACKAGE_NAME,
                                DatabaseHelper.TYPE,
                                DatabaseHelper.TIMESTAMP},
                        String.format("%s = '%s'", DatabaseHelper.PACKAGE_NAME, packageName),
                        null, null, null, DatabaseHelper.TIMESTAMP + " DESC");
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

        appInfo.setOnClickListener(this);
        appLaunch.setOnClickListener(this);

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

    @Override
    public void onClick(View view) {
        String packageBase = packageName.startsWith("package:") ? packageName.substring("package:".length()) : packageName;
        final PackageManager packageManager = view.getContext().getPackageManager();
        Intent launchIntent = packageManager.getLaunchIntentForPackage(packageBase);
        if (launchIntent == null) {
            Toast.makeText(view.getContext(), "app not available", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (view.getId()) {
            case R.id.appinfo:
                //redirect user to app Settings, from http://stackoverflow.com/a/17167502
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse(packageName));
                startActivity(intent);
                break;
            case R.id.applaunch:
                startActivity(launchIntent);
                break;
        }
    }
}

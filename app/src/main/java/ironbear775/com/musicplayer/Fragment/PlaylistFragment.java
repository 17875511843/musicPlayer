package ironbear775.com.musicplayer.Fragment;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.PlaylistAdapter;
import ironbear775.com.musicplayer.Class.Playlist;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.PlaylistDbHelper;

/**
 * Created by ironbear on 2017/2/3.
 */

public class PlaylistFragment extends Fragment implements View.OnClickListener {

    public static final List<Playlist> list = new ArrayList<>();
    public static Fragment playlistDetailFragment;
    private RelativeLayout createNew;
    private PlaylistAdapter adapter;
    private EditText input;
    private ImageView submit;
    private ImageView cancel;
    private ImageView icon;
    private TextView createText;
    private PlaylistDbHelper dbHelper1;
    private SQLiteDatabase database;
    private int flag = 0;
    private boolean isClickable = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.play_list_layout, container, false);
        list.clear();

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        getActivity().registerReceiver(clickableReceiver, filter);

        readList();
        findView(view);

        if (MusicUtils.launchPage == 4) {

            MusicUtils.setLaunchPage(getActivity(), MusicUtils.FROM_ADAPTER);
        }

        return view;
    }

    private void readList() {
        Cursor cursor;
        String db = "create table playlist ("
                + "id integer primary key autoincrement, "
                + "title text,"
                + "count integer)";
        dbHelper1 = new PlaylistDbHelper(getActivity(), "playlist.db", db);
        database = dbHelper1.getWritableDatabase();
        cursor = database.query("playlist", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Playlist playlist = new Playlist(cursor.getString(cursor.getColumnIndex("title")), "");
                list.add(playlist);
            } while (cursor.moveToNext());
        }
        cursor.close();
        dbHelper1.close();
        database.close();
    }

    private void findView(final View view) {
        icon = view.findViewById(R.id.create_playlist_icon);
        createText = view.findViewById(R.id.create_new);
        input = view.findViewById(R.id.create_input);
        cancel = view.findViewById(R.id.create_cancel);
        submit = view.findViewById(R.id.create_submit);
        createNew = view.findViewById(R.id.create_playlist);
        ListView playlist = view.findViewById(R.id.playlist_listView);
        adapter = new PlaylistAdapter(getActivity(), R.layout.playlist_item_layout, list);
        playlist.setAdapter(adapter);
        playlist.setOnItemClickListener((parent, view1, position, id) -> {
            if (isClickable) {
                String name = list.get(position).getName();
                StringBuilder table = new StringBuilder();
                table.append("table");
                for (char a : name.toCharArray()) {
                    table.append((int) a);
                }

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putString("name", table.toString());
                bundle.putString("title", name);

                if (MusicList.playlistFragment != null) {
                    transaction.hide(MusicList.playlistFragment);
                }

                transaction.setCustomAnimations(
                        R.animator.fragment_slide_left_enter,
                        R.animator.fragment_slide_left_exit,
                        R.animator.fragment_slide_right_enter,
                        R.animator.fragment_slide_right_exit);

                playlistDetailFragment = new PlaylistDetailFragment();
                playlistDetailFragment.setArguments(bundle);
                transaction.add(R.id.content, playlistDetailFragment);
                transaction.commit();
            }
        });
        playlist.setOnItemLongClickListener((parent, view12, position, id) -> {
            final String name = list.get(position).getName();
            if (isClickable) {
                final StringBuilder t = new StringBuilder();
                t.append("table");
                for (char a : name.toCharArray()) {
                    t.append((int) a);
                }

                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle(R.string.delete_alert);
                dialog.setMessage(name);

                dialog.setCancelable(true);
                dialog.setPositiveButton(R.string.delete_confrim, (dialog1, which) -> {

                    dbHelper1 = new PlaylistDbHelper(getActivity(), "playlist.db", name);
                    database = dbHelper1.getWritableDatabase();
                    database.delete("playlist", "title = ?", new String[]{name});

                    database.close();
                    dbHelper1.close();
                    list.remove(position);
                    adapter.notifyDataSetChanged();

                    getActivity().deleteDatabase(t.toString() + ".db");

                });
                dialog.setNegativeButton(R.string.delete_cancel, (dialog12, which) -> {

                });
                dialog.show();
            }
            return true;
        });
        createNew.setOnClickListener(this);
        cancel.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_submit:
                String name = input.getText().toString();
                if (name.equals("")) {

                    Intent intent1 = new Intent("show snackBar");
                    intent1.putExtra("text id", R.string.must_have_name);
                    getActivity().sendBroadcast(intent1);
                } else {
                    input.setText("");
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getName().equals(name)) {
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0) {
                        Playlist p = new Playlist(name, "");
                        list.add(p);
                        adapter.notifyDataSetChanged();

                        StringBuilder table = new StringBuilder();

                        table.append("table");

                        for (char a : name.toCharArray()) {
                            table.append((int) a);
                        }
                        Log.d("TABLE", table.toString());
                        String db = "create table " + table.toString() + " ("
                                + "id integer primary key autoincrement, "
                                + "title text,"
                                + "artist text,"
                                + "albumArtUri text, "
                                + "album text, "
                                + "uri text )";
                        dbHelper1 = new PlaylistDbHelper(getActivity(), table.toString() + ".db", db);
                        dbHelper1.getWritableDatabase();
                        dbHelper1.close();

                        PlaylistDbHelper dbHelper2 = new PlaylistDbHelper(getActivity(), "playlist.db", db);
                        database = dbHelper2.getWritableDatabase();
                        ContentValues value = new ContentValues();
                        value.put("title", name);
                        database.insert("playlist", null, value);
                        dbHelper2.close();
                        database.close();
                        flag = 0;
                    }
                    icon.setVisibility(View.VISIBLE);
                    createText.setVisibility(View.VISIBLE);
                    input.setVisibility(View.GONE);
                    cancel.setVisibility(View.GONE);
                    submit.setVisibility(View.GONE);
                }
                break;
            case R.id.create_cancel:
                input.setText("");
                icon.setVisibility(View.VISIBLE);
                createText.setVisibility(View.VISIBLE);
                input.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                submit.setVisibility(View.GONE);
                break;
            case R.id.create_playlist:
                icon.setVisibility(View.GONE);
                createText.setVisibility(View.GONE);
                input.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                submit.setVisibility(View.VISIBLE);
                break;
        }
    }

    private final BroadcastReceiver clickableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "SetClickable_True":
                        isClickable = true;
                        createNew.setClickable(true);
                        break;
                    case "SetClickable_False":
                        isClickable = false;
                        createNew.setClickable(false);
                        break;
                }
            }
        }
    };

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(clickableReceiver);
        super.onDestroyView();
    }
}

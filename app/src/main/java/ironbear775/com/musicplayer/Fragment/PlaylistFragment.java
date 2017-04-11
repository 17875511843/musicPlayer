package ironbear775.com.musicplayer.Fragment;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.PlaylistAdapter;
import ironbear775.com.musicplayer.Class.Playlist;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;
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

        MusicUtils musicUtils = new MusicUtils(getActivity());

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        getActivity().registerReceiver(clickableReceiver,filter);

        readList();
        findView(view);

        if (MusicUtils.launchPage==4) {
            if (MusicService.mediaPlayer.isPlaying()) {
                MusicList.footTitle.setText(MusicService.music.getTitle());
                MusicList.footArtist.setText(MusicService.music.getArtist());
                MusicList.PlayOrPause.setImageResource(R.drawable.footpausewhite);
                Glide.with(this)
                        .load(MusicService.music.getAlbumArtUri())
                        .asBitmap()
                        .placeholder(R.drawable.default_album_art)
                        .into(MusicList.footAlbumArt);
                Glide.with(this)
                        .load(MusicService.music.getAlbumArtUri())
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.default_album_art)
                        .into(MusicList.accountHeader.getHeaderBackgroundView());
            } else {
                MusicListFragment.readMusic(getActivity());
                MusicList.footTitle.setText(MusicListFragment.musicList.get(MusicUtils.pos).getTitle());
                MusicList.footArtist.setText(MusicListFragment.musicList.get(MusicUtils.pos).getArtist());
                MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);
                musicUtils.getFootAlbumArt(MusicUtils.pos, MusicListFragment.musicList);
            }
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
        icon = (ImageView) view.findViewById(R.id.create_playlist_icon);
        createText = (TextView) view.findViewById(R.id.create_new);
        input = (EditText) view.findViewById(R.id.create_input);
        cancel = (ImageView) view.findViewById(R.id.create_cancel);
        submit = (ImageView) view.findViewById(R.id.create_submit);
        createNew = (RelativeLayout) view.findViewById(R.id.create_playlist);
        ListView playlist = (ListView) view.findViewById(R.id.playlist_listView);
        adapter = new PlaylistAdapter(getActivity(), R.layout.playlist_item_layout, list);
        playlist.setAdapter(adapter);
        playlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
            }
        });
        playlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
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
                    dialog.setPositiveButton(R.string.delete_confrim, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dbHelper1 = new PlaylistDbHelper(getActivity(), "playlist.db", name);
                            database = dbHelper1.getWritableDatabase();
                            database.delete("playlist", "title = ?", new String[]{name});

                            database.close();
                            dbHelper1.close();
                            list.remove(position);
                            adapter.notifyDataSetChanged();

                            getActivity().deleteDatabase(t.toString()+".db");

                        }
                    });
                    dialog.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.show();
                }
                return true;
            }
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
                    Snackbar.make(getView(), getResources().getString(R.string.must_have_name), Snackbar.LENGTH_SHORT)
                            .setDuration(1000)
                            .show();
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
                        Log.d("TABLE",table.toString());
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
            switch (intent.getAction()){
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
    };

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(clickableReceiver);
        super.onDestroyView();
    }
}

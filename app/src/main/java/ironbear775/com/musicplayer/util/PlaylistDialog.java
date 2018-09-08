package ironbear775.com.musicplayer.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

import ironbear775.com.musicplayer.activity.MusicList;
import ironbear775.com.musicplayer.adapter.PlaylistAdapter;
import ironbear775.com.musicplayer.entity.Music;
import ironbear775.com.musicplayer.entity.Playlist;
import ironbear775.com.musicplayer.fragment.PlaylistFragment;

import static com.mikepenz.iconics.Iconics.TAG;
import static ironbear775.com.musicplayer.R.id;
import static ironbear775.com.musicplayer.R.layout;
import static ironbear775.com.musicplayer.R.string;

/**
 * Created by ironbear on 2017/2/5.
 */

public class PlaylistDialog extends Dialog implements View.OnClickListener {
    private ImageView createNewIcon;
    private TextView createNewText;
    private ImageView createCancel;
    private ImageView createSubmit;
    private EditText createEdit;
    private ListView playlist;
    private int flag = 0;
    private PlaylistDbHelper dbHelper1;
    private SQLiteDatabase database;
    private PlaylistAdapter adapter;
    private Set<Integer> playlistPositionSet;
    private ArrayList<Music> musicList;
    private Dialog dialog;
    private DialogReceiver receiver;
    private Context mContext;

    public PlaylistDialog(Context context, Set<Integer> positionSet, ArrayList<Music> musicArrayList) {
        super(context);
        playlistPositionSet = positionSet;
        musicList = musicArrayList;
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.playlist_dialog_layout);
        setTitle(string.add_to_playlist);
        PlaylistFragment.list.clear();
        receiver = new DialogReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ADD_TO_PLAYLIST_FINISHED");
        filter.addAction("READ_PLAYLIST_FINISHED");
        filter.addAction("CREATE_PLAYLIST_FINISHED");
        mContext.registerReceiver(receiver, filter);

        findView();
        new Thread(readListRunnable).start();

        playlist.setOnItemClickListener((parent, view, position, id) -> {
            final String name = PlaylistFragment.list.get(position).getName();
            final StringBuilder table = new StringBuilder();
            table.append("table");
            final char abc[] = name.toCharArray();
            for (char a : abc) {
                table.append((int) a);
            }
            dialog = ProgressDialog.show(mContext, null, mContext.getResources().getString(string.adding));
            final Message message = new Message();
            message.what = 1;
            dialog.show();
            if (!MusicList.isAlbum && !MusicList.isArtist) {
                new Thread(() -> {

                    PlaylistDbHelper dbHelper = new PlaylistDbHelper(mContext,
                            table.toString() + ".db", name);
                    SQLiteDatabase database1 = dbHelper.getWritableDatabase();
                    for (int pos : playlistPositionSet) {
                        ContentValues values = new ContentValues();

                        if (pos < musicList.size()) {
                            String uri = musicList.get(pos).getUri();
                            values.put("title", musicList.get(pos).getTitle());
                            values.put("artist", musicList.get(pos).getArtist());
                            values.put("albumArtUri", musicList.get(pos).getAlbumArtUri());
                            values.put("album", musicList.get(pos).getAlbum());
                            values.put("uri", uri);

                            Cursor cur = database1.query(table.toString(),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null);

                            if (cur != null) {
                                if (cur.moveToFirst()) {
                                    do {
                                        if (uri.equals(cur.getString(cur.getColumnIndex("uri")))) {
                                            flag = 1;
                                            break;
                                        }
                                    } while (cur.moveToNext());
                                }
                                cur.close();
                            }
                            if (flag == 0) {
                                database1.insert(table.toString(), null, values);
                                values.clear();
                            } else {
                                flag = 0;
                            }

                        }
                    }
                    dbHelper.close();
                    database1.close();
                    mContext.sendBroadcast(new Intent("ADD_TO_PLAYLIST_FINISHED"));
                }).start();
            } else if (MusicList.isAlbum) {
                new Thread(() -> {

                    PlaylistDbHelper dbHelper = new PlaylistDbHelper(mContext,
                            table.toString() + ".db", name);
                    SQLiteDatabase database1 = dbHelper.getWritableDatabase();
                    for (int pos : playlistPositionSet) {

                        if (pos < musicList.size()) {
                            String albumTag = musicList.get(pos).getAlbum();
                            Cursor cursor;
                            if ("from album in artist".equals(MusicList.fromWhere)
                                    && !"".equals(MusicList.artistInAlbum)) {
                                cursor = mContext.getContentResolver().query(
                                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        null,
                                        MediaStore.Audio.Media.ALBUM + "=? and " + MediaStore.Audio.Media.ARTIST + "=?",
                                        new String[]{albumTag, MusicList.artistInAlbum},
                                        MediaStore.Audio.Media.TITLE);
                            } else {
                                cursor =mContext.getContentResolver().query(
                                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        null,
                                        MediaStore.Audio.Media.ALBUM + "=?",
                                        new String[]{albumTag},
                                        MediaStore.Audio.Media.TITLE);
                            }
                            if (cursor != null && cursor.moveToFirst()) {
                                do {
                                    ContentValues values = new ContentValues();

                                    String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                                    values.put("title", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                                    values.put("artist", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                                    values.put("albumArtUri", String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                                            , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                                    values.put("album", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                                    values.put("uri", uri);

                                    Cursor cur = database1.query(table.toString(),
                                            null,
                                            null,
                                            null,
                                            null, null,
                                            null);
                                    if (cur.moveToFirst()) {
                                        do {
                                            String uri1 = cur.getString(cur.getColumnIndex("uri"));
                                            if (uri.equals(uri1)) {
                                                flag = 1;
                                                break;
                                            }
                                        } while (cur.moveToNext());
                                    }
                                    cur.close();
                                    if (flag == 0) {
                                        database1.insert(table.toString(), null, values);
                                        values.clear();

                                    } else {
                                        flag = 0;
                                    }

                                } while (cursor.moveToNext());
                                cursor.close();
                            }
                        }
                    }

                    database1.close();
                    dbHelper.close();
                    mContext.sendBroadcast(new Intent("ADD_TO_PLAYLIST_FINISHED"));

                }).start();
            } else if (MusicList.isArtist) {

                PlaylistDbHelper dbHelper = new PlaylistDbHelper(mContext,
                        table.toString() + ".db", name);
                SQLiteDatabase database1 = dbHelper.getWritableDatabase();
                new Thread(() -> {
                    for (int pos : playlistPositionSet) {

                        if (pos < musicList.size()) {
                            String artistTag = musicList.get(pos).getArtist();
                            Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    null, MediaStore.Audio.Media.ARTIST + "=?", new String[]{artistTag}, MediaStore.Audio.Media.TITLE);

                            if (cursor != null && cursor.moveToFirst()) {
                                do {
                                    ContentValues values = new ContentValues();

                                    String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                                    values.put("title", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                                    values.put("artist", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                                    values.put("albumArtUri", String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                                            , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                                    values.put("album", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                                    values.put("uri", uri);

                                    Cursor cur = database1.query(table.toString(),
                                            null,
                                            null,
                                            null,
                                            null, null,
                                            null);
                                    if (cur.moveToFirst()) {
                                        do {
                                            String uri1 = cur.getString(cur.getColumnIndex("uri"));
                                            if (uri.equals(uri1)) {
                                                flag = 1;
                                                break;
                                            }
                                        } while (cur.moveToNext());
                                    }
                                    cur.close();
                                    if (flag == 0) {
                                        database1.insert(table.toString(), null, values);
                                        values.clear();

                                    } else {
                                        flag = 0;
                                    }
                                } while (cursor.moveToNext());
                                cursor.close();
                            }
                        }
                    }

                    database1.close();
                    dbHelper.close();
                    mContext.sendBroadcast(new Intent("ADD_TO_PLAYLIST_FINISHED"));
                }).start();

                database.close();
            }
        });
    }

    private Runnable readListRunnable = new Runnable() {
        @Override
        public void run() {
            readList();
            mContext.sendBroadcast(new Intent("READ_PLAYLIST_FINISHED"));
        }
    };

    private void readList() {
        Cursor cursor;
        String db = "create table playlist ("
                + "id integer primary key autoincrement, "
                + "title text,"
                + "count integer)";
        dbHelper1 = new PlaylistDbHelper(mContext, "playlist.db", db);
        database = dbHelper1.getWritableDatabase();
        cursor = database.query("playlist", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Playlist playlist = new Playlist(cursor.getString(cursor.getColumnIndex("title")), "");
                PlaylistFragment.list.add(playlist);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        dbHelper1.close();
    }

    private void findView() {
        RelativeLayout createNew = findViewById(id.dialog_create_playlist);
        createNewIcon = findViewById(id.dialog_create_icon);
        createNewText = findViewById(id.dialog_create_text);
        createCancel = findViewById(id.dialog_create_cancel);
        createSubmit = findViewById(id.dialog_create_submit);
        createEdit = findViewById(id.dialog_create_input);
        playlist = findViewById(id.dialog_listView);
        adapter = new PlaylistAdapter(mContext,
                layout.playlist_item_layout, PlaylistFragment.list);
        playlist.setAdapter(adapter);

        createNew.setOnClickListener(this);
        createCancel.setOnClickListener(this);
        createSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case id.dialog_create_playlist:
                createSubmit.setVisibility(View.VISIBLE);
                createCancel.setVisibility(View.VISIBLE);
                createEdit.setVisibility(View.VISIBLE);
                createNewIcon.setVisibility(View.GONE);
                createNewText.setVisibility(View.GONE);
                MusicUtils.requestFocus(createEdit);
                if (mContext instanceof Activity)
                    MusicUtils.showSoftKeyboard((Activity) mContext);
                break;
            case id.dialog_create_cancel:
                createEdit.setText("");
                createSubmit.setVisibility(View.GONE);
                createCancel.setVisibility(View.GONE);
                createEdit.setVisibility(View.GONE);
                createNewIcon.setVisibility(View.VISIBLE);
                createNewText.setVisibility(View.VISIBLE);
                if (mContext instanceof Activity)
                    MusicUtils.hideSoftKeyboard((Activity) mContext, createEdit);
                break;
            case id.dialog_create_submit:
                String name = createEdit.getText().toString();
                StringBuilder table = new StringBuilder();
                table.append("table");
                char abc[] = name.toCharArray();
                for (char a : abc) {
                    table.append((int) a);
                }

                if (name.equals("")) {
                    Snackbar.make(createEdit, v.getResources().getString(string.must_have_name), Snackbar.LENGTH_SHORT)
                            .setDuration(1000)
                            .show();
                } else {
                    createEdit.setText("");
                    for (int i = 0; i < PlaylistFragment.list.size(); i++) {
                        if (PlaylistFragment.list.get(i).getName().equals(name)) {
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 1) {
                        Snackbar.make(playlist, v.getResources().getString(string.already_have) + name, Snackbar.LENGTH_SHORT)
                                .setDuration(1000)
                                .show();
                    } else if (flag == 0) {
                        String db = "create table " + table.toString() + " ("
                                + "id integer primary key autoincrement, "
                                + "title text,"
                                + "artist text,"
                                + "albumArtUri text, "
                                + "album text, "
                                + "uri text )";
                        dbHelper1 = new PlaylistDbHelper(mContext, table.toString() + ".db", db);
                        dbHelper1.getWritableDatabase();

                        PlaylistDbHelper dbHelper2 = new PlaylistDbHelper(mContext, "playlist.db", db);
                        database = dbHelper2.getWritableDatabase();
                        ContentValues value = new ContentValues();
                        value.put("title", name);
                        database.insert("playlist", null, value);
                        database.close();
                        dbHelper2.close();
                        flag = 0;
                        Playlist p = new Playlist(name, "");
                        PlaylistFragment.list.add(p);
                        mContext.sendBroadcast(new Intent("CREATE_PLAYLIST_FINISHED"));

                        createEdit.setText("");
                        createSubmit.setVisibility(View.GONE);
                        createCancel.setVisibility(View.GONE);
                        createEdit.setVisibility(View.GONE);
                        createNewIcon.setVisibility(View.VISIBLE);
                        createNewText.setVisibility(View.VISIBLE);
                        if (mContext instanceof Activity)
                            MusicUtils.hideSoftKeyboard((Activity) mContext, createEdit);
                    }

                }
                break;

        }
    }

    private class DialogReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case "ADD_TO_PLAYLIST_FINISHED":
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                        Snackbar.make(playlist, mContext.getResources().getString(string.added), Snackbar.LENGTH_LONG)
                                .setDuration(1000)
                                .show();
                    }
                    break;
                case "READ_PLAYLIST_FINISHED":
                    if (adapter != null)
                        adapter.notifyDataSetChanged();
                    break;
                case "CREATE_PLAYLIST_FINISHED":
                    if (adapter != null)
                        adapter.notifyDataSetChanged();

                    createSubmit.setVisibility(View.GONE);
                    createCancel.setVisibility(View.GONE);
                    createEdit.setVisibility(View.GONE);
                    createNewIcon.setVisibility(View.VISIBLE);
                    createNewText.setVisibility(View.VISIBLE);
                    break;

            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        mContext.unregisterReceiver(receiver);
    }
}


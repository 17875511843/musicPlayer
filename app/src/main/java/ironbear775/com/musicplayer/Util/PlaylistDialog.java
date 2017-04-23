package ironbear775.com.musicplayer.Util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.HashSet;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.PlaylistAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Class.Playlist;
import ironbear775.com.musicplayer.Fragment.PlaylistFragment;

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
    private Set<Integer> playlistPositionSet = new HashSet<>();
    private ArrayList<Music> musicList = new ArrayList<>();
    private Dialog dialog;
    public PlaylistDialog(Context context, Set<Integer> positionSet, ArrayList<Music> musicArrayList) {
        super(context);
        playlistPositionSet = positionSet;
        musicList = musicArrayList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.playlist_dialog_layout);
        setTitle(string.add_to_playlist);
        PlaylistFragment.list.clear();
        readList();
        findView();
        playlist.setOnItemClickListener((parent, view, position, id) -> {
            final String name = PlaylistFragment.list.get(position).getName();
            final StringBuilder table = new StringBuilder();
            table.append("table");
            final char abc[] = name.toCharArray();
            for (char a:abc){
                table.append((int)a);
            }
            dialog = ProgressDialog.show(getContext(),null,getContext().getResources().getString(string.adding));
            final Message message = new Message();
            message.what = 1;
            dialog.show();
            if (!MusicList.isAlbum && !MusicList.isArtist) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int pos : playlistPositionSet) {
                            ContentValues values = new ContentValues();
                            String title = musicList.get(pos).getTitle();
                            String artist = musicList.get(pos).getArtist();
                            String albumArtUri = musicList.get(pos).getAlbumArtUri();
                            String album = musicList.get(pos).getAlbum();
                            String uri = musicList.get(pos).getUri();
                            values.put("title", title);
                            values.put("artist", artist);
                            values.put("albumArtUri", albumArtUri);
                            values.put("album", album);
                            values.put("uri", uri);
                            PlaylistDbHelper dbHelper = new PlaylistDbHelper(getContext(),
                                    table.toString() + ".db", name);
                            SQLiteDatabase database1 = dbHelper.getWritableDatabase();

                            Cursor cur = database1.query(table.toString(), null, null, null, null, null, null);
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

                        }
                        database.close();
                        addHandler.sendMessage(message);
                    }
                }).start();
            } else if (MusicList.isAlbum) {
                new Thread(() -> {
                    for (int pos : playlistPositionSet) {
                        String albumTag = musicList.get(pos).getAlbum();
                        Log.d("Tag", albumTag);
                        ArrayList<Music> albumMusicList = new ArrayList<>();
                        Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                null, MediaStore.Audio.Media.ALBUM + "=?", new String[]{albumTag}, MediaStore.Audio.Media.TITLE);

                        if (cursor != null && cursor.moveToFirst()) {
                            do {
                                int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                                String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                                final int album_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                                Music music = new Music();
                                String albumArtUri = String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                                        , album_id));

                                music.setAlbumArtUri(albumArtUri);
                                music.setTitle(title);
                                music.setAlbum_id(album_id);
                                music.setUri(uri);
                                music.setAlbum(album);
                                music.setArtist(artist);
                                music.setDuration(duration);

                                if (duration > 20000) {
                                    albumMusicList.add(music);
                                }
                            } while (cursor.moveToNext());
                            cursor.close();
                        }
                        for (int i = 0; i < albumMusicList.size(); i++) {
                            ContentValues values = new ContentValues();
                            String title = albumMusicList.get(i).getTitle();
                            String artist = albumMusicList.get(i).getArtist();
                            String albumArtUri = albumMusicList.get(i).getAlbumArtUri();
                            String album = albumMusicList.get(i).getAlbum();
                            String uri = albumMusicList.get(i).getUri();
                            values.put("title", title);
                            values.put("artist", artist);
                            values.put("albumArtUri", albumArtUri);
                            values.put("album", album);
                            values.put("uri", uri);
                            PlaylistDbHelper dbHelper = new PlaylistDbHelper(getContext(),
                                    table.toString() + ".db", name);
                            SQLiteDatabase database1 = dbHelper.getWritableDatabase();

                            Cursor cur = database1.query(table.toString(), null, null, null, null, null, null);
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

                        }
                    }
                    database.close();
                    addHandler.sendMessage(message);
                }).start();
            } else if (MusicList.isArtist) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int pos : playlistPositionSet) {

                            String artistTag = musicList.get(pos).getArtist();
                            ArrayList<Music> artistMusicList = new ArrayList<>();
                            Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    null, MediaStore.Audio.Media.ARTIST + "=?", new String[]{artistTag}, MediaStore.Audio.Media.TITLE);

                            if (cursor != null && cursor.moveToFirst()) {
                                do {
                                    int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                                    String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                                    final int album_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                                    Music music = new Music();
                                    String albumArtUri = String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                                            , album_id));

                                    music.setAlbumArtUri(albumArtUri);
                                    music.setTitle(title);
                                    music.setAlbum_id(album_id);
                                    music.setUri(uri);
                                    music.setAlbum(album);
                                    music.setArtist(artist);
                                    music.setDuration(duration);

                                    if (duration > 20000) {
                                        artistMusicList.add(music);
                                    }
                                } while (cursor.moveToNext());
                                cursor.close();
                            }
                            for (int i = 0; i < artistMusicList.size(); i++) {
                                ContentValues values = new ContentValues();
                                String title = artistMusicList.get(i).getTitle();
                                String artist = artistMusicList.get(i).getArtist();
                                String albumArtUri = artistMusicList.get(i).getAlbumArtUri();
                                String album = artistMusicList.get(i).getAlbum();
                                String uri = artistMusicList.get(i).getUri();
                                values.put("title", title);
                                values.put("artist", artist);
                                values.put("albumArtUri", albumArtUri);
                                values.put("album", album);
                                values.put("uri", uri);
                                PlaylistDbHelper dbHelper = new PlaylistDbHelper(getContext(),
                                        table.toString() + ".db", name);
                                SQLiteDatabase database1 = dbHelper.getWritableDatabase();

                                Cursor cur = database1.query(table.toString(), null, null, null, null, null, null);
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

                            }
                        }
                        database.close();
                        addHandler.sendMessage(message);
                    }
                }).start();

            }
        });
    }

    private Handler addHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1 && dialog.isShowing()){
                dialog.dismiss();
                Snackbar.make(playlist, getContext().getResources().getString(string.added), Snackbar.LENGTH_LONG)
                        .setDuration(1000)
                        .show();
            }
        }
    };

    private void readList() {
        Cursor cursor;
        String db = "create table playlist ("
                + "id integer primary key autoincrement, "
                + "title text,"
                + "count integer)";
        dbHelper1 = new PlaylistDbHelper(getContext(), "playlist.db", db);
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
        RelativeLayout createNew = (RelativeLayout) findViewById(id.dialog_create_playlist);
        createNewIcon = (ImageView) findViewById(id.dialog_create_icon);
        createNewText = (TextView) findViewById(id.dialog_create_text);
        createCancel = (ImageView) findViewById(id.dialog_create_cancel);
        createSubmit = (ImageView) findViewById(id.dialog_create_submit);
        createEdit = (EditText) findViewById(id.dialog_create_input);
        playlist = (ListView) findViewById(id.dialog_listView);
        adapter = new PlaylistAdapter(getContext(),
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
                break;
            case id.dialog_create_cancel:
                createEdit.setText("");
                createSubmit.setVisibility(View.GONE);
                createCancel.setVisibility(View.GONE);
                createEdit.setVisibility(View.GONE);
                createNewIcon.setVisibility(View.VISIBLE);
                createNewText.setVisibility(View.VISIBLE);
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
                        dbHelper1 = new PlaylistDbHelper(getContext(), table.toString() + ".db", db);
                        dbHelper1.getWritableDatabase();

                        PlaylistDbHelper dbHelper2 = new PlaylistDbHelper(getContext(), "playlist.db", db);
                        database = dbHelper2.getWritableDatabase();
                        ContentValues value = new ContentValues();
                        value.put("title", name);
                        database.insert("playlist", null, value);
                        database.close();
                        dbHelper2.close();
                        flag = 0;
                        Playlist p = new Playlist(name, "");
                        PlaylistFragment.list.add(p);
                        adapter.notifyDataSetChanged();
                    }

                    createSubmit.setVisibility(View.GONE);
                    createCancel.setVisibility(View.GONE);
                    createEdit.setVisibility(View.GONE);
                    createNewIcon.setVisibility(View.VISIBLE);
                    createNewText.setVisibility(View.VISIBLE);
                }
                break;

        }
    }
}

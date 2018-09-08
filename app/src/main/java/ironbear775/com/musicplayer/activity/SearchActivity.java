package ironbear775.com.musicplayer.activity;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

import ironbear775.com.musicplayer.adapter.MusicAdapter;
import ironbear775.com.musicplayer.entity.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.service.MusicService;
import ironbear775.com.musicplayer.util.MusicUtils;

/**
 * Created by ironbear on 2017/4/29.
 */

public class SearchActivity extends BaseActivity {
    private Toolbar toolbar;
    private EditText searchEdit;
    public static ArrayList<Music> musicList = new ArrayList<>();
    private FastScrollRecyclerView listview;
    private MusicAdapter adapter;
    public static int count = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);

        findView();
        IntentFilter filter = new IntentFilter();
        filter.addAction("notifyDataSetChanged");
        filter.addAction("SEARCH_FINISHED");
        registerReceiver(clickableReceiver, filter);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String searchTag = s.toString().trim();
                musicList.clear();
                if (searchTag.length() >= 1) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Cursor cursor = getContentResolver()
                                    .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                            null,
                                            MediaStore.Audio.Media.TITLE + " LIKE? OR " +
                                                    MediaStore.Audio.Media.ALBUM + " LIKE? OR " +
                                                    MediaStore.Audio.Media.ARTIST + " LIKE?",
                                            new String[]{"%" + searchTag + "%", "%" + searchTag + "%", "%" + searchTag + "%"},
                                            MediaStore.Audio.Media.TITLE);
                            if (cursor != null) {
                                if (cursor.moveToFirst()) {
                                    do {
                                        Music music = new Music();

                                        music.setID(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                                        music.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));

                                        music.setAlbumArtUri(String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                                                , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                                        music.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                                        music.setAlbum_id(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                                        music.setUri(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                                        music.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                                        music.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                                        music.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                                        if (!music.getUri().contains(".wmv")) {
                                            if (music.getDuration() >= MusicUtils.getInstance().time[MusicUtils.getInstance().filterNum]) {
                                                musicList.add(music);
                                            }
                                        }
                                    } while (cursor.moveToNext());
                                }

                                cursor.close();
                                sendBroadcast(new Intent("SEARCH_FINISHED"));
                            }
                        }
                    }).start();
                } else
                    listview.setVisibility(View.GONE);

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    private void findView() {
        toolbar = findViewById(R.id.search_toolbar);
        searchEdit = findViewById(R.id.search_edit);
        listview = findViewById(R.id.search_result_list);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        listview.setLayoutManager(manager);

        adapter = new MusicAdapter(this, musicList);
        listview.setAdapter(adapter);

        adapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                setClickAction(position);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void setClickAction(int position) {

       
        MusicUtils.getInstance().startMusic(this,position, 0, 7);
        
        Intent intent = new Intent("update_cover");
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        if (MusicService.mediaPlayer.isPlaying() && count == 1) {
            Intent intent = new Intent("search_play");
            sendBroadcast(intent);
        }
        unregisterReceiver(clickableReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private final BroadcastReceiver clickableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "notifyDataSetChanged":
                        adapter.notifyDataSetChanged();
                        break;
                    case "SEARCH_FINISHED":
                        listview.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();

                        if (musicList.size() == 0)
                            listview.setVisibility(View.GONE);
                        break;
                }
            }
        }
    };
}

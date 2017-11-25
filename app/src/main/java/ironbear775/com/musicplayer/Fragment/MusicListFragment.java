package ironbear775.com.musicplayer.Fragment;


import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.MusicAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2017/1/24.
 */

public class MusicListFragment extends android.app.Fragment {
    public static ArrayList<Music> musicList = new ArrayList<>();
    public static int pos = 0;
    public static int count = 0;
    public static final Set<Integer> positionSet = new HashSet<>();
    private MusicAdapter musicAdapter;
    private com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView musicView;
    private MusicUtils musicUtils;
    private Toolbar toolbar;
    private RelativeLayout shuffle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shuffle_item_layout, container, false);
        findView(view);

        setHasOptionsMenu(true);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        getActivity().sendBroadcast(new Intent("set toolbar gone"));

        toolbar.setTitle(R.string.toolbar_title_music_player);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);

        if (!MusicUtils.enableShuffle)
            shuffle.setVisibility(View.GONE);
        else
            shuffle.setVisibility(View.VISIBLE);

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("notifyDataSetChanged");
        filter.addAction("enableShuffle");
        filter.addAction("refresh adapter");

        getActivity().registerReceiver(clickableReceiver, filter);

        musicList.clear();

        readMusic(getActivity());

        initView();

        musicUtils = new MusicUtils(getActivity());

        pos = MusicUtils.pos;

        if (MusicUtils.launchPage == 1) {
            if (MusicList.first) {
                MusicList.list = musicList;
                MusicList.shufflelist = musicList;
                getActivity();
                SharedPreferences.Editor ed = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE).edit();
                ed.putBoolean("firstTime", false);
                ed.apply();
                ed.commit();
                MusicUtils.saveArray(getActivity(), MusicList.list);
                MusicUtils.saveShuffleArray(getActivity(), MusicList.shufflelist);
            }
            MusicUtils.setLaunchPage(getActivity(),MusicUtils.FROM_ADAPTER);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().sendBroadcast(new Intent("open drawer"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initAdapter(){
        musicAdapter = new MusicAdapter(getActivity(), musicList);
        musicView.setAdapter(musicAdapter);

        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (MusicList.actionMode != null) {
                    //多选状态
                    musicUtils.addOrRemoveItem(position, positionSet, musicAdapter);
                } else {
                    setClickAction(position);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (MusicList.actionMode == null) {
                    Intent intent = new Intent("ActionModeChanged");
                    getActivity().sendBroadcast(intent);
                }
            }
        });
    }
    private void initView() {

        musicView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        musicView.setLayoutManager(layoutManager);

        initAdapter();

        shuffle.setOnClickListener(v -> {
            count = 1;
            MusicRecentAddedFragment.count = 0;
            AlbumDetailFragment.count = 0;
            ArtistDetailFragment.count = 0;
            PlaylistDetailFragment.count = 0;
            FolderDetailFragment.count = 0;
            MusicList.count = 0;

            musicUtils = new MusicUtils(v.getContext());
            musicUtils.shufflePlay(musicList, 1);
        });
    }

    private void setClickAction(int position) {

        int progress = 0;

        count = 1;
        MusicRecentAddedFragment.count = 0;
        AlbumDetailFragment.count = 0;
        ArtistDetailFragment.count = 0;
        PlaylistDetailFragment.count = 0;
        FolderDetailFragment.count = 0;
        MusicList.count = 0;

        musicUtils = new MusicUtils(getActivity());
        musicUtils.startMusic(position, progress, 1);

        Intent intent = new Intent("set footBar");
        Intent intent1 = new Intent("set PlayOrPause");
        intent.putExtra("footTitle",musicList.get(position).getTitle());
        intent.putExtra("footArtist",musicList.get(position).getArtist());
        intent1.putExtra("PlayOrPause",R.drawable.footplaywhite);
        getActivity().sendBroadcast(intent);
        getActivity().sendBroadcast(intent1);

        musicUtils.getFootAlbumArt(position, musicList,MusicUtils.FROM_ADAPTER);

        pos = position;

    }


    private void findView(View view) {
        shuffle = view.findViewById(R.id.shuffle);
        toolbar = view.findViewById(R.id.music_toolbar);
        musicView = view.findViewById(R.id.music_list);
    }

    //获取音乐各种信息
    public static void readMusic(Context context) {

        Cursor cursor;
        cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (MusicUtils.isFlyme) {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {

                        Music music = new Music();
                        music.setID(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                        music.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));

                        music.setAlbumArtUri(String.valueOf(ContentUris.withAppendedId(
                                Uri.parse("content://media/external/audio/albumart")
                                , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                        music.setUri(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                        music.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                        music.setAlbum_id(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                        music.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                        music.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                        music.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                        if (!music.getUri().contains(".wmv")) {
                            if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum])
                                musicList.add(music);
                        }
                    } while (cursor.moveToNext());
                    cursor.close();
                }
            }
        } else {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        if (cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC) == 17) {

                            Music music = new Music();
                            music.setID(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                            music.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));

                            music.setAlbumArtUri(String.valueOf(ContentUris.withAppendedId(
                                    Uri.parse("content://media/external/audio/albumart")
                                    , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                            music.setUri(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                            music.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                            music.setAlbum_id(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                            music.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                            music.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                            music.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                            if (!music.getUri().contains(".wmv")) {
                                if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum])
                                    musicList.add(music);
                            }
                        }
                    } while (cursor.moveToNext());
                    cursor.close();
                }
            }
        }
    }

    private final BroadcastReceiver clickableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "SetClickable_True":
                        if (musicAdapter != null)
                        musicAdapter.setClickable(true);
                        break;
                    case "SetClickable_False":
                        musicAdapter.setClickable(false);
                        break;
                    case "notifyDataSetChanged":
                        musicAdapter.notifyDataSetChanged();
                        musicList.clear();
                        readMusic(context);
                        break;
                    case "enableShuffle":
                        if (!MusicUtils.enableShuffle)
                            shuffle.setVisibility(View.GONE);
                        else
                            shuffle.setVisibility(View.VISIBLE);

                        break;
                    case "refresh adapter":
                        //initView();
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

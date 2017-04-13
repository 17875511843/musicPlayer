package ironbear775.com.musicplayer.Fragment;


import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.MusicAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2017/1/24.
 */

public class MusicListFragment extends android.app.Fragment {
    public static final ArrayList<Music> musicList = new ArrayList<>();
    public static int pos;
    public static int count = 0;
    public static final Set<Integer> positionSet = new HashSet<>();
    private MusicAdapter musicAdapter;
    private com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView musicView;
    private MusicUtils musicUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_layout, container, false);
        findView(view);

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("notifyDataSetChanged");
        getActivity().registerReceiver(clickableReceiver, filter);

        musicList.clear();

        readMusic(getActivity());
        Log.d("SIZE", musicList.size()+"");

        musicUtils = new MusicUtils(getActivity());

        initView();
        pos = MusicUtils.pos;

        if (MusicUtils.launchPage==1) {
            if (MusicList.flag == 1 && MusicService.mediaPlayer.isPlaying()) {
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
            } else if (MusicList.flag == 1) {
                MusicList.footTitle.setText(MusicService.music.getTitle());
                MusicList.footArtist.setText(MusicService.music.getArtist());
                MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);
                Glide.with(this)
                        .load(MusicService.music.getAlbumArtUri())
                        .placeholder(R.drawable.default_album_art)
                        .into(MusicList.footAlbumArt);
                Glide.with(this)
                        .load(MusicService.music.getAlbumArtUri())
                        .centerCrop()
                        .placeholder(R.drawable.default_album_art)
                        .into(MusicList.accountHeader.getHeaderBackgroundView());
            } else if (musicList.size() >= 1 && MusicList.flag == 0) {
                MusicList.footTitle.setText(musicList.get(pos).getTitle());
                MusicList.footArtist.setText(musicList.get(pos).getArtist());
                MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);
                musicUtils.getFootAlbumArt(pos, musicList);
            }
        }

        return view;
    }

    private void initView() {
        musicView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        musicView.setLayoutManager(layoutManager);

        musicView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .color(Color.parseColor("#22616161"))
                .sizeResId(R.dimen.divider)
                .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                .build());
        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.shuffle, musicView, false);
        musicAdapter = new MusicAdapter(getActivity(), musicList);
        musicAdapter.setHeaderView(headerView);
        musicView.setAdapter(musicAdapter);

        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (MusicList.actionMode != null) {
                    //多选状态
                    if (position!=0){
                        musicUtils.addOrRemoveItem(position,positionSet,musicAdapter,true);
                    }
                } else {
                    setClickAction(position);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (position > 0) {
                    if (MusicList.actionMode == null) {
                        Intent intent = new Intent("ActionModeChanged");
                        getActivity().sendBroadcast(intent);
                    }
                }
            }
        });

    }

    private void setClickAction(int position) {

        int progress = 0;

        count = 1;
        MusicRecentAddedFragment.count = 0;
        AlbumDetailFragment.count = 0;
        ArtistDetailFragment.count = 0;
        PlaylistDetailFragment.count = 0;

        if (position == 0) {
            musicUtils.shufflePlay(musicList);
        } else {
            position = position - 1;

            musicUtils.startMusic(position,musicList, progress);

            MusicList.footTitle.setText(musicList.get(position).getTitle());
            MusicList.footArtist.setText(musicList.get(position).getArtist());
            MusicList.PlayOrPause.setImageResource(R.drawable.footpausewhite);

            musicUtils.getFootAlbumArt(position,musicList);

            pos = position;
        }
    }

    private void findView(View view) {
        musicView = (FastScrollRecyclerView) view.findViewById(R.id.music_list);
    }

    //获取音乐各种信息
    public static void readMusic(Context context) {

        Cursor cursor;
        cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Music music = new Music();

                    music.setID(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                    music.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
                    music.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                    music.setAlbumArtUri(String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                            , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                    music.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    music.setAlbum_id(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                    music.setUri(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                    music.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                    music.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));


                    if (music.getDuration() >= 20000 && !music.getUri().contains(".wmv")) {
                        musicList.add(music);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
    }

    private final BroadcastReceiver clickableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "SetClickable_True":
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
            }
        }
    };

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(clickableReceiver);
        super.onDestroyView();
    }
}

package ironbear775.com.musicplayer.Fragment;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.ArtistAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2017/1/26.
 */

public class ArtistListFragment extends Fragment {
    public static final ArrayList<Music> artistlist = new ArrayList<>();
    public static int count = 0;
    public static Fragment artistDetailFragment;
    public static final Set<Integer> positionSet = new HashSet<>();
    private ArtistAdapter artistAdapter;
    private com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView artistView;
    private MusicUtils musicUtils;

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

        artistlist.clear();
        readArtist(getActivity());

        musicUtils = new MusicUtils(getActivity());

        initView();

        if (MusicUtils.launchPage==2) {
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
                        .placeholder(R.drawable.default_album_art_land)
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

    private void initView() {
        artistView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                getActivity(), LinearLayout.VERTICAL, false);
        artistView.setLayoutManager(layoutManager);

        artistAdapter = new ArtistAdapter(getActivity(), artistlist);
        artistView.setAdapter(artistAdapter);

        if (artistlist.size() > 0) {
            artistAdapter.setOnItemClickListener(new ArtistAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (MusicList.actionMode != null) {
                        //多选状态
                        musicUtils.addOrRemoveItem(position, positionSet, artistAdapter);
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
    }

    private void setClickAction(int position) {
        count = 1;
        AlbumListFragment.count = 1;
        String artistTag = artistlist.get(position).getArtist();
        ArrayList<Music> songMusicList = new ArrayList<>();
        Cursor cursor = getActivity().getBaseContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media.ARTIST + "=?",
                new String[]{artistTag},
                MediaStore.Audio.Media.TITLE);
        if (MusicUtils.isFlyme){
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                            Music music = new Music();

                            music.setID(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                            music.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));

                            music.setAlbumArtUri(String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                                    , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                            music.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                            music.setAlbum_id(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                            music.setUri(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                            music.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                            music.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                            music.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                            if (!music.getUri().contains(".wmv")) {
                                if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum])
                                    songMusicList.add(music);
                            }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }else {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        if (cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC) == 17) {
                            Music music = new Music();

                            music.setID(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                            music.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));

                            music.setAlbumArtUri(String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                                    , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                            music.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                            music.setAlbum_id(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                            music.setUri(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                            music.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                            music.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                            music.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                            if (!music.getUri().contains(".wmv")) {
                                if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum])
                                    songMusicList.add(music);
                            }
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("artist",artistTag);
        bundle.putParcelableArrayList("musicList", songMusicList);

        if (MusicList.artistFragment != null) {
            transaction.hide(MusicList.artistFragment);
        }
        transaction.setCustomAnimations(
                R.animator.fragment_slide_left_enter,
                R.animator.fragment_slide_left_exit,
                R.animator.fragment_slide_right_enter,
                R.animator.fragment_slide_right_exit);
        artistDetailFragment = new ArtistDetailFragment();
        artistDetailFragment.setArguments(bundle);
        transaction.add(R.id.content, artistDetailFragment);
        transaction.commit();

    }


    private void findView(View view) {
        artistView = (FastScrollRecyclerView) view.findViewById(R.id.music_list);
    }

    private void readArtist(Context context) {
        Music music = new Music();
        Music music1 = new Music();
        music.setArtist("");
        music1.setArtist("");

        Cursor cursor;
        cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.ARTIST);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {

                    if (music.getArtist().equals("")) {
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
                    }
                    if (!music.getArtist().equals("") && music1.getArtist().equals("")) {
                        music1.setID(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                        music1.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
                        music1.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                        music1.setAlbumArtUri(String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                                , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                        music1.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                        music1.setAlbum_id(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                        music1.setUri(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                        music1.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                        music1.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                    }
                    if (!music.getArtist().equals("") && !music1.getArtist().equals("")) {

                        Music musicAlbum = new Music();
                        if (!music.getArtist().equals(music1.getArtist())) {
                            musicAlbum.setID(music.getID());
                            musicAlbum.setSize(music.getSize());
                            musicAlbum.setDuration(music.getDuration());

                            musicAlbum.setAlbumArtUri(music.getAlbumArtUri());
                            musicAlbum.setTitle(music.getUri());
                            musicAlbum.setAlbum_id(music.getAlbum_id());
                            musicAlbum.setUri(music.getUri());
                            musicAlbum.setAlbum(music.getAlbum());
                            musicAlbum.setArtist(music.getArtist());
                            if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum])
                                artistlist.add(musicAlbum);
                        }
                        music.setID(music1.getID());
                        music.setSize(music1.getSize());
                        music.setDuration(music1.getDuration());

                        music.setAlbumArtUri(music1.getAlbumArtUri());
                        music.setTitle(music1.getUri());
                        music.setAlbum_id(music1.getAlbum_id());
                        music.setUri(music1.getUri());
                        music.setAlbum(music1.getAlbum());
                        music.setArtist(music1.getArtist());

                        music1.setArtist("");
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
            if (!music.getArtist().equals("")) {
                artistlist.add(music);
            }
        }
    }

    private final BroadcastReceiver clickableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "SetClickable_True":
                    artistAdapter.setClickable(true);
                    break;
                case "SetClickable_False":
                    artistAdapter.setClickable(false);
                    break;
                case "notifyDataSetChanged":
                    artistAdapter.notifyDataSetChanged();
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

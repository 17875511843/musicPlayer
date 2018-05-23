package ironbear775.com.musicplayer.Fragment;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.ArtistAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2017/1/26.
 */

public class ArtistListFragment extends Fragment {
    public static final ArrayList<Music> artistlist = new ArrayList<>();
    public static final ArrayList<Music> artistDetailAlbumList = new ArrayList<>();
    public static Fragment artistDetailFragment;
    public static final Set<Integer> positionSet = new HashSet<>();
    private ArtistAdapter artistAdapter;
    private com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView artistView;
    private String artist;

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("ArtistListFragment");
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("ArtistListFragment");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_layout, container, false);
        findView(view);

        reCreateView();

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("notifyDataSetChanged");
        filter.addAction("restart yourself");
        filter.addAction("ARTIST_LIST_FRAGMENT_READ_MUSIC_FINISHED");
        filter.addAction("ARTIST_DETAIL_FRAGMENT_READ_MUSIC_FINISHED");
        getActivity().registerReceiver(clickableReceiver, filter);

        artistlist.clear();
        new Thread(readArtistRunnable).start();

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
                        MusicUtils.getInstance().addOrRemoveItem(getActivity(), position, positionSet, artistAdapter);
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
        MusicUtils.getInstance().isArtistlistFragmentOpen = true;
        artist = artistlist.get(position).getArtist();

        new Thread(readMusicRunnable).start();
    }


    private void findView(View view) {
        artistView = view.findViewById(R.id.music_list);
    }

    private Runnable readArtistRunnable = new Runnable() {
        @Override
        public void run() {
            readArtist(getActivity());

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initView();

                    if (MusicUtils.getInstance().launchPage == 2) {
                        MusicUtils.getInstance().setLaunchPage(getActivity(), MusicUtils.getInstance().FROM_ADAPTER);
                    }
                    if (artistAdapter != null)
                        artistAdapter.notifyDataSetChanged();
                    if (readArtistRunnable != null)
                        readArtistRunnable = null;


                }
            });
        }
    };
    private Runnable readMusicRunnable = new Runnable() {
        @Override
        public void run() {
            readArtistAlbum(getActivity());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putString("artist", artist);
                    bundle.putParcelableArrayList("musiclist", artistDetailAlbumList);

                    if (MusicList.artistFragment != null) {
                        transaction.hide(MusicList.artistFragment);
                    }

                    transaction.setCustomAnimations(
                            R.animator.fragment_slide_left_enter,
                            R.animator.fragment_slide_left_exit);
                    artistDetailFragment = new ArtistDetailFragment();
                    artistDetailFragment.setArguments(bundle);
                    transaction.add(R.id.content, artistDetailFragment);
                    transaction.commit();
                    MusicUtils.getInstance().fromWhere = MusicUtils.getInstance().FROM_ARTIST_PAGE;
                }
            });
        }
    };

    private void readArtistAlbum(Context context) {
        artistDetailAlbumList.clear();
        int flag = 0;
        Cursor albumCursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media.ARTIST + "=?",
                new String[]{artist},
                MediaStore.Audio.Media.ALBUM);
        if (MusicUtils.getInstance().isFlyme) {
            if (albumCursor != null) {
                if (albumCursor.moveToFirst()) {
                    do {
                        Music music = new Music();
                        music.setAlbum(albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));

                        if (artistDetailAlbumList.size() > 0) {
                            for (int i = 0; i < artistDetailAlbumList.size(); i++) {
                                if (music.getAlbum().equals(artistDetailAlbumList.get(i).getAlbum()))
                                    flag = 1;
                            }
                        }
                        if (flag == 0) {
                            music.setID(albumCursor.getLong(albumCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                            music.setSize(albumCursor.getLong(albumCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));

                            music.setAlbumArtUri(String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                                    , albumCursor.getInt(albumCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                            music.setTitle(albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                            music.setAlbum_id(albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                            music.setUri(albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                            music.setArtist(albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                            music.setDuration(albumCursor.getInt(albumCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                            if (!music.getUri().contains(".wmv")) {
                                if (music.getDuration() >= MusicUtils.getInstance().time[MusicUtils.getInstance().filterNum])
                                    artistDetailAlbumList.add(music);
                            }

                        } else
                            flag = 0;

                    } while (albumCursor.moveToNext());
                }
                albumCursor.close();
            }
        } else {
            if (albumCursor != null) {
                if (albumCursor.moveToFirst()) {
                    do {
                        Music music = new Music();
                        music.setAlbum(albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));

                        if (artistDetailAlbumList.size() > 0) {
                            for (int i = 0; i < artistDetailAlbumList.size(); i++) {
                                if (music.getAlbum().equals(artistDetailAlbumList.get(i).getAlbum()))
                                    flag = 1;
                            }
                        }
                        if (flag == 0) {
                            music.setID(albumCursor.getLong(albumCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                            music.setSize(albumCursor.getLong(albumCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));

                            music.setAlbumArtUri(String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                                    , albumCursor.getInt(albumCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                            music.setTitle(albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                            music.setAlbum_id(albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                            music.setUri(albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                            music.setArtist(albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                            music.setDuration(albumCursor.getInt(albumCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                            if (!music.getUri().contains(".wmv")) {
                                if (music.getDuration() >= MusicUtils.getInstance().time[MusicUtils.getInstance().filterNum])
                                    artistDetailAlbumList.add(music);
                            }

                        } else
                            flag = 0;

                    } while (albumCursor.moveToNext());
                }
                albumCursor.close();
            }
        }
    }

    private void readArtist(Context context) {
        Cursor cursor;
        cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.ARTIST);
        int flag = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Music music = new Music();
                    music.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                    music.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));

                    if (artistlist.size() > 0) {
                        for (int i = 0; i < artistlist.size(); i++) {
                            if (music.getArtist().equals(artistlist.get(i).getArtist())) {
                                flag = 1;
                            }
                        }
                    }
                    if (flag == 0) {
                        music.setID(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                        music.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));

                        music.setAlbumArtUri(String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                                , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                        music.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                        music.setAlbum_id(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                        music.setUri(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                        music.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                        if (!music.getUri().contains(".wmv") && !music.getUri().contains(".mkv"))
                            if (music.getDuration() >= MusicUtils.getInstance().time[MusicUtils.getInstance().filterNum])
                                artistlist.add(music);

                    } else
                        flag = 0;

                } while (cursor.moveToNext());
                cursor.close();
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
                        if (artistAdapter != null)
                            artistAdapter.setClickable(true);
                        break;
                    case "SetClickable_False":
                        if (artistAdapter != null)
                            artistAdapter.setClickable(false);
                        break;
                    case "notifyDataSetChanged":
                        if (artistAdapter != null)
                            artistAdapter.notifyDataSetChanged();
                        break;
                    case "restart yourself":
                        reCreateView();
                        if (artistAdapter != null)
                            artistAdapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    };

    private void reCreateView() {
        try {
            Resources.Theme theme = getActivity().getTheme();
            TypedValue appBgValue = new TypedValue();
            TypedValue colorPrimaryValue = new TypedValue();

            theme.resolveAttribute(R.attr.appBg, appBgValue, true);
            theme.resolveAttribute(R.attr.colorPrimary, colorPrimaryValue, true);
            Resources resources = getResources();

            int appBg = ResourcesCompat.getColor(resources,
                    appBgValue.resourceId, null);
            int colorPrimary = ResourcesCompat.getColor(resources,
                    colorPrimaryValue.resourceId, null);
            artistView.setBackgroundColor(appBg);
            MusicList.colorPri = colorPrimary;
            getActivity().findViewById(R.id.toolbar).setBackgroundColor(colorPrimary);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(clickableReceiver);
        super.onDestroyView();
    }

}

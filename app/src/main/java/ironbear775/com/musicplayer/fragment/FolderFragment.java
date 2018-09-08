package ironbear775.com.musicplayer.fragment;

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
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ironbear775.com.musicplayer.activity.MusicList;
import ironbear775.com.musicplayer.adapter.FolderAdapter;
import ironbear775.com.musicplayer.entity.Music;
import ironbear775.com.musicplayer.entity.Playlist;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.util.MusicUtils;
import ironbear775.com.musicplayer.util.MyLinearLayoutManager;

/**
 * Created by ironbear on 2017/5/13.
 */

public class FolderFragment extends android.app.Fragment {
    public static ArrayList<Playlist> playlists = new ArrayList<>();
    public static ArrayList<Music> musicList = new ArrayList<>();
    private FolderAdapter adapter;
    public static final Set<Integer> positionSet = new HashSet<>();
    private FastScrollRecyclerView folderView;
    public static FolderDetailFragment folderDetailFragment;
    private String folderTag;
    private String folder;

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("FolderFragment");
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("FolderFragment");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_layout, container, false);
        findView(view);

        reCreateView();

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("remove");
        filter.addAction("restart yourself");
        getActivity().registerReceiver(clickableReceiver, filter);

        playlists.clear();
        new Thread(readMusicRunnable).start();

        initView();

        if (MusicUtils.getInstance().launchPage == 6) {
            MusicUtils.getInstance().setLaunchPage(getActivity(), MusicUtils.getInstance().FROM_ADAPTER);
        }

        return view;
    }

    private void initView() {
        folderView.setHasFixedSize(true);

        MyLinearLayoutManager linearLayoutManager = new MyLinearLayoutManager(getActivity());
        folderView.setLayoutManager(linearLayoutManager);

        adapter = new FolderAdapter(getActivity(), playlists);
        folderView.setAdapter(adapter);

        adapter.setOnItemClickListener((view, position) -> setClickAction(position));

    }

    private void setClickAction(int position) {
        folderTag = playlists.get(position).getName();
        folder = playlists.get(position).getCount();

        new Thread(readFolderMusicRunnable).start();
    }

    private Runnable readFolderMusicRunnable = new Runnable() {
        @Override
        public void run() {
            readMusic(getActivity());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putString("folder", folderTag);
                    bundle.putString("folderPath", folder);
                    bundle.putParcelableArrayList("musicList", musicList);

                    if (MusicList.folderFragment != null) {
                        transaction.hide(MusicList.folderFragment);
                    }
                    transaction.setCustomAnimations(
                            R.animator.fragment_slide_left_enter,
                            R.animator.fragment_slide_left_exit,
                            R.animator.fragment_slide_right_enter,
                            R.animator.fragment_slide_right_exit);
                    folderDetailFragment = new FolderDetailFragment();
                    folderDetailFragment.setArguments(bundle);
                    transaction.add(R.id.content, folderDetailFragment);
                    transaction.commit();

                }
            });
        }
    };
    private Runnable readMusicRunnable = new Runnable() {
        @Override
        public void run() {
            readFolder(getActivity());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (adapter != null)
                        adapter.notifyDataSetChanged();
                    if (readMusicRunnable != null)
                        readMusicRunnable = null;
                }
            });
        }
    };

    private void readMusic(Context context) {
        musicList.clear();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media.DATA + " LIKE?",
                new String[]{"%" + folderTag + "%"},
                MediaStore.Audio.Media.TITLE);

        if (MusicUtils.getInstance().isFlyme) {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Music music = new Music();
                        String data;
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

                        if (!music.getUri().contains(".wmv") && !music.getUri().contains(".mkv")) {
                            if (music.getDuration() >= MusicUtils.getInstance().time[MusicUtils.getInstance().filterNum]) {

                                data = music.getUri();
                                int p = data.lastIndexOf("/");

                                if ((data.substring(0, p)).equals(folder)) {
                                    musicList.add(music);
                                }

                            }
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        } else {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        if (cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC) == 17) {
                            Music music = new Music();

                            String data;
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

                            if (!music.getUri().contains(".wmv") && !music.getUri().contains(".mkv")) {
                                if (music.getDuration() >= MusicUtils.getInstance().time[MusicUtils.getInstance().filterNum]) {

                                    data = music.getUri();
                                    int p = data.lastIndexOf("/");

                                    if ((data.substring(0, p)).equals(folder)) {
                                        musicList.add(music);
                                    }

                                }
                            }
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }
    }


    private void readFolder(Context context) {
        ArrayList<String> name = new ArrayList<>();
        int flag = 0;
        Cursor cursor;
        cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                    if (duration >= MusicUtils.getInstance().time[MusicUtils.getInstance().filterNum]) {
                        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                        Log.d("TAG", "readFolder: "+data);
                        int p = data.lastIndexOf("/");
                        int s = data.substring(0, p).lastIndexOf("/");

                        if (name.size() > 0) {
                            for (int i = 0; i < name.size(); i++) {
                                if (data.substring(0, p).equals(name.get(i))) {
                                    flag = 1;
                                }
                            }
                        }
                        if (flag == 0) {
                            Playlist list = new Playlist(data.substring(s + 1, p), data.substring(0, p));
                            name.add(data.substring(0, p));
                            playlists.add(list);
                        } else
                            flag = 0;
                    }


                } while (cursor.moveToNext());
                cursor.close();
            }
        }
    }

    private void findView(View view) {
        folderView = view.findViewById(R.id.music_list);
    }

    private final BroadcastReceiver clickableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "SetClickable_True":
                        adapter.setClickable(true);
                        break;
                    case "SetClickable_False":
                        adapter.setClickable(false);
                        break;
                    case "remove":
                        String path = intent.getStringExtra("folderName");
                        for (int i = 0; i < playlists.size(); i++) {
                            if (playlists.get(i).getCount().equals(path)) {
                                playlists.remove(i);
                                adapter.notifyDataSetChanged();
                                break;
                            }
                        }
                        break;
                    case "restart yourself":
                        reCreateView();
                        adapter.notifyDataSetChanged();
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
            folderView.setBackgroundColor(appBg);
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

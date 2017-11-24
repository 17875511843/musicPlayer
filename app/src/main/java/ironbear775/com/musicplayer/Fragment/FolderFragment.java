package ironbear775.com.musicplayer.Fragment;

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

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.FolderAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Class.Playlist;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2017/5/13.
 */

public class FolderFragment extends android.app.Fragment {
    public static ArrayList<Playlist> playlists = new ArrayList<>();
    private FolderAdapter adapter;
    public static final Set<Integer> positionSet = new HashSet<>();
    private FastScrollRecyclerView folderView;
    public static FolderDetailFragment folderDetailFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_layout, container, false);
        findView(view);


        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("remove");
        getActivity().registerReceiver(clickableReceiver, filter);

        playlists.clear();
        readFolder(getActivity());

        initView();

        if (MusicUtils.launchPage == 6) {

            MusicUtils.setLaunchPage(getActivity(),MusicUtils.FROM_ADAPTER);
        }

        return view;
    }

    private void initView() {
        folderView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                getActivity(), LinearLayout.VERTICAL, false);
        folderView.setLayoutManager(layoutManager);

        adapter = new FolderAdapter(getActivity(), playlists);
        folderView.setAdapter(adapter);

        if (playlists.size() > 0) {
            adapter.setOnItemClickListener((view, position) -> setClickAction(position));
        }
    }

    private void setClickAction(int position) {
        String foldertTag = playlists.get(position).getName();
        String folder = playlists.get(position).getCount();

        ArrayList<Music> folderMusicList = new ArrayList<>();
        folderMusicList.clear();
        Cursor cursor = getActivity().getBaseContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media.DATA + " LIKE?",
                new String[]{"%" + foldertTag + "%"},
                MediaStore.Audio.Media.TITLE);

        if (MusicUtils.isFlyme) {
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

                        if (!music.getUri().contains(".wmv")) {
                            if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum]) {

                                data = music.getUri();
                                int p = data.lastIndexOf("/");

                                if ((data.substring(0, p)).equals(folder)) {
                                    folderMusicList.add(music);
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

                            if (!music.getUri().contains(".wmv")) {
                                if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum]) {

                                    data = music.getUri();
                                    int p = data.lastIndexOf("/");

                                    if ((data.substring(0, p)).equals(folder)) {
                                        folderMusicList.add(music);
                                    }

                                }
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
        bundle.putString("folder", foldertTag);
        bundle.putString("folderPath", folder);
        bundle.putParcelableArrayList("musicList", folderMusicList);

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

    private void findView(View view) {
        folderView = view.findViewById(R.id.music_list);

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

                    if (duration >= MusicUtils.time[MusicUtils.filterNum]) {
                        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

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
                            if (playlists.get(i).getCount().equals(path)){
                                playlists.remove(i);
                                adapter.notifyDataSetChanged();
                                break;
                            }
                        }
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

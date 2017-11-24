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

        if (MusicUtils.launchPage == 2) {

            MusicUtils.setLaunchPage(getActivity(), MusicUtils.FROM_ADAPTER);
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

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("artist", artistTag);

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
        MusicUtils.fromWhere = MusicUtils.FROM_ARTIST_PAGE;
    }


    private void findView(View view) {
        artistView = view.findViewById(R.id.music_list);
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

                        if (!music.getUri().contains(".wmv"))
                            if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum])
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
        }
    };

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(clickableReceiver);
        super.onDestroyView();
    }

}

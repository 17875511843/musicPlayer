package ironbear775.com.musicplayer.Fragment;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

import github.nisrulz.recyclerviewhelper.RVHItemTouchHelperCallback;
import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.PlaylistDetaiNewlAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.PlaylistDbHelper;

/**
 * Created by ironbear on 2017/2/4.
 */

public class PlaylistDetailFragment extends Fragment {

    public static ArrayList<Music> musicList = new ArrayList<>();
    public static int count = 0;
    public static int pos = 0;
    public static boolean isChange = false;
    public static String name;
    private RelativeLayout shuffleLayout;
    private PlaylistDbHelper dbHelper;
    private SQLiteDatabase database;
    private boolean isClickable = true;
    private MusicUtils musicUtils;
    private Toolbar toolbar;
    private PlaylistDetaiNewlAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_detail_layout, container, false);
        name = getArguments().getString("name");
        musicList.clear();

        findView(view);

        setHasOptionsMenu(true);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        MusicList.toolbar.setVisibility(View.GONE);
        toolbar.setTitle(getArguments().getString("title"));
        toolbar.setTitleTextColor(Color.WHITE);

        if (!MusicUtils.enableShuffle)
            shuffleLayout.setVisibility(View.GONE);
        else
            shuffleLayout.setVisibility(View.VISIBLE);

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("playlist delete item");
        filter.addAction("playlist swap item");
        filter.addAction("enableShuffle");

        getActivity().registerReceiver(clickableReceiver,filter);

        readList();

        musicUtils = new MusicUtils(getActivity());

        shuffleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicList.size() > 0) {
                    count = 1;
                    MusicListFragment.count = 0;
                    AlbumDetailFragment.count = 0;
                    MusicRecentAddedFragment.count = 0;
                    ArtistDetailFragment.count = 0;
                    musicUtils.shufflePlay(musicList,4);
                }
            }
        });
        return view;
    }

    private void findView(View view) {
        toolbar = (Toolbar) view.findViewById(R.id.playlist_toolbar);
        shuffleLayout = (RelativeLayout) view.findViewById(R.id.shuffle_playlist);
        FastScrollRecyclerView listView = (FastScrollRecyclerView) view.findViewById(R.id.playlist_detail_listView);
        adapter = new PlaylistDetaiNewlAdapter(getActivity().getApplicationContext(), musicList,name);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        listView.setLayoutManager(layoutManager);

        listView.setAdapter(adapter);
        listView.hasFixedSize();

        adapter.setOnItemClickListener(new PlaylistDetaiNewlAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                setClickAction(position);
            }
        });

        ItemTouchHelper.Callback callback = new RVHItemTouchHelperCallback(adapter,true,false,true);
        ItemTouchHelper helper  = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(listView);
    }


    @Override
    public void onCreateOptionsMenu(android.view.Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isChange) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            PlaylistDbHelper dbHelper = new PlaylistDbHelper(getActivity(),
                                    PlaylistDetailFragment.name + ".db", "");
                            SQLiteDatabase database = dbHelper.getWritableDatabase();
                            database.delete(PlaylistDetailFragment.name, null, null);
                            for (int i = 0; i < PlaylistDetailFragment.musicList.size(); i++) {
                                ContentValues values = new ContentValues();

                                values.put("title", PlaylistDetailFragment.musicList.get(i).getTitle());
                                values.put("artist", PlaylistDetailFragment.musicList.get(i).getArtist());
                                values.put("albumArtUri", PlaylistDetailFragment.musicList.get(i).getAlbumArtUri());
                                values.put("album", PlaylistDetailFragment.musicList.get(i).getAlbum());
                                values.put("uri", PlaylistDetailFragment.musicList.get(i).getUri());
                                database.insert(PlaylistDetailFragment.name, null, values);
                            }
                            database.close();
                            PlaylistDetailFragment.isChange = false;
                        }
                    }).start();
                }
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(this);
                transaction.setCustomAnimations(
                        R.animator.fragment_slide_right_enter,
                        R.animator.fragment_slide_right_exit,
                        R.animator.fragment_slide_left_enter,
                        R.animator.fragment_slide_left_exit

                );
                transaction.show(MusicList.playlistFragment);
                transaction.commit();
                getActivity().getWindow().setStatusBarColor(0);
                MusicList.toolbar.setBackgroundColor(0);
                MusicList.toolbar.setVisibility(View.VISIBLE);
                MusicList.toolbar.setTitle(R.string.toolbar_title_playlist);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setClickAction(int position) {
        if (isClickable) {
            int progress = 0;
            FolderDetailFragment.count = 0;
            MusicListFragment.count = 0;
            MusicRecentAddedFragment.count = 0;
            AlbumDetailFragment.count = 0;
            ArtistDetailFragment.count = 0;
            count = 1;
            if (isChange){
                adapter.notifyDataSetChanged();
            }
            musicUtils.startMusic(position, progress,4);

            MusicList.footTitle.setText(musicList.get(position).getTitle());
            MusicList.footArtist.setText(musicList.get(position).getArtist());
            MusicList.PlayOrPause.setImageResource(R.drawable.footpausewhite);

            musicUtils.getFootAlbumArt(position,musicList);

            pos = position;
        }
    }

    private void readList() {

        Cursor cursor;
        String db = "";
        dbHelper = new PlaylistDbHelper(getActivity(), name + ".db", db);
        database = dbHelper.getWritableDatabase();
        cursor = database.query(name, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Music music = new Music();
                music.setTitle( cursor.getString(cursor.getColumnIndex("title")));
                music.setArtist(cursor.getString(cursor.getColumnIndex("artist")));
                music.setAlbumArtUri(cursor.getString(cursor.getColumnIndex("albumArtUri")));
                music.setAlbum(cursor.getString(cursor.getColumnIndex("album")));
                music.setUri(cursor.getString(cursor.getColumnIndex("uri")));
                musicList.add(music);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        dbHelper.close();
    }

    private final BroadcastReceiver clickableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case "SetClickable_True":
                    isClickable = true;
                    shuffleLayout.setClickable(true);
                    break;
                case "SetClickable_False":
                    isClickable = false;
                    shuffleLayout.setClickable(false);
                    break;
                case "playlist swap item":
                    musicList = intent.getParcelableArrayListExtra("new list");
                    isChange = true;
                    break;
                case "enableShuffle":
                    if (!MusicUtils.enableShuffle)
                        shuffleLayout.setVisibility(View.GONE);
                    else
                        shuffleLayout.setVisibility(View.VISIBLE);

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

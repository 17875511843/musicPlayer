package ironbear775.com.musicplayer.Fragment;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

import github.nisrulz.recyclerviewhelper.RVHItemTouchHelperCallback;
import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.PlaylistDetailAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.PlaylistDbHelper;

/**
 * Created by ironbear on 2017/2/4.
 */

public class PlaylistDetailFragment extends Fragment {

    public static ArrayList<Music> musicList = new ArrayList<>();
    public static int pos = 0;
    public static boolean isChange = false;
    public static String name;
    private RelativeLayout shuffleLayout;
    private boolean isClickable = true;
    private Toolbar toolbar;
    private PlaylistDetailAdapter adapter;
    private FastScrollRecyclerView listView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_detail_layout, container, false);

        name = getArguments().getString("name");
        musicList.clear();

        findView(view);

        reCreateView();

        setHasOptionsMenu(true);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        getActivity().sendBroadcast(new Intent("set toolbar gone"));
        toolbar.setTitle(getArguments().getString("title"));
        toolbar.setTitleTextColor(Color.WHITE);

        if (!MusicUtils.getInstance().enableShuffle)
            shuffleLayout.setVisibility(View.GONE);
        else
            shuffleLayout.setVisibility(View.VISIBLE);

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("playlist delete item");
        filter.addAction("playlist swap item");
        filter.addAction("enableShuffle");
        filter.addAction("restart yourself");
        filter.addAction("PLAY_LIST_DETAIL_FRAGMENT_READ_MUSIC_FINISHED");

        getActivity().registerReceiver(clickableReceiver, filter);

        new Thread(readListRunnable).start();

        shuffleLayout.setOnClickListener(v -> {
            if (musicList.size() > 0) {
                MusicUtils.getInstance().playPage = 5;
                MusicUtils.getInstance().shufflePlay(getActivity(),musicList, 4);
            }
        });
        return view;
    }

    private void findView(View view) {
        toolbar = view.findViewById(R.id.playlist_toolbar);
        shuffleLayout = view.findViewById(R.id.shuffle_playlist);
        listView = view.findViewById(R.id.playlist_detail_listView);
        adapter = new PlaylistDetailAdapter(getActivity().getApplicationContext(), musicList, name);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        listView.setLayoutManager(layoutManager);

        listView.setAdapter(adapter);
        listView.hasFixedSize();

        adapter.setOnItemClickListener((view1, position) -> setClickAction(getActivity(),position));

        ItemTouchHelper.Callback callback = new RVHItemTouchHelperCallback(adapter, true, false, true);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
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
                    new Thread(() -> {
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
                    }).start();
                }
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.remove(this);
                transaction.setCustomAnimations(
                        R.animator.fragment_slide_right_enter,
                        R.animator.fragment_slide_right_exit,
                        R.animator.fragment_slide_left_enter,
                        R.animator.fragment_slide_left_exit

                );
                transaction.show(MusicList.playlistFragment);
                transaction.commit();
                getActivity().getWindow().setStatusBarColor(MusicList.colorPri);
                Intent intent = new Intent("set toolbar text");
                intent.putExtra("title", R.string.toolbar_title_playlist);
                getActivity().sendBroadcast(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setClickAction(Context context,int position) {
        if (isClickable) {
            int progress = 0;
            MusicUtils.getInstance().playPage = 5;

            MusicUtils.getInstance().startMusic(context,position, progress, 4);

            Intent intent = new Intent("set footBar");
            Intent intent1 = new Intent("set PlayOrPause");
            intent.putExtra("footTitle", musicList.get(position).getTitle());
            intent.putExtra("footArtist", musicList.get(position).getArtist());
            intent1.putExtra("PlayOrPause", R.drawable.play_to_pause_white_anim);
            getActivity().sendBroadcast(intent);
            getActivity().sendBroadcast(intent1);

            MusicUtils.getInstance().getFootAlbumArt(context,position, musicList, MusicUtils.getInstance().FROM_ADAPTER);

            pos = position;
        }
    }

    private Runnable readListRunnable = new Runnable() {
        @Override
        public void run() {
            readList();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (adapter != null)
                        adapter.notifyDataSetChanged();
                    if (readListRunnable != null)
                        readListRunnable = null;
                }
            });
        }
    };

    private void readList() {

        Cursor cursor;
        String db = "";
        PlaylistDbHelper dbHelper = new PlaylistDbHelper(getActivity(), name + ".db", db);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        cursor = database.query(name, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Music music = new Music();
                music.setTitle(cursor.getString(cursor.getColumnIndex("title")));
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
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "SetClickable_True":
                        isClickable = true;
                        shuffleLayout.setClickable(true);
                        break;
                    case "SetClickable_False":
                        isClickable = false;
                        shuffleLayout.setClickable(false);
                        break;
                    case "playlist swap item":
                        isChange = true;
                        break;
                    case "enableShuffle":
                        if (!MusicUtils.getInstance().enableShuffle)
                            shuffleLayout.setVisibility(View.GONE);
                        else
                            shuffleLayout.setVisibility(View.VISIBLE);

                        break;
                    case "restart yourself":
                        reCreateView();
                        if (adapter != null)
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

            toolbar.setBackgroundColor(colorPrimary);
            shuffleLayout.setBackgroundColor(colorPrimary);
            listView.setBackgroundColor(appBg);

            MusicList.colorPri = colorPrimary;
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

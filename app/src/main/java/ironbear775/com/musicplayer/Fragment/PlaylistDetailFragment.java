package ironbear775.com.musicplayer.Fragment;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.yydcdut.sdlv.DragListView;
import com.yydcdut.sdlv.Menu;
import com.yydcdut.sdlv.MenuItem;
import com.yydcdut.sdlv.SlideAndDragListView;

import java.util.ArrayList;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.PlaylistDetailAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.PlaylistDbHelper;

/**
 * Created by ironbear on 2017/2/4.
 */

public class PlaylistDetailFragment extends Fragment implements View.OnClickListener {

    public static final ArrayList<Music> musicList = new ArrayList<>();
    public static int count = 0;
    public static int pos = 0;
    public static boolean isChange = false;
    public static String name;
    private SlideAndDragListView<Music> listView;
    private RelativeLayout shuffleLayout;
    private PlaylistDetailAdapter adapter;
    private PlaylistDbHelper dbHelper;
    private SQLiteDatabase database;
    private boolean isClickable = true;
    private MusicUtils musicUtils;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_detail_layout, container, false);
        name = getArguments().getString("name");
        musicList.clear();

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        getActivity().registerReceiver(clickableReceiver,filter);

        readList();

        musicUtils = new MusicUtils(getActivity());

        findView(view);
        return view;
    }

    private void findView(View view) {
        shuffleLayout = (RelativeLayout) view.findViewById(R.id.shuffle_playlist);
        listView = (SlideAndDragListView<Music>) view.findViewById(R.id.playlist_detail_listView);
        adapter = new PlaylistDetailAdapter(getActivity(), R.layout.playlist_detail_item_layout, musicList);
        final Menu menu = new Menu(false, false, adapter.getItemViewType(0));
        menu.addItem(new MenuItem.Builder()
                .setWidth(200)
                .setBackground(new ColorDrawable(Color.RED))
                .setText(getResources().getString(R.string.delete))
                .setDirection(MenuItem.DIRECTION_RIGHT)
                .build());
        listView.setMenu(menu);
        listView.setAdapter(adapter);
        shuffleLayout.setOnClickListener(this);
        listView.setOnListItemClickListener(new SlideAndDragListView.OnListItemClickListener() {
            @Override
            public void onListItemClick(View v, int position) {
                setClickAction(position);
            }
        });
        listView.setOnListItemLongClickListener(new SlideAndDragListView.OnListItemLongClickListener() {
            @Override
            public void onListItemLongClick(View view, int position) {
                listView.setOnDragListener(new DragListView.OnDragListener() {
                    int oldPosition;

                    @Override
                    public void onDragViewStart(int position) {
                        oldPosition = position;
                    }

                    @Override
                    public void onDragViewMoving(int position) {

                    }

                    @Override
                    public void onDragViewDown(int position) {
                        if (oldPosition != position)
                            isChange = true;
                    }
                }, musicList);
            }
        });


        listView.setOnMenuItemClickListener(new SlideAndDragListView.OnMenuItemClickListener() {
            @Override
            public int onMenuItemClick(View v, final int itemPosition, int buttonPosition, int direction) {
                switch (direction) {
                    case MenuItem.DIRECTION_RIGHT:
                        switch (buttonPosition) {
                            case 0:
                                final Music music = musicList.get(itemPosition);
                                String db = "";
                                musicList.remove(itemPosition);
                                adapter.notifyDataSetChanged();
                                Snackbar.make(getView(), R.string.delete_from_list, Snackbar.LENGTH_SHORT)
                                        .setDuration(1000)
                                        .show();
                                dbHelper = new PlaylistDbHelper(getActivity(), name + ".db", db);
                                database = dbHelper.getWritableDatabase();
                                database.delete(name, "title = ?", new String[]{music.getTitle()});
                                database.close();
                                dbHelper.close();
                                return Menu.ITEM_SCROLL_BACK;
                        }
                        break;
                    default:
                        return Menu.ITEM_NOTHING;
                }
                return Menu.ITEM_NOTHING;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shuffle_playlist:
                count = 1;
                MusicListFragment.count = 0;
                AlbumDetailFragment.count = 0;
                MusicRecentAddedFragment.count = 0;
                ArtistDetailFragment.count = 0;
                musicUtils.shufflePlay(musicList);
                break;
        }
    }

    private void setClickAction(int position) {
        if (isClickable) {
            int progress = 0;
            MusicListFragment.count = 0;
            MusicRecentAddedFragment.count = 0;
            AlbumDetailFragment.count = 0;
            ArtistDetailFragment.count = 0;
            count = 1;

            musicUtils.startMusic(position,musicList, progress);

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
            }
        }
    };

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(clickableReceiver);
        super.onDestroyView();
    }
}

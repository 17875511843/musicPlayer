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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.AlbumGridAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2017/1/24.
 */

public class AlbumListFragment extends Fragment {
    public static final ArrayList<Music> albumlist = new ArrayList<>();
    public static final ArrayList<Music> albumDetailist = new ArrayList<>();
    public static Fragment detailFragment;
    public static final Set<Integer> positionSet = new HashSet<>();
    private AlbumGridAdapter albumAdapter;
    private com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView albumView;
    private String albumID;
    private String albumTag;

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("AlbumListFragment");
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("AlbumListFragment");
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
        getActivity().registerReceiver(clickableReceiver, filter);

        albumlist.clear();
        new Thread(readAlbumRunnable).start();

        return view;
    }

    private void initView() {
        albumView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(
                getActivity(), 2, GridLayoutManager.VERTICAL, false);
        albumView.setItemAnimator(null);
        albumView.setLayoutManager(layoutManager);
        albumAdapter = new AlbumGridAdapter(getActivity(), albumlist);
        albumView.setAdapter(albumAdapter);

        if (albumlist.size() > 0) {
            albumAdapter.setOnItemClickListener(new AlbumGridAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (MusicList.actionMode != null) {
                        //多选状态
                        MusicUtils.getInstance().addOrRemoveItem(getActivity(), position, positionSet, albumAdapter);
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

        albumID = albumlist.get(position).getAlbum_id();
        albumTag = albumlist.get(position).getAlbum();

        new Thread(readDetailRunnable).start();
    }

    private void readDetailMusic(Context context) {
        albumDetailist.clear();
        Cursor cursor;
        cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media.ALBUM_ID + "=?",
                new String[]{albumID}, MediaStore.Audio.Media.TRACK);

        if (MusicUtils.getInstance().isFlyme) {
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    Music music = new Music();

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
                    music.setTrack(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)));
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK));
                    if (!music.getUri().contains(".wmv")) {
                        if (music.getDuration() >= MusicUtils.getInstance().time[MusicUtils.getInstance().filterNum])
                            albumDetailist.add(music);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        } else {
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    if (cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC) == 17) {
                        Music music = new Music();

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
                        music.setTrack(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)));

                        if (!music.getUri().contains(".wmv")) {
                            if (music.getDuration() >= MusicUtils.getInstance().time[MusicUtils.getInstance().filterNum])
                                albumDetailist.add(music);
                        }
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
    }


    private void findView(View view) {
        albumView = view.findViewById(R.id.music_list);
    }

    private Runnable readAlbumRunnable = new Runnable() {
        @Override
        public void run() {
            readAlbum(getActivity());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initView();

                    if (MusicUtils.getInstance().launchPage == 3) {
                        MusicUtils.getInstance().setLaunchPage(getActivity(), MusicUtils.getInstance().FROM_ADAPTER);
                    }

                    if (albumAdapter != null)
                        albumAdapter.notifyDataSetChanged();
                    if (readAlbumRunnable != null)
                        readAlbumRunnable = null;
                }
            });
        }
    };

    private Runnable readDetailRunnable = new Runnable() {

        @Override
        public void run() {
            readDetailMusic(getActivity());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putString("album", albumTag);
                    bundle.putString("from", "from album");

                    bundle.putParcelableArrayList("musicList", albumDetailist);

                    if (MusicList.albumFragment != null) {
                        transaction.hide(MusicList.albumFragment);
                    }
                    transaction.setCustomAnimations(
                            R.animator.fragment_slide_left_enter,
                            R.animator.fragment_slide_left_exit,
                            R.animator.fragment_slide_right_enter,
                            R.animator.fragment_slide_right_exit);

                    detailFragment = new AlbumDetailFragment();
                    detailFragment.setArguments(bundle);
                    transaction.add(R.id.content, detailFragment);
                    transaction.commit();
                    MusicUtils.getInstance().fromWhere = MusicUtils.getInstance().FROM_ALBUM_PAGE;
                }
            });
        }
    };

    //获取音乐各种信息
    private void readAlbum(Context context) {

        int flag = 0;
        Cursor cursor;
        cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.ALBUM);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Music music = new Music();
                    music.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                    music.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                    music.setAlbum_id(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));

                    if (albumlist.size() > 0) {
                        for (int i = 0; i < albumlist.size(); i++) {
                            if (music.getAlbum_id().equals(albumlist.get(i).getAlbum_id())) {
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
                                albumlist.add(music);

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
                        albumAdapter.setClickable(true);
                        break;
                    case "SetClickable_False":
                        albumAdapter.setClickable(false);
                        break;
                    case "notifyDataSetChanged":
                        albumAdapter.notifyDataSetChanged();
                        break;
                    case "restart yourself":
                        reCreateView();
                        albumAdapter.notifyDataSetChanged();
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

            albumView.setBackgroundColor(appBg);
            getActivity().findViewById(R.id.toolbar).setBackgroundColor(colorPrimary);
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
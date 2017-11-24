package ironbear775.com.musicplayer.Fragment;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.AlbumDetailAdapter;
import ironbear775.com.musicplayer.Adapter.AlbumInArtistAdapter;
import ironbear775.com.musicplayer.Adapter.ArtistDetailAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.SquareImageView;

/**
 * Created by ironbear on 2017/1/26.
 */

public class ArtistDetailFragment extends Fragment {
    public static ArrayList<Music> musicList = new ArrayList<>();
    public static ArrayList<Music> albumList = new ArrayList<>();
    public static boolean CLICK_ALBUMLIST = false;
    public static boolean CLICK_SONGLIST = false;

    public static int count = 0;
    public static final Set<Integer> positionSet = new HashSet<>();
    public static final Set<Integer> albumPositionSet = new HashSet<>();
    public static int pos = 0;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ArtistDetailAdapter songAdapter;
    private AlbumInArtistAdapter albumAdapter;
    private SquareImageView artistImage;
    private RelativeLayout playAll;
    private MusicUtils musicUtils;
    private File appDir;
    public static String artist;
    private TextDrawable drawable;
    public static Fragment detailFragment;
    public static int color;
    public static int playColor;
    private boolean isSongAdapterClickable = true;
    private boolean isAlbumAdapterClickable = true;
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.artist_detail_new_layout, container, false);
        artist = getArguments().getString("artist");
        musicList.clear();
        albumList.clear();

        mContext = getActivity();

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("notifyDataSetChanged");
        filter.addAction("get color");
        filter.addAction("notifyAdapterIsClickable");
        filter.addAction("init songListView");
        filter.addAction("init albumListView");

        getActivity().registerReceiver(clickableReceiver, filter);
        getActivity().sendBroadcast(new Intent("set toolbar gone"));

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String folder = "MusicPlayer/artist";
        appDir = new File(path, folder);

        setHasOptionsMenu(true);

        findView(view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(artist);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);

        musicUtils = new MusicUtils(getActivity());

        ReadMusicAsyncTask readMusicAsyncTask = new ReadMusicAsyncTask();
        ReadAlbumAsyncTask readAlbumAsyncTask = new ReadAlbumAsyncTask();
        readMusicAsyncTask.execute(mContext);
        readAlbumAsyncTask.execute(mContext);

        return view;
    }

    private void loadImage(String newKeyWord) {
        final File file = new File(appDir, newKeyWord);

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getRandomColor();

        drawable = TextDrawable.builder()
                .beginConfig()
                .fontSize(120)
                .endConfig()
                .buildRect(artist, color);
        if (file.exists()) {

            Glide.with(getActivity())
                    .load(file)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            Palette.from(resource).generate(palette -> {
                                Palette.Swatch swatch = palette.getVibrantSwatch();
                                if (swatch != null) {
                                    playAll.setBackgroundColor(swatch.getRgb());
                                    ColorDrawable colorDrawable = new ColorDrawable(swatch.getRgb());
                                    collapsingToolbarLayout.setContentScrim(colorDrawable);
                                    getActivity().getWindow().setStatusBarColor(swatch.getRgb());
                                } else {
                                    swatch = palette.getMutedSwatch();
                                    if (swatch != null) {
                                        playAll.setBackgroundColor(swatch.getRgb());
                                        ColorDrawable colorDrawable = new ColorDrawable(swatch.getRgb());
                                        collapsingToolbarLayout.setContentScrim(colorDrawable);
                                        getActivity().getWindow().setStatusBarColor(swatch.getRgb());
                                    }
                                }
                                Intent intent = new Intent("get color");
                                if (swatch != null) {
                                    intent.putExtra("color", swatch.getRgb());
                                }
                                getActivity().sendBroadcast(intent);
                            });
                        }
                    });

            Glide.with(this)
                    .load(file)
                    .asBitmap()
                    .fitCenter()
                    .placeholder(drawable)
                    .into(artistImage);
        } else {
            musicUtils.setAlbumCoverToAdapter(musicList.get(0),artistImage,MusicUtils.FROM_ADAPTER);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_artist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(this);
                transaction.setCustomAnimations(
                        R.animator.fragment_slide_right_enter,
                        R.animator.fragment_slide_right_exit
                );
                transaction.show(MusicList.artistFragment);
                transaction.commit();

                getActivity().getWindow().setStatusBarColor(0);

                Intent intent = new Intent("set toolbar text");
                intent.putExtra("title",R.string.toolbar_title_artist);
                getActivity().sendBroadcast(intent);

                MusicUtils.fromWhere = MusicUtils.CLEAR;

                break;
            case R.id.re_download_artist:
                MusicUtils.updateArtist(artistImage, getActivity().getApplicationContext(), artist, drawable, getActivity());
                break;
            case R.id.load_local_artist:
                Intent intent1 = new Intent();
                intent1.setType("image/*");
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent1, 1001);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            Uri uri = intent.getData();

            String newKeyWord = artist;
            if (newKeyWord.contains("/"))
                newKeyWord = newKeyWord.replace("/", "_");

            try {
                File file = new File(appDir, newKeyWord);
                if (file.exists()) {
                    file.delete();
                }

                BitmapFactory.Options opt = new BitmapFactory.Options();

                opt.inPreferredConfig = Bitmap.Config.RGB_565;

                Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uri),
                        null, opt);

                if (bitmap != null) {

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(file);
                        if (bitmap.getByteCount() > 3000000) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, fos);
                        } else if (bitmap.getByteCount() > 2500000) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, fos);
                        } else if (bitmap.getByteCount() > 2000000) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                        } else if (bitmap.getByteCount() < 1500000) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos);
                        } else if (bitmap.getByteCount() < 1000000) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                        }
                        fos.flush();

                        loadImage(newKeyWord);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (fos != null) {
                                fos.close();
                            }
                            if (!bitmap.isRecycled()) {
                                bitmap.recycle();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void findView(View view) {
        toolbar = view.findViewById(R.id.artist_toolbar);
        collapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar);
        playAll = view.findViewById(R.id.artist_play_all);
        artistImage = view.findViewById(R.id.artist_detail_iv);

        artistImage.setTag(R.id.artist_url, artist);

        artistImage.setOnLongClickListener(view1 -> {

            Intent intent1 = new Intent();
            intent1.setType("image/*");
            intent1.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent1, 1001);
            return false;
        });

        playAll.setOnClickListener(v -> {
            FolderDetailFragment.count = 0;
            MusicListFragment.count = 0;
            MusicRecentAddedFragment.count = 0;
            AlbumDetailFragment.count = 0;
            PlaylistDetailFragment.count = 0;
            MusicList.count = 0;
            count = 1;

            musicUtils = new MusicUtils(v.getContext());
            musicUtils.playAll(musicList, 2);
            pos = 0;
        });
    }

    private void setAlbumOnClickAction(int position) {
        count = 1;
        String albumId = albumList.get(position).getAlbum_id();
        String albumTag = albumList.get(position).getAlbum();
        ArrayList<Music> albumMusicList = new ArrayList<>();
        Cursor cursor;
        cursor = getActivity().getBaseContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Audio.Media.ALBUM_ID+ "=? "
                , new String[]{albumId}, MediaStore.Audio.Media.TRACK);

        if (MusicUtils.isFlyme) {
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
                        if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum])
                            albumMusicList.add(music);
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
                            if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum])
                                albumMusicList.add(music);
                        }
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("album", albumTag);
        bundle.putParcelableArrayList("musicList", albumMusicList);

        if (ArtistListFragment.artistDetailFragment != null) {
            transaction.hide(ArtistListFragment.artistDetailFragment);
        }
        transaction.setCustomAnimations(
                R.animator.fragment_slide_left_enter,
                R.animator.fragment_slide_left_exit

        );
        detailFragment = new AlbumDetailFragment();
        detailFragment.setArguments(bundle);
        transaction.add(R.id.content, detailFragment);
        transaction.commit();
        MusicUtils.fromWhere = MusicUtils.FROM_ARTIST_DETAIl_PAGE;
    }

    private void setClickAction(int position) {

        FolderDetailFragment.count = 0;
        MusicListFragment.count = 0;
        MusicRecentAddedFragment.count = 0;
        AlbumDetailFragment.count = 0;
        PlaylistDetailFragment.count = 0;
        MusicList.count = 0;
        count = 1;

        musicUtils = new MusicUtils(getActivity());
        musicUtils.startMusic(position, 0, 2);

        Intent intent = new Intent("set footBar");
        Intent intent1 = new Intent("set PlayOrPause");
        intent.putExtra("footTitle",musicList.get(position).getTitle());
        intent.putExtra("footArtist",musicList.get(position).getArtist());
        intent1.putExtra("PlayOrPause",R.drawable.footplaywhite);
        getActivity().sendBroadcast(intent);
        getActivity().sendBroadcast(intent1);

        musicUtils.getFootAlbumArt(position, musicList,MusicUtils.FROM_ADAPTER);

        pos = position;
    }

    private final BroadcastReceiver clickableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "SetClickable_True":
                        songAdapter.setClickable(true);
                        playAll.setClickable(true);
                        break;
                    case "SetClickable_False":
                        songAdapter.setClickable(false);
                        playAll.setClickable(false);
                        break;
                    case "notifyDataSetChanged":
                        songAdapter.notifyDataSetChanged();
                        albumAdapter.notifyDataSetChanged();
                        if (musicList.size() == 0 || albumList.size() == 0) {
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction transaction;

                            transaction = fragmentManager.beginTransaction();
                            transaction.hide(ArtistListFragment.artistDetailFragment);
                            transaction.setCustomAnimations(
                                    R.animator.fragment_slide_right_enter,
                                    R.animator.fragment_slide_right_exit

                            );
                            transaction.show(MusicList.artistFragment);
                            transaction.commit();
                            ArtistListFragment.artistDetailFragment = null;
                            Intent intent1 = new Intent("set toolbar text");
                            intent1.putExtra("title",R.string.toolbar_title_artist);
                            getActivity().sendBroadcast(intent1);
                        }
                        break;
                    case "get color":
                        playColor = intent.getIntExtra("color", 0);
                        break;
                    case "notifyAdapterIsClickable":
                        isAlbumAdapterClickable = true;
                        isSongAdapterClickable = true;
                        break;
                    case "init songListView":
                        String newKeyWord;
                        if (artist != null && artist.contains("/")) {
                            newKeyWord = artist.replace("/", "_");
                        } else {
                            newKeyWord = artist;
                        }

                        loadImage(newKeyWord);

                        FastScrollRecyclerView songListView = getView().findViewById(R.id.artist_detail_list);
                        songListView.setHasFixedSize(true);

                        songListView.setNestedScrollingEnabled(false);

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

                        songListView.setLayoutManager(linearLayoutManager);
                        songAdapter = new ArtistDetailAdapter(getActivity(), musicList);
                        songListView.setAdapter(songAdapter);

                        songAdapter.setOnItemClickListener(new AlbumDetailAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                if (MusicList.actionMode != null) {
                                    //多选状态
                                    if (isSongAdapterClickable) {
                                        musicUtils.addOrRemoveItem(position, positionSet, songAdapter);
                                    }
                                } else {
                                    setClickAction(position);
                                }
                            }

                            @Override
                            public void onItemLongClick(View view, int position) {
                                if (MusicList.actionMode == null) {
                                    isAlbumAdapterClickable = false;
                                    CLICK_ALBUMLIST = false;
                                    CLICK_SONGLIST = true;
                                    Intent intent = new Intent("ActionModeChanged");
                                    intent.putExtra("from", "from artist adapter");
                                    getActivity().sendBroadcast(intent);
                                }
                            }
                        });
                        break;
                    case "init albumListView":

                        RecyclerView albumListView = getView().findViewById(R.id.album_in_artist_view);

                        albumListView.setNestedScrollingEnabled(false);

                        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(
                                getActivity(), 1, GridLayoutManager.HORIZONTAL, false);
                        albumListView.setItemAnimator(null);
                        albumListView.setLayoutManager(layoutManager);
                        albumAdapter = new AlbumInArtistAdapter(getActivity(), albumList);
                        albumListView.setAdapter(albumAdapter);

                        albumAdapter.setOnItemClickListener(new AlbumInArtistAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                if (MusicList.actionMode != null) {
                                    //多选状态
                                    if (isAlbumAdapterClickable) {
                                        musicUtils.addOrRemoveItem(position, albumPositionSet, albumAdapter);
                                    }
                                } else {
                                    setAlbumOnClickAction(position);
                                }
                            }

                            @Override
                            public void onItemLongClick(View view, int position) {
                                if (MusicList.actionMode == null) {
                                    isSongAdapterClickable = false;
                                    CLICK_ALBUMLIST = true;
                                    CLICK_SONGLIST = false;
                                    Intent intent = new Intent("ActionModeChanged");
                                    intent.putExtra("from", "from album in artist");
                                    getActivity().sendBroadcast(intent);
                                }
                            }
                        });
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

    private static ArrayList<Music> readArtist(Context context,int type){
        int flag = 0;
        ArrayList<Music> arrayList = new ArrayList<>();
        if (type == 1) {
            Cursor albumCursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Audio.Media.ARTIST + "=?",
                    new String[]{artist},
                    MediaStore.Audio.Media.ALBUM);
            if (MusicUtils.isFlyme) {
                if (albumCursor != null) {
                    if (albumCursor.moveToFirst()) {
                        do {
                            Music music = new Music();
                            music.setAlbum(albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));

                            if (arrayList.size() > 0) {
                                for (int i = 0; i < arrayList.size(); i++) {
                                    if (music.getAlbum().equals(arrayList.get(i).getAlbum()))
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
                                    if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum])
                                        arrayList.add(music);
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

                            if (arrayList.size() > 0) {
                                for (int i = 0; i < arrayList.size(); i++) {
                                    if (music.getAlbum().equals(arrayList.get(i).getAlbum()))
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
                                    if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum])
                                        arrayList.add(music);
                                }

                            } else
                                flag = 0;

                        } while (albumCursor.moveToNext());
                    }
                    albumCursor.close();
                }
            }
        }else if (type == 2){
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Audio.Media.ARTIST + "=?",
                    new String[]{artist},
                    MediaStore.Audio.Media.TITLE);
            if (MusicUtils.isFlyme){
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            Music music = new Music();

                            music.setAlbumArtUri(String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                                    , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                            music.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                            music.setAlbum_id(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                            music.setUri(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                            music.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                            music.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                            music.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                            if (!music.getUri().contains(".wmv")) {
                                if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum])
                                    arrayList.add(music);
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

                                music.setAlbumArtUri(String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                                        , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                                music.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                                music.setAlbum_id(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                                music.setUri(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                                music.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                                music.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                                music.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                                if (!music.getUri().contains(".wmv")) {
                                    if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum])
                                        arrayList.add(music);
                                }
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
            }
        }
        return arrayList;
    }

    static class ReadMusicAsyncTask extends AsyncTask<Context,Nullable,Nullable>{

        @Override
        protected Nullable doInBackground(Context... contexts) {
            musicList = readArtist(contexts[0],1);
            contexts[0].sendBroadcast(new Intent("init songListView"));
            return null;
        }
    }


    static class ReadAlbumAsyncTask extends AsyncTask<Context,Nullable,Nullable>{

        @Override
        protected Nullable doInBackground(Context... contexts) {
            albumList = readArtist(contexts[0],2);
            contexts[0].sendBroadcast(new Intent("init albumListView"));
            return null;
        }
    }
}

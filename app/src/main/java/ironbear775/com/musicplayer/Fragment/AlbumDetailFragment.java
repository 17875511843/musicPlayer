package ironbear775.com.musicplayer.Fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.AlbumDetailAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.SquareImageView;

/**
 * Created by ironbear on 2017/1/25.
 */

public class AlbumDetailFragment extends Fragment {
    public static int pos = 0;
    public static int count = 0;
    public static final Set<Integer> positionSet = new HashSet<>();
    public static ArrayList<Music> musicList = new ArrayList<>();
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private RelativeLayout playAll;
    private SquareImageView albumArt;
    private AlbumDetailAdapter albumAdapter;
    private MusicUtils musicUtils;
    public static int id;
    public static int playColor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.album_detail_layout, container, false);

        id = this.getId();

        musicList = getArguments().getParcelableArrayList("musicList");
        String album = getArguments().getString("album");
        setHasOptionsMenu(true);

        findView(view);

        musicUtils = new MusicUtils(getActivity());

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(album);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
        getActivity().sendBroadcast(new Intent("set toolbar gone"));

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("notifyDataSetChanged");
        filter.addAction("hide albumDetailFragment");
        filter.addAction("hide fragment on switch");
        filter.addAction("get color from album");
        getActivity().registerReceiver(clickableReceiver, filter);

        Glide.with(getActivity())
                .load(musicList.get(0).getAlbumArtUri())
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
                                Intent intent1 = new Intent("set toolbar color");
                                intent1.putExtra("color", swatch.getRgb());
                                getActivity().sendBroadcast(intent1);
                                getActivity().getWindow().setStatusBarColor(swatch.getRgb());
                            } else {
                                swatch = palette.getMutedSwatch();
                                if (swatch != null) {
                                    playAll.setBackgroundColor(swatch.getRgb());
                                    ColorDrawable colorDrawable = new ColorDrawable(swatch.getRgb());
                                    collapsingToolbarLayout.setContentScrim(colorDrawable);

                                    Intent intent1 = new Intent("set toolbar color");
                                    intent1.putExtra("color", swatch.getRgb());
                                    getActivity().sendBroadcast(intent1);
                                    getActivity().getWindow().setStatusBarColor(swatch.getRgb());
                                }
                            }
                            Intent intent = new Intent("get color from album");
                            if (swatch != null) {
                                intent.putExtra("color", swatch.getRgb());
                            }
                            getActivity().sendBroadcast(intent);
                        });
                    }
                });

        albumArt.setTag(R.id.item_url, musicList.get(0).getTitle() + musicList.get(0).getArtist());
        musicUtils.setAlbumCoverToAdapter(musicList.get(0), albumArt, MusicUtils.FROM_ADAPTER);

        return view;
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
                        R.animator.fragment_slide_right_exit,
                        R.animator.fragment_slide_left_enter,
                        R.animator.fragment_slide_left_exit
                );

                if (MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_DETAIl_PAGE) {

                    transaction.show(ArtistListFragment.artistDetailFragment);
                    transaction.commit();
                    getActivity().getWindow().setStatusBarColor(ArtistDetailFragment.playColor);
                    ArtistDetailFragment.color = 0;
                    MusicUtils.fromWhere = MusicUtils.FROM_ARTIST_PAGE;

                }

                if (MusicUtils.fromWhere == MusicUtils.FROM_ALBUM_PAGE) {
                    transaction.show(MusicList.albumFragment);
                    transaction.commit();
                    getActivity().getWindow().setStatusBarColor(0);
                    Intent intent1 = new Intent("set toolbar text");
                    intent1.putExtra("title", R.string.toolbar_title_album);
                    getActivity().sendBroadcast(intent1);
                    MusicUtils.fromWhere = MusicUtils.CLEAR;
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findView(View view) {
        toolbar = view.findViewById(R.id.album_toolbar);
        collapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar);
        playAll = view.findViewById(R.id.album_play_all);
        albumArt = view.findViewById(R.id.album_detail_art);
        FastScrollRecyclerView albumListView = view.findViewById(R.id.album_detail_list);
        albumListView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        albumListView.setLayoutManager(linearLayoutManager);

        albumArt.setOnLongClickListener(view1 -> {
            try {
                Mp3File mp3File = new Mp3File(musicList.get(0).getUri());
                if (!mp3File.hasId3v2Tag()
                        || (mp3File.hasId3v2Tag()
                        && mp3File.getId3v2Tag().getAlbumImage() == null)) {
                    Intent intent1 = new Intent();
                    intent1.setType("image/*");
                    intent1.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent1, 1011);
                }

            } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                e.printStackTrace();
            }
            return false;
        });
        albumAdapter = new AlbumDetailAdapter(getActivity(), musicList);
        albumListView.setAdapter(albumAdapter);

        albumAdapter.setOnItemClickListener(new AlbumDetailAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (MusicList.actionMode != null) {
                    //多选状态
                    musicUtils.addOrRemoveItem(position, positionSet, albumAdapter);
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

        playAll.setOnClickListener(v -> {
            FolderDetailFragment.count = 0;
            MusicListFragment.count = 0;
            MusicRecentAddedFragment.count = 0;
            ArtistDetailFragment.count = 0;
            PlaylistDetailFragment.count = 0;
            MusicList.count = 0;
            count = 1;

            musicUtils = new MusicUtils(v.getContext());
            musicUtils.playAll(musicList, 3);

        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 1011 && resultCode == Activity.RESULT_OK) {

            Uri uri = intent.getData();
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    "MusicPlayer/album");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            String newAlbumTitle, newSinger;
            String album = musicList.get(0).getAlbum();
            String artist = musicList.get(0).getArtist();

            newAlbumTitle = album;

            if (album != null && album.contains("/")) {
                newAlbumTitle = album.replace("/", "_");
            }

            newSinger = artist;

            if (artist.contains("/")) {
                newSinger = artist.replace("/", "_");
            }

            try {
                File albumFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                        "MusicPlayer/album/" + newAlbumTitle + "_" + newSinger);

                if (albumFile.exists()) {
                    albumFile.delete();
                }

                BitmapFactory.Options opt = new BitmapFactory.Options();

                opt.inPreferredConfig = Bitmap.Config.RGB_565;

                Bitmap bitmap = null;
                if (uri != null) {
                    bitmap = BitmapFactory.decodeStream(getActivity().
                                    getContentResolver().openInputStream(uri),
                            null, opt);
                }

                if (bitmap != null) {

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(albumFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
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

                            Glide.with(this)
                                    .load(albumFile)
                                    .into(albumArt);
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

    private void setClickAction(int position) {

        FolderDetailFragment.count = 0;
        MusicListFragment.count = 0;
        MusicRecentAddedFragment.count = 0;
        ArtistDetailFragment.count = 0;
        PlaylistDetailFragment.count = 0;
        MusicList.count = 0;
        count = 1;

        int progress = 0;

        musicUtils = new MusicUtils(getActivity());
        musicUtils.startMusic(position, progress, 3);


        Intent intent = new Intent("set footBar");
        Intent intent1 = new Intent("set PlayOrPause");
        intent.putExtra("footTitle", musicList.get(position).getTitle());
        intent.putExtra("footArtist", musicList.get(position).getArtist());
        intent1.putExtra("PlayOrPause", R.drawable.footplaywhite);
        getActivity().sendBroadcast(intent);
        getActivity().sendBroadcast(intent1);

        musicUtils.getFootAlbumArt(position, musicList, MusicUtils.FROM_ADAPTER);
        pos = position;
    }

    private final BroadcastReceiver clickableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "SetClickable_True":
                        albumAdapter.setClickable(true);
                        playAll.setClickable(true);
                        break;
                    case "SetClickable_False":
                        albumAdapter.setClickable(false);
                        playAll.setClickable(false);
                        break;
                    case "notifyDataSetChanged":
                        albumAdapter.notifyDataSetChanged();
                        if (musicList.size() == 0) {
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction transaction;

                            transaction = fragmentManager.beginTransaction();
                            transaction.hide(AlbumListFragment.detailFragment);
                            transaction.setCustomAnimations(
                                    R.animator.fragment_slide_right_enter,
                                    R.animator.fragment_slide_right_exit,
                                    R.animator.fragment_slide_left_enter,
                                    R.animator.fragment_slide_left_exit
                            );
                            transaction.show(MusicList.albumFragment);
                            transaction.commit();
                            AlbumListFragment.detailFragment = null;
                            Intent intent1 = new Intent("set toolbar text");
                            intent1.putExtra("title", R.string.toolbar_title_album);
                            getActivity().sendBroadcast(intent1);
                        }
                        break;
                    case "hide albumDetailFragment":
                        getActivity().getWindow().setStatusBarColor(ArtistDetailFragment.playColor);
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.hide(getFragmentManager().findFragmentById(id));
                        transaction.setCustomAnimations(
                                R.animator.fragment_slide_right_enter,
                                R.animator.fragment_slide_right_exit,
                                R.animator.fragment_slide_left_enter,
                                R.animator.fragment_slide_left_exit

                        );
                        transaction.show(ArtistListFragment.artistDetailFragment);
                        transaction.commit();
                        MusicUtils.fromWhere = MusicUtils.FROM_ARTIST_PAGE;
                        break;
                    case "hide fragment on switch":
                        FragmentManager fragmentManager1 = getFragmentManager();
                        FragmentTransaction transaction1 = fragmentManager1.beginTransaction();
                        transaction1.hide(getFragmentManager().findFragmentById(id));
                        transaction1.commit();
                        break;
                    case "get color from album":
                        playColor = intent.getIntExtra("color", 0);
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

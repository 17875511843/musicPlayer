package ironbear775.com.musicplayer.Fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.umeng.analytics.MobclickAgent;

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
    public static final Set<Integer> positionSet = new HashSet<>();
    public static ArrayList<Music> musicList;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private RelativeLayout playAll;
    private SquareImageView albumArt;
    private AlbumDetailAdapter albumAdapter;
    public static int id;
    public static int playColor;
    private FastScrollRecyclerView albumListView;

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("AlbumDetailFragment");
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("AlbumDetailFragment");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.album_detail_layout, container, false);

        id = this.getId();

        musicList = getArguments().getParcelableArrayList("musicList");
        String album = getArguments().getString("album");
        setHasOptionsMenu(true);

        findView(view);
        reCreateView();

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
        filter.addAction("restart yourself");
        getActivity().registerReceiver(clickableReceiver, filter);

        Glide.with(getActivity())
                .asBitmap()
                .load(musicList.get(0).getAlbumArtUri())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
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
        MusicUtils.getInstance().setAlbumCoverToAdapter(getActivity(),musicList.get(0),
                albumArt, MusicUtils.getInstance().FROM_ADAPTER);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.remove(this);
                transaction.setCustomAnimations(
                        R.animator.fragment_slide_right_enter,
                        R.animator.fragment_slide_right_exit,
                        R.animator.fragment_slide_left_enter,
                        R.animator.fragment_slide_left_exit
                );

                if (MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_DETAIl_PAGE) {

                    MusicUtils.getInstance().fromWhere = MusicUtils.getInstance().FROM_ARTIST_PAGE;
                    transaction.show(ArtistListFragment.artistDetailFragment);
                    transaction.commit();
                    getActivity().getWindow().setStatusBarColor(ArtistDetailFragment.playColor);
                    ArtistDetailFragment.color = 0;

                } else if (MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ALBUM_PAGE) {
                    transaction.show(MusicList.albumFragment);
                    transaction.commit();
                    getActivity().getWindow().setStatusBarColor(MusicList.colorPri);
                    Intent intent1 = new Intent("set toolbar text");
                    intent1.putExtra("title", R.string.toolbar_title_album);
                    getActivity().sendBroadcast(intent1);
                    MusicUtils.getInstance().fromWhere = MusicUtils.getInstance().CLEAR;
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
        albumListView = view.findViewById(R.id.album_detail_list);
        albumListView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        albumListView.setLayoutManager(linearLayoutManager);

        albumArt.setOnLongClickListener(view1 -> {
            Intent intent1 = new Intent();
            intent1.setType("image/*");
            intent1.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent1, 1011);
            return false;
        });
        albumAdapter = new AlbumDetailAdapter(getActivity(), musicList);
        albumListView.setAdapter(albumAdapter);

        albumAdapter.setOnItemClickListener(new AlbumDetailAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (MusicList.actionMode != null) {
                    //多选状态
                    MusicUtils.getInstance().addOrRemoveItem(getActivity(),position, positionSet, albumAdapter);
                } else {
                    setClickAction(getActivity(),position);
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
            MusicUtils.getInstance().playPage = 4;

            MusicUtils.getInstance().playAll(getActivity(),musicList, 3);

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

    private void setClickAction(Context context,int position) {

        MusicUtils.getInstance().playPage = 4;

        int progress = 0;

        MusicUtils.getInstance().startMusic(context,position, progress, 3);


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
                        transaction.remove(getFragmentManager().findFragmentById(id));
                        transaction.setCustomAnimations(
                                R.animator.fragment_slide_right_enter,
                                R.animator.fragment_slide_right_exit,
                                R.animator.fragment_slide_left_enter,
                                R.animator.fragment_slide_left_exit

                        );
                        transaction.show(ArtistListFragment.artistDetailFragment);
                        transaction.commit();
                        MusicUtils.getInstance().fromWhere = MusicUtils.getInstance().FROM_ARTIST_PAGE;
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

            theme.resolveAttribute(R.attr.appBg, appBgValue, true);
            Resources resources = getResources();

            int appBg = ResourcesCompat.getColor(resources,
                    appBgValue.resourceId, null);

            albumListView.setBackgroundColor(appBg);


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

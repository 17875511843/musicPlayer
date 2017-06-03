package ironbear775.com.musicplayer.Fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.album_detail_layout, container, false);

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

        MusicList.toolbar.setVisibility(View.GONE);
        //MusicList.toolbar.setTitle(getArguments().getString("album"));

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("notifyDataSetChanged");
        getActivity().registerReceiver(clickableReceiver, filter);



        Glide.with(getActivity())
                .load(musicList.get(0).getAlbumArtUri())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                Palette.Swatch swatch = palette.getVibrantSwatch();
                                if (swatch != null) {
                                    playAll.setBackgroundColor(swatch.getRgb());
                                    ColorDrawable colorDrawable = new ColorDrawable(swatch.getRgb());
                                    collapsingToolbarLayout.setContentScrim(colorDrawable);
                                    MusicList.toolbar.setBackgroundColor(swatch.getRgb());
                                    getActivity().getWindow().setStatusBarColor(swatch.getRgb());
                                } else {
                                    swatch = palette.getMutedSwatch();
                                    if (swatch != null) {
                                        playAll.setBackgroundColor(swatch.getRgb());
                                        ColorDrawable colorDrawable = new ColorDrawable(swatch.getRgb());
                                        collapsingToolbarLayout.setContentScrim(colorDrawable);
                                        MusicList.toolbar.setBackgroundColor(swatch.getRgb());
                                        getActivity().getWindow().setStatusBarColor(swatch.getRgb());
                                    }
                                }
                            }
                        });
                    }
                });

        Glide.with(getActivity())
                .load(musicList.get(0).getAlbumArtUri())
                .asBitmap()
                .placeholder(R.drawable.default_album_art)
                .into(albumArt);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent in = new Intent("notifyDataSetChanged");

        switch (item.getItemId()) {
            case android.R.id.home:

                if (MusicList.actionMode != null) {
                    MusicList.actionMode = null;
                    AlbumDetailFragment.positionSet.clear();
                    getActivity().getWindow().setStatusBarColor(0);
                    MusicList.toolbar.setBackgroundColor(0);
                    getActivity().sendBroadcast(in);
                } else {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.hide(this);
                    transaction.setCustomAnimations(
                            R.animator.fragment_slide_right_enter,
                            R.animator.fragment_slide_right_exit,
                            R.animator.fragment_slide_left_enter,
                            R.animator.fragment_slide_left_exit

                    );
                    transaction.show(MusicList.albumFragment);
                    transaction.commit();
                    getActivity().getWindow().setStatusBarColor(0);
                    MusicList.toolbar.setBackgroundColor(0);
                    MusicList.toolbar.setVisibility(View.VISIBLE);
                    MusicList.toolbar.setTitle(R.string.toolbar_title_album);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findView(View view) {
        toolbar = (Toolbar) view.findViewById(R.id.album_toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        playAll = (RelativeLayout) view.findViewById(R.id.album_play_all);
        albumArt = (SquareImageView) view.findViewById(R.id.album_detail_art);
        FastScrollRecyclerView albumListView = (FastScrollRecyclerView) view.findViewById(R.id.album_detail_list);
        albumListView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        albumListView.setLayoutManager(linearLayoutManager);

        albumAdapter = new AlbumDetailAdapter(getActivity(), musicList);
        albumListView.setAdapter(albumAdapter);

        albumAdapter.setOnItemClickListener(new AlbumDetailAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (MusicList.actionMode != null) {
                    //多选状态
                    musicUtils.addOrRemoveItem(position,positionSet,albumAdapter);
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

        playAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicListFragment.count = 0;
                MusicRecentAddedFragment.count = 0;
                ArtistDetailFragment.count = 0;
                count = 1;
                musicUtils = new MusicUtils(v.getContext());
                musicUtils.playAll(musicList);

            }
        });
    }

    private void setClickAction(int position) {

        MusicListFragment.count = 0;
        MusicRecentAddedFragment.count = 0;
        ArtistDetailFragment.count = 0;
        PlaylistDetailFragment.count = 0;
        count = 1;
        int progress = 0;

        musicUtils = new MusicUtils(getActivity());
        musicUtils.startMusic(position,musicList, progress);

        MusicList.footTitle.setText(musicList.get(position).getTitle());
        MusicList.footArtist.setText(musicList.get(position).getArtist());
        MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);

        musicUtils.getFootAlbumArt(position,musicList);
        pos = position;
    }

    private final BroadcastReceiver clickableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
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
                    if (musicList.size() == 0){
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
                        MusicList.toolbar.setVisibility(View.VISIBLE);
                        MusicList.toolbar.setTitle(R.string.toolbar_title_album);
                    }
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

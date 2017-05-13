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
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
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
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.io.File;
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
 * Created by ironbear on 2017/1/26.
 */

public class ArtistDetailFragment extends Fragment {
    public static ArrayList<Music> musicList = new ArrayList<>();
    public static int count = 0;
    public static final Set<Integer> positionSet = new HashSet<>();
    public static int pos = 0;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AlbumDetailAdapter songAdapter;
    private SquareImageView artistImage;
    private RelativeLayout playAll;
    private MusicUtils musicUtils;
    private File appDir;
    private String artist;
    private TextDrawable drawable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.artist_detail_new_layout, container, false);

        musicList = getArguments().getParcelableArrayList("musicList");
        artist = getArguments().getString("artist");
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String folder = "MusicPlayer/artist";
        appDir = new File(path, folder);

        setHasOptionsMenu(true);
        findView(view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(artist);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);

        MusicList.toolbar.setVisibility(View.GONE);
        // MusicList.toolbar.setTitle(artist);

        musicUtils = new MusicUtils(getActivity());

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("notifyDataSetChanged");
        getActivity().registerReceiver(clickableReceiver, filter);

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getRandomColor();

        String newKeyWord;
        if (artist != null && artist.contains("/")) {
            newKeyWord = artist.replace("/", "_");
        } else {
            newKeyWord = artist;
        }

        final File file = new File(appDir, newKeyWord);

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
                            Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
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
                                }
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
            Glide.with(this)
                    .load(musicList.get(0).getAlbumArtUri())
                    .asBitmap()
                    .fitCenter()
                    .placeholder(drawable)
                    .into(artistImage);
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_artist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent in = new Intent("notifyDataSetChanged");

        switch (item.getItemId()) {
            case android.R.id.home:

                if (MusicList.actionMode != null) {
                    MusicList.actionMode = null;
                    ArtistDetailFragment.positionSet.clear();
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
                    transaction.show(MusicList.artistFragment);
                    transaction.commit();

                    getActivity().getWindow().setStatusBarColor(0);
                    MusicList.toolbar.setBackgroundColor(0);
                    MusicList.toolbar.setVisibility(View.VISIBLE);
                    MusicList.toolbar.setTitle(R.string.toolbar_title_artist);
                }
                break;
            case R.id.re_download_artist:
                MusicUtils.updateArtist(artistImage, getActivity().getApplicationContext(), artist, drawable, getActivity());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findView(View view) {
        toolbar = (Toolbar) view.findViewById(R.id.artist_toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        playAll = (RelativeLayout) view.findViewById(R.id.artist_play_all);
        artistImage = (SquareImageView) view.findViewById(R.id.artist_detail_iv);
        FastScrollRecyclerView songListView = (FastScrollRecyclerView) view.findViewById(R.id.artist_detail_list);
        songListView.setHasFixedSize(true);

        artistImage.setTag(R.id.artist_url, artist);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        songListView.setLayoutManager(linearLayoutManager);
        songListView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .color(Color.parseColor("#22616161"))
                .sizeResId(R.dimen.divider)
                .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                .build());
        songAdapter = new AlbumDetailAdapter(getActivity(), musicList);
        songListView.setAdapter(songAdapter);

        songAdapter.setOnItemClickListener(new AlbumDetailAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (MusicList.actionMode != null) {
                    //多选状态
                    musicUtils.addOrRemoveItem(position, positionSet, songAdapter);
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
                AlbumDetailFragment.count = 0;
                count = 1;
                musicUtils = new MusicUtils(v.getContext());
                musicUtils.playAll(musicList);
                pos = 0;
            }
        });
    }

    private void setClickAction(int position) {

        MusicListFragment.count = 0;
        MusicRecentAddedFragment.count = 0;
        AlbumDetailFragment.count = 0;
        PlaylistDetailFragment.count = 0;
        count = 1;
        musicUtils = new MusicUtils(getActivity());
        musicUtils.startMusic(position, musicList, 0);
        MusicList.footTitle.setText(musicList.get(position).getTitle());
        MusicList.footArtist.setText(musicList.get(position).getArtist());
        MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);
        musicUtils.getFootAlbumArt(position, musicList);

        pos = position;
    }

    private final BroadcastReceiver clickableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
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

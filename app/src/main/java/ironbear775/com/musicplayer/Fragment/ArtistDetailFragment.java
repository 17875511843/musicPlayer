package ironbear775.com.musicplayer.Fragment;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
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

/**
 * Created by ironbear on 2017/1/26.
 */

public class ArtistDetailFragment extends Fragment {
    public static ArrayList<Music> musicList = new ArrayList<>();
    public static int count = 0;
    public static final Set<Integer> positionSet = new HashSet<>();
    public static int pos = 0;
    private AlbumDetailAdapter songAdapter;
    private ImageView artistImage;
    private RelativeLayout playAll;
    private MusicUtils musicUtils;
    private File appDir;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.artist_detail_layout, container, false);

        musicList = getArguments().getParcelableArrayList("musicList");
        String artist = getArguments().getString("artist");
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String folder = "MusicPlayer";
        appDir = new File(path, folder);

        MusicList.toolbar.setTitle(artist);

        musicUtils = new MusicUtils(getActivity());

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("notifyDataSetChanged");
        getActivity().registerReceiver(clickableReceiver, filter);

        findView(view);
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getRandomColor();

        String newKeyWord;
        if (artist != null && artist.contains("/")) {
            newKeyWord = artist.replace("/", "_");
        }else {
            newKeyWord = artist;
        }

        final File file = new File(appDir, newKeyWord);

        TextDrawable drawable = TextDrawable.builder()
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
                                    } else {
                                        swatch = palette.getMutedSwatch();
                                        if (swatch != null) {
                                            playAll.setBackgroundColor(swatch.getRgb());
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
            artistImage.setImageDrawable(drawable);
        }
        return view;
    }

    private void findView(View view) {
        playAll = (RelativeLayout) view.findViewById(R.id.artist_play_all);
        artistImage = (ImageView) view.findViewById(R.id.artist_detail_iv);
        RecyclerView songListView = (RecyclerView) view.findViewById(R.id.artist_detail_list);
        songListView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

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
                    musicUtils.addOrRemoveItem(position,positionSet,songAdapter,false);
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
        musicUtils.startMusic(position,musicList,0);
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

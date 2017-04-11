package ironbear775.com.musicplayer.Fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.AlbumDetailAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2017/1/25.
 */

public class AlbumDetailFragment extends Fragment {
    public static int count = 0;
    public static final Set<Integer> positionSet = new HashSet<>();
    public static ArrayList<Music> musicList = new ArrayList<>();
    private RelativeLayout playAll;
    private ImageView albumArt;
    private AlbumDetailAdapter albumAdapter;
    private MusicUtils musicUtils;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.album_detail_layout, container, false);
        musicList = getArguments().getParcelableArrayList("musicList");

        musicUtils = new MusicUtils(getActivity());

        MusicList.toolbar.setTitle(getArguments().getString("album"));

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("notifyDataSetChanged");
        getActivity().registerReceiver(clickableReceiver, filter);

        findView(view);

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

        Glide.with(getActivity())
                .load(musicList.get(0).getAlbumArtUri())
                .asBitmap()
                .placeholder(R.drawable.default_album_art)
                .into(albumArt);

        return view;
    }

    private void findView(View view) {
        playAll = (RelativeLayout) view.findViewById(R.id.album_play_all);
        albumArt = (ImageView) view.findViewById(R.id.album_detail_art);
        FastScrollRecyclerView albumListView = (FastScrollRecyclerView) view.findViewById(R.id.album_detail_list);
        albumListView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        albumListView.setLayoutManager(linearLayoutManager);

        albumListView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .color(Color.parseColor("#22616161"))
                .sizeResId(R.dimen.divider)
                .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                .build());
        albumAdapter = new AlbumDetailAdapter(getActivity(), musicList);
        albumListView.setAdapter(albumAdapter);

        albumAdapter.setOnItemClickListener(new AlbumDetailAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (MusicList.actionMode != null) {
                    //多选状态
                    musicUtils.addOrRemoveItem(position,positionSet,albumAdapter,false);
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

        musicUtils.startMusic(position,musicList, progress);

        MusicList.footTitle.setText(musicList.get(position).getTitle());
        MusicList.footArtist.setText(musicList.get(position).getArtist());
        MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);

        musicUtils.getFootAlbumArt(position,musicList);

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

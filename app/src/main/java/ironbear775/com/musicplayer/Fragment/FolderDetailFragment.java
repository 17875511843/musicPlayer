package ironbear775.com.musicplayer.Fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.MusicAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2017/5/13.
 */

public class FolderDetailFragment extends Fragment {
    public static int count = 0;
    public static final Set<Integer> positionSet = new HashSet<>();
    public static ArrayList<Music> musicList = new ArrayList<>();
    public static int pos = 0;
    private MusicAdapter musicAdapter;
    private com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView musicView;
    private MusicUtils musicUtils;
    private Toolbar toolbar;
    private RelativeLayout shuffle;
    private String folderPath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shuffle_item_layout, container, false);

        musicList.clear();
        musicList = getArguments().getParcelableArrayList("musicList");
        String folderTitle = getArguments().getString("folder");
        folderPath = getArguments().getString("folderPath");

        findView(view);

        setHasOptionsMenu(true);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getActivity().sendBroadcast(new Intent("set toolbar gone"));
        toolbar.setTitle(folderTitle);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.close_white);

        if (!MusicUtils.enableShuffle)
            shuffle.setVisibility(View.GONE);
        else
            shuffle.setVisibility(View.VISIBLE);

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("notifyDataSetChanged");
        filter.addAction("enableShuffle");

        getActivity().registerReceiver(clickableReceiver, filter);

        musicUtils = new MusicUtils(getActivity());

        initView();

        return view;
    }

    private void initView() {
        musicView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        musicView.setLayoutManager(layoutManager);

        musicAdapter = new MusicAdapter(getActivity(), musicList);
        musicView.setAdapter(musicAdapter);

        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (MusicList.actionMode != null) {
                    //多选状态
                    musicUtils.addOrRemoveItem(position, positionSet, musicAdapter);
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

        shuffle.setOnClickListener(v -> {
            count = 1;
            MusicListFragment.count = 0;
            MusicRecentAddedFragment.count = 0;
            AlbumDetailFragment.count = 0;
            ArtistDetailFragment.count = 0;
            PlaylistDetailFragment.count = 0;
            MusicList.count = 0;

            musicUtils = new MusicUtils(v.getContext());
            musicUtils.shufflePlay(musicList,5);
        });
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent in = new Intent("notifyDataSetChanged");

        switch (item.getItemId()) {
            case android.R.id.home:
                if (MusicList.actionMode != null) {
                    MusicList.actionMode = null;
                    FolderDetailFragment.positionSet.clear();
                    getActivity().getWindow().setStatusBarColor(0);
                    getActivity().sendBroadcast(new Intent("set toolbar clear"));
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
                    transaction.show(MusicList.folderFragment);
                    transaction.commit();

                    getActivity().getWindow().setStatusBarColor(0);
                    Intent intent = new Intent("set toolbar text");
                    intent.putExtra("title",R.string.toolbar_title_folder);
                    getActivity().sendBroadcast(intent);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setClickAction(int position) {

        count = 1;
        MusicListFragment.count = 0;
        MusicRecentAddedFragment.count = 0;
        AlbumDetailFragment.count = 0;
        ArtistDetailFragment.count = 0;
        PlaylistDetailFragment.count = 0;
        MusicList.count = 0;

        int progress = 0;
        musicUtils = new MusicUtils(getActivity());
        musicUtils.startMusic(position, progress,5);

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

    private void findView(View view) {
        shuffle = view.findViewById(R.id.shuffle);
        toolbar = view.findViewById(R.id.music_toolbar);
        musicView = view.findViewById(R.id.music_list);
    }

    private final BroadcastReceiver clickableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "SetClickable_True":
                        musicAdapter.setClickable(true);
                        break;
                    case "SetClickable_False":
                        musicAdapter.setClickable(false);
                        break;
                    case "notifyDataSetChanged":
                        musicAdapter.notifyDataSetChanged();
                        if (musicList.size() == 0){

                            Intent remove = new Intent("remove");
                            remove.putExtra("folderName",folderPath);

                            getActivity().sendBroadcast(remove);

                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction transaction;

                            transaction = fragmentManager.beginTransaction();
                            if (FolderFragment.folderDetailFragment != null)
                                transaction.hide(FolderFragment.folderDetailFragment);
                            transaction.setCustomAnimations(
                                    R.animator.fragment_slide_right_enter,
                                    R.animator.fragment_slide_right_exit,
                                    R.animator.fragment_slide_left_enter,
                                    R.animator.fragment_slide_left_exit
                            );
                            transaction.show(MusicList.folderFragment);
                            transaction.commit();
                            FolderFragment.folderDetailFragment = null;
                            Intent intent1 = new Intent("set toolbar text");
                            intent1.putExtra("title",R.string.toolbar_title_folder);
                            getActivity().sendBroadcast(intent1);

                        }
                        break;
                    case "enableShuffle":
                        if (!MusicUtils.enableShuffle)
                            shuffle.setVisibility(View.GONE);
                        else
                            shuffle.setVisibility(View.VISIBLE);

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

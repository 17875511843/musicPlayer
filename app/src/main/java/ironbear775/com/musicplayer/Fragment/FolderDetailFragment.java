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

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

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
    private String folderTitle;
    private String folderPath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shuffle_item_layout, container, false);

        musicList.clear();
        musicList = getArguments().getParcelableArrayList("musicList");
        folderTitle = getArguments().getString("folder");
        folderPath = getArguments().getString("folderPath");

        findView(view);

        setHasOptionsMenu(true);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MusicList.toolbar.setVisibility(View.GONE);
        toolbar.setTitle(folderTitle);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.close_white);

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("notifyDataSetChanged");
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

        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = 1;
                MusicListFragment.count = 0;
                MusicRecentAddedFragment.count = 0;
                AlbumDetailFragment.count = 0;
                ArtistDetailFragment.count = 0;
                PlaylistDetailFragment.count = 0;
                musicUtils = new MusicUtils(v.getContext());
                musicUtils.shufflePlay(musicList);
            }
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
                    transaction.show(MusicList.folderFragment);
                    transaction.commit();

                    getActivity().getWindow().setStatusBarColor(0);
                    MusicList.toolbar.setBackgroundColor(0);
                    MusicList.toolbar.setVisibility(View.VISIBLE);
                    MusicList.toolbar.setTitle(R.string.toolbar_title_folder);
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

        int progress = 0;
        musicUtils = new MusicUtils(getActivity());
        musicUtils.startMusic(position, musicList, progress);

        MusicList.footTitle.setText(musicList.get(position).getTitle());
        MusicList.footArtist.setText(musicList.get(position).getArtist());
        MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);

        musicUtils.getFootAlbumArt(position, musicList);

        pos = position;

    }

    private void findView(View view) {
        shuffle = (RelativeLayout) view.findViewById(R.id.shuffle);
        toolbar = (Toolbar) view.findViewById(R.id.music_toolbar);
        musicView = (FastScrollRecyclerView) view.findViewById(R.id.music_list);
    }

    private final BroadcastReceiver clickableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
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

                        MusicList.myContext.sendBroadcast(remove);

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
                        MusicList.toolbar.setVisibility(View.VISIBLE);
                        MusicList.toolbar.setTitle(R.string.toolbar_title_folder);

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

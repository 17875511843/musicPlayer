package ironbear775.com.musicplayer.Fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.MusicAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.MyLinearLayoutManager;

/**
 * Created by ironbear on 2017/5/13.
 */

public class FolderDetailFragment extends Fragment {
    public static final Set<Integer> positionSet = new HashSet<>();
    public static ArrayList<Music> musicList = new ArrayList<>();
    public static int pos = 0;
    private MusicAdapter musicAdapter;
    private com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView musicView;
    private Toolbar toolbar;
    private RelativeLayout shuffle;
    private String folderPath;

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("FolderDetailFragment");
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("FolderDetailFragment");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shuffle_list_layout, container, false);

        musicList = getArguments().getParcelableArrayList("musicList");
        String folderTitle = getArguments().getString("folder");
        folderPath = getArguments().getString("folderPath");

        findView(view);

        reCreateView();

        setHasOptionsMenu(true);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getActivity().sendBroadcast(new Intent("set toolbar gone"));
        toolbar.setTitle(folderTitle);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.close_white);

        if (!MusicUtils.getInstance().enableShuffle)
            shuffle.setVisibility(View.GONE);
        else
            shuffle.setVisibility(View.VISIBLE);

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("notifyDataSetChanged");
        filter.addAction("enableShuffle");
        filter.addAction("restart yourself");

        getActivity().registerReceiver(clickableReceiver, filter);

        initView();

        return view;
    }

    private void initView() {
        musicView.setHasFixedSize(true);
        MyLinearLayoutManager linearLayoutManager = new MyLinearLayoutManager(getActivity());
         musicView.setLayoutManager(linearLayoutManager);

        musicAdapter = new MusicAdapter(getActivity(), musicList);
        musicView.setAdapter(musicAdapter);

        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (MusicList.actionMode != null) {
                    //多选状态
                    MusicUtils.getInstance().addOrRemoveItem(getActivity(),position, positionSet, musicAdapter);
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

        shuffle.setOnClickListener(v -> {
            MusicUtils.getInstance().playPage = 6;

            MusicUtils.getInstance().shufflePlay(getActivity(),musicList, 5);
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
                    getActivity().getWindow().setStatusBarColor(MusicList.colorPri);
                    getActivity().sendBroadcast(new Intent("set toolbar clear"));
                    getActivity().sendBroadcast(in);
                } else {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.remove(this);
                    transaction.setCustomAnimations(
                            R.animator.fragment_slide_right_enter,
                            R.animator.fragment_slide_right_exit,
                            R.animator.fragment_slide_left_enter,
                            R.animator.fragment_slide_left_exit
                    );
                    transaction.show(MusicList.folderFragment);
                    transaction.commit();

                    getActivity().getWindow().setStatusBarColor(MusicList.colorPri);
                    Intent intent = new Intent("set toolbar text");
                    intent.putExtra("title", R.string.toolbar_title_folder);
                    getActivity().sendBroadcast(intent);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setClickAction(Context context,int position) {

        MusicUtils.getInstance().playPage = 6;

        int progress = 0;

        MusicUtils.getInstance().startMusic(context,position, progress, 5);

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
                        if (musicList.size() == 0) {

                            Intent remove = new Intent("remove");
                            remove.putExtra("folderName", folderPath);

                            getActivity().sendBroadcast(remove);

                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction transaction;

                            transaction = fragmentManager.beginTransaction();
                            if (FolderFragment.folderDetailFragment != null)
                                transaction.remove(FolderFragment.folderDetailFragment);
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
                            intent1.putExtra("title", R.string.toolbar_title_folder);
                            getActivity().sendBroadcast(intent1);

                        }
                        break;
                    case "enableShuffle":
                        if (!MusicUtils.getInstance().enableShuffle)
                            shuffle.setVisibility(View.GONE);
                        else
                            shuffle.setVisibility(View.VISIBLE);

                        break;
                    case "restart yourself":
                        reCreateView();
                        musicAdapter.notifyDataSetChanged();
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

            toolbar.setBackgroundColor(colorPrimary);
            shuffle.setBackgroundColor(colorPrimary);
            musicView.setBackgroundColor(appBg);

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

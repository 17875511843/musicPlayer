package ironbear775.com.musicplayer.util;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import github.nisrulz.recyclerviewhelper.RVHItemTouchHelperCallback;
import ironbear775.com.musicplayer.adapter.OnPlayingListAdapter;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.service.MusicService;

/**
 * Created by ironb on 2017/11/25.
 */

public class OnPlayingListDialog extends Dialog {
    private Context mContext;
    private RecyclerView onPlayingListView;
    private OnPlayingListAdapter onPlayingListAdapter;
    private OnPlayingReceiver receiver;
    private boolean isChanged = false;
    public OnPlayingListDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onplaying_list_dialog_layout);

        receiver = new OnPlayingReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("update onPlayingList");
        intentFilter.addAction("list changed");

        mContext.registerReceiver(receiver, intentFilter);

        onPlayingListView = findViewById(R.id.on_playing_list_view);
        onPlayingListAdapter = new OnPlayingListAdapter(mContext, MusicService.onPlayingList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                mContext, LinearLayoutManager.VERTICAL, false);
        onPlayingListView.setLayoutManager(layoutManager);

        onPlayingListView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mContext)
                .color(mContext.getResources().getColor(R.color.divider_trans)).build());
        onPlayingListView.setAdapter(onPlayingListAdapter);
        onPlayingListView.hasFixedSize();

        onPlayingListAdapter.setOnItemClickListener((view, position) -> setClickAction(mContext,position));

        ItemTouchHelper.Callback callback = new RVHItemTouchHelperCallback(
                onPlayingListAdapter, true, false, true);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(onPlayingListView);

        onPlayingListView.scrollToPosition(MusicService.musicPosition);
    }

    private void setClickAction(Context context,int position) {
        MusicUtils.getInstance().startMusic(context,position,0,11);
        mContext.sendBroadcast(new Intent("update_cover"));
        mContext.sendBroadcast(new Intent("set lyric from service"));
    }

    private class OnPlayingReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null ){

                switch (action){
                    case "update onPlayingList":
                        onPlayingListView.scrollToPosition(MusicService.musicPosition);
                        onPlayingListAdapter.notifyDataSetChanged();
                        break;
                    case "list changed":
                        isChanged = true;
                        break;
                }
            }
        }
    }

    @Override
    protected void onStop() {
        if (isChanged) {
            if (MusicService.isRandom)
                MusicUtils.getInstance().saveShuffleArray(mContext, MusicService.onPlayingList);
            else
                MusicUtils.getInstance().saveArray(mContext, MusicService.onPlayingList);
        }
        mContext.unregisterReceiver(receiver);
        super.onStop();
    }
}

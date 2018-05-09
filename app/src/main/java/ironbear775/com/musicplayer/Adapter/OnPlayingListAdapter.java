package ironbear775.com.musicplayer.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;
import ironbear775.com.musicplayer.Activity.TagEditActivity;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;
import ironbear775.com.musicplayer.Util.DetailDialog;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.PlaylistDialog;

/**
 * Created by ironb on 2017/11/25.
 */

public class OnPlayingListAdapter extends RecyclerView.Adapter<OnPlayingListAdapter.OnPlayingListViewHolder>
        implements RVHAdapter {
    private final LayoutInflater mInflater;
    private final ArrayList<Music> mList;
    private boolean isClickable = true;
    private Context mContext;

    public OnPlayingListAdapter(Context context, ArrayList<Music> list) {
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public OnPlayingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.onplaying_item_layout, parent, false);

        return new OnPlayingListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnPlayingListViewHolder holder, int position) {
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> {
                if (isClickable) {
                    mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            });
        }

        holder.menu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(mContext, holder.menu);
            popupMenu.inflate(R.menu.menu_onplay);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menu_add:
                        Set<Integer> listPositionSet = new HashSet<>();
                        listPositionSet.add(position);
                        PlaylistDialog dialog = new PlaylistDialog(mContext, listPositionSet, mList);
                        dialog.show();
                        break;
                    case R.id.play_next:

                        Music music = mList.get(position);
                        if (position!=MusicService.musicPosition)
                            mList.remove(position);
                        if (position < MusicService.musicPosition)
                            MusicService.musicPosition--;
                        mList.add(MusicService.musicPosition + 1, music);
                        notifyDataSetChanged();
                        mContext.sendBroadcast(new Intent("list changed"));
                        break;
                    case R.id.remove_from_queue:
                        remove(position);
                        break;
                    case R.id.menu_detail:
                        DetailDialog detailDialog = new DetailDialog(mContext, mList, position);
                        detailDialog.show();
                        break;
                    case R.id.tag_edit:
                        if (mList.get(position).getUri().contains(".mp3")
                                || mList.get(position).getUri().contains(".MP3")) {
                            Intent intent = new Intent(mContext, TagEditActivity.class);
                            intent.putExtra("music", (Parcelable) mList.get(position));
                            mContext.startActivity(intent);
                        } else {
                            Toast.makeText(mContext, R.string.open_failed, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        if (mList.get(position).getUri().equals(
                MusicService.music.getUri()))
            holder.itemView.setBackgroundResource(R.color.items_selected_bg_color);
        else
            holder.itemView.setBackground(null);

        holder.num.setText(String.valueOf(position));
        holder.title.setText(mList.get(position).getTitle());
        holder.others.setText(mList.get(position).getArtist());

    }

    @Override
    public void onBindViewHolder(@NonNull OnPlayingListViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (!payloads.isEmpty()) {
            if (payloads.get(0) instanceof Integer) {
                holder.num.setText(String.valueOf(payloads.get(0)));
                if (!mList.get(position).getUri().equals(
                        MusicService.music.getUri()))
                    holder.itemView.setBackground(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        swap(fromPosition, toPosition);
        return false;
    }

    private void swap(int fromPosition, int toPosition) {
        if (mList.get(fromPosition).getUri().equals(
                MusicService.music.getUri()))
            MusicService.musicPosition = toPosition;
        if (mList.get(toPosition).getUri().equals(
                MusicService.music.getUri()))
            MusicService.musicPosition = fromPosition;

        notifyItemChanged(fromPosition, toPosition);
        notifyItemChanged(toPosition, fromPosition);
        Collections.swap(mList, fromPosition, toPosition);

        notifyItemMoved(fromPosition, toPosition);
        mContext.sendBroadcast(new Intent("list changed"));

    }

    private void remove(int position) {
        mList.remove(position);
        if (position < MusicService.musicPosition)
            MusicService.musicPosition--;
        notifyDataSetChanged();

        mContext.sendBroadcast(new Intent("list changed"));

        if (position == MusicService.musicPosition){
            MusicUtils.getInstance().startMusic(mContext,MusicService.musicPosition,0,10);
            mContext.sendBroadcast(new Intent("update_cover"));
            mContext.sendBroadcast(new Intent("set lyric from service"));
        }
    }

    @Override
    public void onItemDismiss(int position, int direction) {
        remove(position);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private PlaylistDetailAdapter.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(PlaylistDetailAdapter.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setClickable(boolean click) {
        this.isClickable = click;
    }


    public class OnPlayingListViewHolder extends RecyclerView.ViewHolder implements RVHViewHolder {
        TextView num, title, others;
        ImageView menu;

        OnPlayingListViewHolder(View itemView) {
            super(itemView);
            num = itemView.findViewById(R.id.on_playing_num);
            title = itemView.findViewById(R.id.on_playing_title);
            others = itemView.findViewById(R.id.on_playing_others);
            menu = itemView.findViewById(R.id.on_playing_menu);
        }

        @Override
        public void onItemSelected(int actionstate) {

        }

        @Override
        public void onItemClear() {

        }
    }
}


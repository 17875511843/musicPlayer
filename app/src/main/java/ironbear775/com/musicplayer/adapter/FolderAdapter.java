package ironbear775.com.musicplayer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

import ironbear775.com.musicplayer.activity.BaseActivity;
import ironbear775.com.musicplayer.activity.MusicList;
import ironbear775.com.musicplayer.entity.Playlist;
import ironbear775.com.musicplayer.fragment.FolderFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.util.MusicUtils;

/**
 * Created by ironbear on 2017/5/13.
 */

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder>{
    private final LayoutInflater mInflater;
    private final ArrayList<Playlist> mList;
    private boolean isClickable = true;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private FolderAdapter.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(FolderAdapter.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setClickable(boolean click) {
        this.isClickable = click;
    }

    public FolderAdapter(Context context, ArrayList<Playlist> list){
        mList = list;
        this.mInflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.folder_item_layout, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        holder.setData(position);
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> {
                if (isClickable) {
                    mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            });
        }

        holder.tv_title.setText(mList.get(position).getName());
        holder.tv_count.setText(mList.get(position).getCount());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class FolderViewHolder extends RecyclerView.ViewHolder {
        final TextView tv_title;
        final TextView tv_count;

        FolderViewHolder(View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.folder_tv_title);
            tv_count = itemView.findViewById(R.id.folder_tv_count);
        }

        public void setData(int position) {
            Set<Integer> positionSet = FolderFragment.positionSet;
            if (MusicUtils.getInstance().isSelectAll){
                positionSet = MusicList.listPositionSet;
            }

            if (BaseActivity.isNight) {
                tv_title.setTextColor(itemView.getResources().getColor(
                        R.color.nightMainTextColor));
            }else {
                tv_title.setTextColor(itemView.getResources().getColor(
                        R.color.lightMainTextColor));
            }

            if (positionSet.contains(position)) {
                itemView.setBackgroundResource(R.color.items_selected_bg_color);
            } else {
                itemView.setBackground(null);
            }
        }
    }
}


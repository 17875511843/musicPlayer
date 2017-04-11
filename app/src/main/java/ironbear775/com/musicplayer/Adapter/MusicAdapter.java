package ironbear775.com.musicplayer.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Fragment.MusicListFragment;
import ironbear775.com.musicplayer.Fragment.MusicRecentAddedFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.DetailDialog;
import ironbear775.com.musicplayer.Util.PlaylistDialog;

/**
 * Created by ironbear on 2017/1/15.
 */

public class MusicAdapter extends RecyclerView.Adapter<MusicViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter{
    private static final int TYPE_HEADER = 0;  //说明是带有Header的
    private static final int TYPE_NORMAL = 1;  //说明是不带有header和footer的
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final ArrayList<Music> mList;
    private View mHeaderView;
    private boolean isClickable = true;

    @NonNull
    @Override
    public String getSectionName(int position) {
        if (position >= 1) {
            return mList.get(position - 1).getTitle().substring(0, 1).toUpperCase(Locale.ENGLISH);
        }else
            return "#";
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    public void setClickable(boolean click) {
        this.isClickable = click;
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null) {
            return TYPE_NORMAL;
        }
        if (position == 0) {
            //第一个item应该加载Header
            return TYPE_HEADER;
        }
        return TYPE_NORMAL;
    }

    public MusicAdapter(Context context, ArrayList<Music> list) {
        this.mList = list;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (mHeaderView != null && viewType == TYPE_HEADER) {
            return new MusicViewHolder(mHeaderView);
        }
        View view = mInflater.inflate(R.layout.item_layout, parent, false);

        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MusicViewHolder holder, final int position) {
        if (position > 0) {
            holder.setData(position);
        }
        if (mOnItemClickListener != null) {


            final int layoutPosition = holder.getLayoutPosition();
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isClickable) {
                        mOnItemClickListener.onItemClick(holder.itemView, layoutPosition);
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(holder.itemView, layoutPosition);
                    return false;
                }
            });
        }
        if (getItemViewType(position) == TYPE_NORMAL) {
            if (holder != null) {
                holder.item_menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(mContext,holder.item_menu);
                        popupMenu.inflate(R.menu.new_menu);
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()){
                                    case R.id.menu_add:
                                        Set<Integer> listPositionSet = new HashSet<>();
                                        switch (MusicList.Mod){
                                            case 1:
                                                listPositionSet = MusicListFragment.positionSet;
                                                break;
                                            case 5:
                                                listPositionSet = MusicRecentAddedFragment.positionSet;
                                                break;
                                        }
                                        listPositionSet.add(position);
                                        PlaylistDialog dialog = new PlaylistDialog(mContext,listPositionSet, mList);
                                        dialog.show();
                                        break;
                                    case R.id.menu_delete:
                                        Log.d("tag",mList.get(position-1).getUri());
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                                        alertDialog.setTitle(R.string.delete_alert_title);
                                        alertDialog.setMessage(mList.get(position-1).getTitle());
                                        alertDialog.setCancelable(true);
                                        alertDialog.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                        alertDialog.setPositiveButton(R.string.delete_confrim, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String uri = mList.get(position-1).getUri();
                                                mContext.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                        MediaStore.Audio.Media.DATA + "=?",
                                                        new String[]{uri});
                                                File file = new File(uri);
                                                if (file.isFile()){
                                                    if (file.delete()) {
                                                        Intent intent = new Intent("notifyDataSetChanged");
                                                        switch (MusicList.Mod){
                                                            case 1:
                                                                MusicListFragment.musicList.remove(position-1);
                                                                break;
                                                            case 5:
                                                                MusicRecentAddedFragment.musicList.remove(position-1);
                                                                break;
                                                        }
                                                        mContext.sendBroadcast(intent);
                                                        Snackbar.make(MusicList.PlayOrPause, R.string.success, Snackbar.LENGTH_SHORT)
                                                                .setDuration(1000)
                                                                .show();
                                                    }else {
                                                        Snackbar.make(MusicList.PlayOrPause, R.string.failed, Snackbar.LENGTH_SHORT)
                                                                .setDuration(1000)
                                                                .show();
                                                    }
                                                }
                                            }
                                        });
                                        alertDialog.show();
                                        break;
                                    case R.id.menu_detail:
                                        DetailDialog detailDialog = new DetailDialog(mContext,mList,position-1);
                                        detailDialog.show();
                                        break;
                                }
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });
                //这里加载数据的时候要注意，是从position-1开始，因为position==0已经被header占用了
                holder.tv_title.setText(mList.get(position-1 ).getTitle());
                holder.tv_others.setText(mList.get(position-1 ).getArtist());
                String albumArtUri = mList.get(position-1 ).getAlbumArtUri();

                Glide.with(mContext)
                        .load(albumArtUri)
                        .placeholder(R.drawable.default_album_art)
                        .into(holder.iv);
            }
        } else if (getItemViewType(position) == TYPE_HEADER) {
        }
    }

    @Override
    public int getItemCount() {
        if (mHeaderView == null) {
            return mList.size();
        } else {
            return mList.size() + 1;
        }
    }

}

class MusicViewHolder extends RecyclerView.ViewHolder   {
    final ImageView iv;
    final TextView tv_title;
    final TextView tv_others;
    final ImageView item_menu;

    MusicViewHolder(final View itemView) {
        super(itemView);
        iv = (ImageView) itemView.findViewById(R.id.iv_image);
        tv_others = (TextView) itemView.findViewById(R.id.tv_others);
        tv_title = (TextView) itemView.findViewById(R.id.tv_title);
        item_menu = (ImageView) itemView.findViewById(R.id.item_menu);
    }

    public void setData(int position){
        Set<Integer> positionSet = new HashSet<>();
        switch (MusicList.Mod){
            case 1:
                positionSet = MusicListFragment.positionSet;
                break;
            case 5:
                positionSet = MusicRecentAddedFragment.positionSet;
        }
        if (positionSet.contains(position)){
            itemView.setBackgroundResource(R.color.items_selected_bg_color);
        }else {
            itemView.setBackgroundResource(R.color.listView_bg_color);
        }
    }
}




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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Fragment.AlbumDetailFragment;
import ironbear775.com.musicplayer.Fragment.AlbumListFragment;
import ironbear775.com.musicplayer.Fragment.ArtistDetailFragment;
import ironbear775.com.musicplayer.Fragment.ArtistListFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.DetailDialog;
import ironbear775.com.musicplayer.Util.PlaylistDialog;

/**
 * Created by ironbear on 2017/1/15.
 */

public class AlbumDetailAdapter extends RecyclerView.Adapter<AlbumDetailViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final ArrayList<Music> mList;
    private boolean isClickable = true;
    private final SimpleDateFormat time = new SimpleDateFormat("m:ss");

    @NonNull
    @Override
    public String getSectionName(int position) {
        return mList.get(position).getTitle().substring(0, 1).toUpperCase(Locale.ENGLISH);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setClickable(boolean click) {
        this.isClickable = click;
    }

    public AlbumDetailAdapter(Context context, ArrayList<Music> list) {
        this.mList = list;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public AlbumDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.detail_layout, parent, false);
        return new AlbumDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AlbumDetailViewHolder holder, final int position) {
        holder.setData(position);
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isClickable) {
                        mOnItemClickListener.onItemClick(holder.itemView, position);
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(holder.itemView, position);
                    return false;
                }
            });
        }
        String albumArtUri = mList.get(position).getAlbumArtUri();
        Glide.with(mContext)
                .load(albumArtUri)
                .placeholder(R.drawable.default_album_art)
                .into(holder.iv);
        holder.tv_title.setText(mList.get(position).getTitle());
        holder.tv_time.setText(time.format(mList.get(position).getDuration()));

        holder.item_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(mContext, holder.item_menu);
                popupMenu.inflate(R.menu.new_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_add:
                                Set<Integer> listPositionSet = new HashSet<>();
                                switch (MusicList.Mod) {
                                    case 2:
                                        listPositionSet = ArtistDetailFragment.positionSet;
                                        break;
                                    case 3:
                                        listPositionSet = AlbumDetailFragment.positionSet;
                                        break;
                                }
                                listPositionSet.add(position);
                                PlaylistDialog dialog = new PlaylistDialog(mContext, listPositionSet, mList);
                                dialog.show();
                                break;
                            case R.id.menu_delete:
                                Log.d("tag", mList.get(position).getUri());
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                                alertDialog.setTitle(R.string.delete_alert_title);
                                alertDialog.setMessage(mList.get(position).getTitle());
                                alertDialog.setCancelable(true);
                                alertDialog.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                alertDialog.setPositiveButton(R.string.delete_confrim, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String uri = mList.get(position).getUri();
                                        String artist = mList.get(position).getArtist();
                                        int pos = 0;
                                        mContext.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                MediaStore.Audio.Media.DATA + "=?",
                                                new String[]{uri});
                                        File file = new File(uri);
                                        if (file.isFile()) {
                                            if (file.delete()) {
                                                Intent intent = new Intent("notifyDataSetChanged");
                                                switch (MusicList.Mod) {
                                                    case 2:
                                                        if (ArtistDetailFragment.musicList.size() == 1){
                                                            for (int i = 0; i< ArtistListFragment.artistlist.size(); i++){
                                                                if (ArtistListFragment.artistlist.get(i).getArtist().equals(artist)){
                                                                    pos = i;
                                                                    break;
                                                                }
                                                            }
                                                            ArtistListFragment.artistlist.remove(pos);
                                                        }
                                                        ArtistDetailFragment.musicList.remove(position);
                                                        break;
                                                    case 3:
                                                        if (AlbumDetailFragment.musicList.size() == 1){
                                                            for (int i = 0; i< AlbumListFragment.albumlist.size(); i++){
                                                                if ( AlbumListFragment.albumlist.get(i).getArtist().equals(artist)){
                                                                    pos = i;
                                                                    break;
                                                                }
                                                            }
                                                            AlbumListFragment.albumlist.remove(pos);
                                                        }
                                                        AlbumDetailFragment.musicList.remove(position);
                                                        break;
                                                }
                                                mContext.sendBroadcast(intent);
                                                Snackbar.make(MusicList.PlayOrPause, R.string.success, Snackbar.LENGTH_SHORT)
                                                        .setDuration(1000)
                                                        .show();
                                            } else {
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
                                DetailDialog detailDialog = new DetailDialog(mContext, mList, position);
                                detailDialog.show();
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}

class AlbumDetailViewHolder extends RecyclerView.ViewHolder {
    final TextView tv_title;
    final TextView tv_time;
    final ImageView iv;
    final ImageView item_menu;

    AlbumDetailViewHolder(View itemView) {
        super(itemView);
        iv = (ImageView) itemView.findViewById(R.id.detail_album_art);
        tv_time = (TextView) itemView.findViewById(R.id.detail_time);
        tv_title = (TextView) itemView.findViewById(R.id.detail_title);
        item_menu = (ImageView) itemView.findViewById(R.id.detail_item_menu);
    }
    public void setData(int position){
        Set<Integer> positionSet = new HashSet<>();
        switch (MusicList.Mod){
            case 2:
                positionSet = ArtistDetailFragment.positionSet;
                break;
            case 3:
                positionSet = AlbumDetailFragment.positionSet;
                break;
        }
        if (positionSet.contains(position)){
            itemView.setBackgroundResource(R.color.items_selected_bg_color);
        }else {
            itemView.setBackgroundResource(R.color.listView_bg_color);
        }
    }
}




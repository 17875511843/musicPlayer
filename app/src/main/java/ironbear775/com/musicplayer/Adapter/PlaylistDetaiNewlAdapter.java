package ironbear775.com.musicplayer.Adapter;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;
import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Fragment.PlaylistDetailFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.PlaylistDbHelper;

/**
 * Created by ironbear on 2017/4/23.
 */

public class PlaylistDetaiNewlAdapter extends RecyclerView.Adapter<PlaylistViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter,RVHAdapter {
    private final LayoutInflater mInflater;
    private final ArrayList<Music> mList;
    private boolean isClickable = true;
    private Context mContext;
    private PlaylistDbHelper dbHelper;
    private SQLiteDatabase database;
    private String mName;

    @NonNull
    @Override
    public String getSectionName(int position) {
        return mList.get(position).getTitle().substring(0, 1).toUpperCase(Locale.ENGLISH);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        swap(fromPosition, toPosition);
        return false;
    }

    private void swap(int firstPosition, int secondPosition) {
        Collections.swap(mList, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
        Intent intent = new Intent("playlist swap item");
        intent.setAction("playlist swap item");
        intent.putExtra("new list",mList);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onItemDismiss(int position, int direction) {
        remove(position);
    }
    private void remove(int position) {

        final Music music = PlaylistDetailFragment.musicList.get(position);
        String db = "";
        dbHelper = new PlaylistDbHelper(mContext, mName + ".db", db);
        database = dbHelper.getWritableDatabase();
        database.delete(mName, "title = ?", new String[]{music.getTitle()});
        database.close();
        dbHelper.close();

        mList.remove(position);
        notifyItemRemoved(position);

        Snackbar.make(MusicList.PlayOrPause, R.string.delete_from_list, Snackbar.LENGTH_SHORT)
                .setDuration(1000)
                .show();

    }


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private PlaylistDetaiNewlAdapter.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(PlaylistDetaiNewlAdapter.OnItemClickListener listener) {
        this.mOnItemClickListener =  listener;
    }

    public void setClickable(boolean click) {
        this.isClickable = click;
    }

    public PlaylistDetaiNewlAdapter(Context context, ArrayList<Music> list,String name) {
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(context);
        this.mName = name;
    }


    @Override
    public PlaylistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.playlist_detail_item_layout, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaylistViewHolder holder, int position) {
         if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isClickable) {
                        mOnItemClickListener.onItemClick(holder.itemView, position);
                    }
                }
            });
        }

        holder.tv_title.setText(mList.get(position).getTitle());
        holder.tv_others.setText(mList.get(position).getArtist());
        String albumArtUri = mList.get(position).getAlbumArtUri();

        Glide.with(mContext)
                .load(albumArtUri)
                .placeholder(R.drawable.default_album_art)
                .into(holder.iv);

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


}


class PlaylistViewHolder extends RecyclerView.ViewHolder implements RVHViewHolder {
    final ImageView iv;
    final TextView tv_title;
    final TextView tv_others;

    PlaylistViewHolder(View itemView) {
        super(itemView);
        iv = (ImageView) itemView.findViewById(R.id.playlist_iv_image);
        tv_title = (TextView) itemView.findViewById(R.id.playlist_tv_title);
        tv_others = (TextView) itemView.findViewById(R.id.playlist_tv_others);
    }

    @Override
    public void onItemSelected(int actionstate) {

    }

    @Override
    public void onItemClear() {

    }
}

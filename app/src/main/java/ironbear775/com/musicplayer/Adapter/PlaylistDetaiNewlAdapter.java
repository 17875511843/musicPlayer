package ironbear775.com.musicplayer.Adapter;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;
import ironbear775.com.musicplayer.Activity.BaseActivity;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Fragment.PlaylistDetailFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;
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
        PlaylistDbHelper dbHelper = new PlaylistDbHelper(mContext, mName + ".db", db);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(mName, "title = ?", new String[]{music.getTitle()});
        database.close();
        dbHelper.close();

        mList.remove(position);
        notifyItemRemoved(position);


        Intent intent1 = new Intent("show snackBar");
        intent1.putExtra("text id",R.string.delete_from_list);
        mContext.sendBroadcast(intent1);

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
        if (BaseActivity.isNight)
            view.setBackgroundResource(R.color.nightBg);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaylistViewHolder holder, int position) {
         if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> {
                if (isClickable) {
                    mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            });
        }

        holder.tv_title.setText(mList.get(position).getTitle());
        holder.tv_others.setText(mList.get(position).getArtist());
        holder.iv.setTag(R.id.item_url, mList.get(position).getTitle()+mList.get(position).getArtist());

        MusicUtils musicUtils = new MusicUtils(mContext);
        musicUtils.setAlbumCoverToAdapter(mList.get(position),holder.iv,MusicUtils.FROM_ADAPTER);
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
        iv = itemView.findViewById(R.id.playlist_iv_image);
        tv_title = itemView.findViewById(R.id.playlist_tv_title);
        tv_others =  itemView.findViewById(R.id.playlist_tv_others);
        if (BaseActivity.isNight){
            tv_title.setTextColor(itemView.getResources().getColor(R.color.nightMainTextColor));
            tv_others.setTextColor(itemView.getResources().getColor(R.color.nightSubTextColor));
        }
    }

    @Override
    public void onItemSelected(int actionstate) {

    }

    @Override
    public void onItemClear() {

    }
}

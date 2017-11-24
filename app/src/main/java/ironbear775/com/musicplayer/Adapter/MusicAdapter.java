package ironbear775.com.musicplayer.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Activity.SearchActivity;
import ironbear775.com.musicplayer.Activity.TagEditActivty;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Fragment.FolderDetailFragment;
import ironbear775.com.musicplayer.Fragment.MusicListFragment;
import ironbear775.com.musicplayer.Fragment.MusicRecentAddedFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;
import ironbear775.com.musicplayer.Util.DetailDialog;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.PlaylistDialog;

/**
 * Created by ironbear on 2017/1/15.
 */

public class MusicAdapter extends RecyclerView.Adapter<MusicViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter{
    private static final int TYPE_HEADER = 0;  //说明是带有Header的
    private static final int TYPE_NORMAL = 1;  //说明是不带有header和footer的
    private final LayoutInflater mInflater;
    private final Activity mActivity;
    private final ArrayList<Music> mList;
    private boolean isClickable = true;

    @NonNull
    @Override
    public String getSectionName(int position) {
        if (position >= 0) {
            return mList.get(position ).getTitle().substring(0, 1).toUpperCase(Locale.ENGLISH);
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

    public void setClickable(boolean click) {
        this.isClickable = click;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_NORMAL;
    }

    public MusicAdapter(Activity activity, ArrayList<Music> list) {
        this.mList = list;
        this.mActivity = activity;
        this.mInflater = LayoutInflater.from(activity);
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_layout, parent, false);

        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MusicViewHolder holder, final int position) {
        if (position >= 0) {
            holder.setData(position);
        }
        if (mOnItemClickListener != null) {

            final int layoutPosition = holder.getLayoutPosition();
            holder.itemView.setOnClickListener(v -> {
                if (isClickable) {
                    mOnItemClickListener.onItemClick(holder.itemView, layoutPosition);
                }
            });
            holder.itemView.setOnLongClickListener(v -> {
                mOnItemClickListener.onItemLongClick(holder.itemView, layoutPosition);
                return false;
            });
        }
        if (getItemViewType(position) == TYPE_NORMAL) {
            if (holder != null) {
                holder.item_menu.setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(mActivity,holder.item_menu);
                    popupMenu.inflate(R.menu.new_menu);
                    popupMenu.setOnMenuItemClickListener(item -> {
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
                                    case 6:
                                        listPositionSet = FolderDetailFragment.positionSet;
                                        break;
                                }
                                listPositionSet.add(position);
                                PlaylistDialog dialog = new PlaylistDialog(mActivity,listPositionSet, mList);
                                dialog.show();
                                break;
                            case R.id.menu_delete:

                                AlertDialog.Builder alertDialog= new AlertDialog.Builder(mActivity);

                                alertDialog.setTitle(R.string.delete_alert_title);
                                alertDialog.setMessage(mList.get(position).getTitle());
                                alertDialog.setCancelable(true);
                                alertDialog.setNegativeButton(R.string.delete_cancel, (dialog1, which) -> {

                                });
                                alertDialog.setPositiveButton(R.string.delete_confrim, (dialog12, which) -> {
                                    String uri = mList.get(position).getUri();
                                    mActivity.getContentResolver().delete(
                                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                            MediaStore.Audio.Media.DATA + "=?",
                                            new String[]{uri});
                                    File file = new File(uri);
                                    if (file.isFile()){
                                        if (file.delete()) {
                                            if (MusicService.musicService != null
                                                    &&MusicService.mediaPlayer.isPlaying()
                                                    &&mList.get(position).getUri().equals(MusicService.music.getUri())) {
                                                Intent intentNext = new Intent("delete current music success");
                                                mActivity.sendBroadcast(intentNext);
                                            }
                                            Intent intent = new Intent("notifyDataSetChanged");
                                            switch (MusicList.Mod){
                                                case 1:
                                                    MusicListFragment.musicList.remove(position);
                                                    break;
                                                case 5:
                                                    MusicRecentAddedFragment.musicList.remove(position);
                                                    break;
                                                case 6:
                                                    FolderDetailFragment.musicList.remove(position);
                                                    break;
                                            }
                                            if (SearchActivity.musicList!=null && SearchActivity.musicList.size()>0){
                                                SearchActivity.musicList.remove(position);
                                            }
                                            mActivity.sendBroadcast(intent);
                                            Intent intent1 = new Intent("show snackBar");
                                            intent1.putExtra("text id",R.string.success);
                                            mActivity.sendBroadcast(intent1);

                                        }else {
                                            Intent intent1 = new Intent("show snackBar");
                                            intent1.putExtra("text id",R.string.failed);
                                            mActivity.sendBroadcast(intent1);
                                        }
                                    }
                                });
                                alertDialog.show();
                                break;
                            case R.id.play_next:
                                Intent intent1 = new Intent("play next");
                                intent1.putExtra("from",1);
                                intent1.putExtra("uri",mList.get(position).getUri());
                                mActivity.sendBroadcast(intent1);
                                break;
                            case R.id.menu_detail:
                                DetailDialog detailDialog = new DetailDialog(mActivity,mList,position);
                                detailDialog.show();
                                break;
                            case R.id.tag_edit:
                                if (mList.get(position).getUri().contains(".mp3")
                                        || mList.get(position).getUri().contains(".MP3")) {
                                    Intent intent = new Intent(mActivity, TagEditActivty.class);
                                    intent.putExtra("music", (Parcelable) mList.get(position));
                                    mActivity.startActivity(intent);
                                }else {
                                    Toast.makeText(mActivity,R.string.open_failed,Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                        return false;
                    });
                    popupMenu.show();
                });

                holder.iv.setTag(R.id.item_url, mList.get(position).getTitle()+mList.get(position).getArtist());
                holder.tv_title.setText(mList.get(position).getTitle());
                String others = mList.get(position).getArtist()+"-"+mList.get(position).getAlbum();
                holder.tv_others.setText(others);

                MusicUtils musicUtils = new MusicUtils(mActivity);
                musicUtils.setAlbumCoverToAdapter(mList.get(position),holder.iv,MusicUtils.FROM_ADAPTER);

            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

}

class MusicViewHolder extends RecyclerView.ViewHolder   {
    final ImageView iv;
    final TextView tv_title;
    final TextView tv_others;
    final ImageView item_menu;

    MusicViewHolder(final View itemView) {
        super(itemView);
        iv = itemView.findViewById(R.id.iv_image);
        tv_others = itemView.findViewById(R.id.tv_others);
        tv_title = itemView.findViewById(R.id.tv_title);
        item_menu = itemView.findViewById(R.id.item_menu);
    }

    public void setData(int position){
        Set<Integer> positionSet = new HashSet<>();
        if (MusicUtils.isSelectAll){
            positionSet = MusicList.listPositionSet;
        }else {
            switch (MusicList.Mod) {
                case 1:
                    positionSet = MusicListFragment.positionSet;
                    break;
                case 5:
                    positionSet = MusicRecentAddedFragment.positionSet;
                    break;
                case 6:
                    positionSet = FolderDetailFragment.positionSet;
                    break;
            }
        }
        if (positionSet.contains(position)){
            itemView.setBackgroundResource(R.color.items_selected_bg_color);
        }else {
            itemView.setBackground(null);
        }
    }
}




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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Activity.TagEditActivty;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Fragment.AlbumDetailFragment;
import ironbear775.com.musicplayer.Fragment.AlbumListFragment;
import ironbear775.com.musicplayer.Fragment.ArtistDetailFragment;
import ironbear775.com.musicplayer.Fragment.ArtistListFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;
import ironbear775.com.musicplayer.Util.DetailDialog;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.PlaylistDialog;

/**
 * Created by ironbear on 2017/10/17.
 */

public class ArtistDetailAdapter extends RecyclerView.Adapter<ArtistDetailViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {
    private final LayoutInflater mInflater;
    private final Activity mActivity;
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

    private ironbear775.com.musicplayer.Adapter.AlbumDetailAdapter.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(ironbear775.com.musicplayer.Adapter.AlbumDetailAdapter.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setClickable(boolean click) {
        this.isClickable = click;
    }

    public ArtistDetailAdapter(Activity activity, ArrayList<Music> list) {
        this.mList = list;
        this.mActivity = activity;
        this.mInflater = LayoutInflater.from(activity);
    }

    @Override
    public ArtistDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.detail_layout, parent, false);
        return new ArtistDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ArtistDetailViewHolder holder, final int position) {
        holder.setData(position);
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> {
                if (isClickable) {
                    mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            });
            holder.itemView.setOnLongClickListener(v -> {
                mOnItemClickListener.onItemLongClick(holder.itemView, position);
                return false;
            });
        }
        if (MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_PAGE) {
            holder.track.setVisibility(View.GONE);
            holder.item_image.setTag(R.id.item_url, mList.get(position).getTitle()+mList.get(position).getArtist());
            MusicUtils musicUtils = new MusicUtils(mActivity);
            musicUtils.setAlbumCoverToAdapter(mList.get(position),holder.item_image,MusicUtils.FROM_ADAPTER);
        }

        holder.tv_title.setText(mList.get(position).getTitle());
        holder.tv_time.setText(time.format(mList.get(position).getDuration()));

        holder.item_menu.setOnClickListener(v -> {
            final PopupMenu popupMenu = new PopupMenu(mActivity, holder.item_menu);
            popupMenu.inflate(R.menu.new_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
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
                        PlaylistDialog dialog = new PlaylistDialog(mActivity, listPositionSet, mList);
                        dialog.show();
                        break;
                    case R.id.menu_delete:
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);
                        alertDialog.setTitle(R.string.delete_alert_title);
                        alertDialog.setMessage(mList.get(position).getTitle());
                        alertDialog.setCancelable(true);
                        alertDialog.setNegativeButton(R.string.delete_cancel, (dialog1, which) -> {

                        });
                        alertDialog.setPositiveButton(R.string.delete_confrim, (dialog12, which) -> {
                            String uri = mList.get(position).getUri();
                            String artist = mList.get(position).getArtist();
                            int pos = 0;
                            mActivity.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    MediaStore.Audio.Media.DATA + "=?",
                                    new String[]{uri});
                            File file = new File(uri);
                            if (file.isFile()) {
                                if (file.delete()) {
                                    if (MusicService.musicService != null
                                            &&MusicService.mediaPlayer.isPlaying()
                                            &&mList.get(position).getUri().equals(MusicService.music.getUri())) {
                                        Intent intentNext = new Intent("delete current music success");
                                        mActivity.sendBroadcast(intentNext);
                                    }
                                    Intent intent = new Intent("notifyDataSetChanged");
                                    switch (MusicList.Mod) {
                                        case 2:
                                            if (ArtistDetailFragment.musicList.size() == 1) {
                                                for (int i = 0; i < ArtistListFragment.artistlist.size(); i++) {
                                                    if (ArtistListFragment.artistlist.get(i).getArtist().equals(artist)) {
                                                        pos = i;
                                                        break;
                                                    }
                                                }
                                                ArtistListFragment.artistlist.remove(pos);
                                            }
                                            ArtistDetailFragment.musicList.remove(position);
                                            break;
                                        case 3:
                                            if (AlbumDetailFragment.musicList.size() == 1) {
                                                for (int i = 0; i < AlbumListFragment.albumlist.size(); i++) {
                                                    if (AlbumListFragment.albumlist.get(i).getArtist().equals(artist)) {
                                                        pos = i;
                                                        break;
                                                    }
                                                }
                                                AlbumListFragment.albumlist.remove(pos);
                                            }
                                            AlbumDetailFragment.musicList.remove(position);
                                            break;
                                    }
                                    mActivity.sendBroadcast(intent);

                                    Intent intent1 = new Intent("show snackBar");
                                    intent1.putExtra("text id",R.string.success);
                                    mActivity.sendBroadcast(intent1);
                                } else {

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
                        DetailDialog detailDialog = new DetailDialog(mActivity, mList, position);
                        detailDialog.show();
                        break;
                    case R.id.tag_edit:
                        if (mList.get(position).getUri().contains(".mp3")
                                || mList.get(position).getUri().contains(".MP3")) {
                            Intent intent = new Intent(mActivity, TagEditActivty.class);
                            intent.putExtra("music", (Parcelable) mList.get(position));
                            mActivity.startActivity(intent);
                        } else {
                            Toast.makeText(mActivity, R.string.open_failed, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}

class ArtistDetailViewHolder extends RecyclerView.ViewHolder {
    final TextView tv_title;
    final TextView tv_time;
    final TextView track;
    final ImageView item_image;
    final ImageView item_menu;

    ArtistDetailViewHolder(View itemView) {
        super(itemView);
        track = itemView.findViewById(R.id.detail_track);
        tv_time = itemView.findViewById(R.id.detail_time);
        tv_title = itemView.findViewById(R.id.detail_title);
        item_menu = itemView.findViewById(R.id.detail_item_menu);
        item_image = itemView.findViewById(R.id.detail_album_art);
    }

    public void setData(int position) {
        Set<Integer> positionSet;
        if (MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_PAGE) {
            if (MusicUtils.isSelectAll) {
                positionSet = MusicList.listPositionSet;
            } else {
                positionSet = ArtistDetailFragment.positionSet;
            }
            if (positionSet.contains(position)) {
                itemView.setBackgroundResource(R.color.items_selected_bg_color);
            } else {
                itemView.setBackground(null);
            }
        }
    }

}





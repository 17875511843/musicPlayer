package ironbear775.com.musicplayer.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Fragment.AlbumListFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.SquareImageView;

/**
 * Created by ironbear on 2017/1/15.
 */

public class AlbumGridAdapter extends RecyclerView.Adapter<AlbumGridViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final ArrayList<Music> mList;
    private boolean isClickable = true;

    @NonNull
    @Override
    public String getSectionName(int position) {
        return mList.get(position).getAlbum().substring(0, 1).toUpperCase(Locale.ENGLISH);
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

    public AlbumGridAdapter(Context context, ArrayList<Music> list) {
        this.mList = list;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public AlbumGridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_grid_layout, parent, false);
        return new AlbumGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AlbumGridViewHolder holder, final int position) {
        holder.item_info.setBackgroundColor(ContextCompat.getColor(mContext,R.color.material_gray_dark));
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
        holder.setData(position);
        holder.tv_title.setText(mList.get(position).getAlbum());
        holder.tv_others.setText(mList.get(position).getArtist());
        final String albumArtUri = mList.get(position).getAlbumArtUri();

        Glide.with(mContext)
                .load(albumArtUri)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                Palette.Swatch swatch = palette.getVibrantSwatch();
                                if (swatch != null) {
                                    holder.item_info.setBackgroundColor(swatch.getRgb());
                                    holder.tv_title.setTextColor(swatch.getTitleTextColor());
                                    holder.tv_others.setTextColor(swatch.getBodyTextColor());
                                } else {
                                    swatch = palette.getMutedSwatch();
                                    if (swatch != null) {
                                        holder.item_info.setBackgroundColor(swatch.getRgb());
                                        holder.tv_title.setTextColor(swatch.getTitleTextColor());
                                        holder.tv_others.setTextColor(swatch.getBodyTextColor());
                                    }
                                }
                            }
                        });
                    }
                });

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


class AlbumGridViewHolder extends RecyclerView.ViewHolder {
    final ImageView iv;
    final TextView tv_title;
    final TextView tv_others;
    final RelativeLayout item_info;
    private final ImageView bg;

    AlbumGridViewHolder(View itemView) {
        super(itemView);
        iv = (SquareImageView) itemView.findViewById(R.id.iv_grid_image);
        tv_others = (TextView) itemView.findViewById(R.id.tv_others);
        tv_title = (TextView) itemView.findViewById(R.id.tv_title);
        item_info = (RelativeLayout) itemView.findViewById(R.id.item_info);
        bg = (ImageView) itemView.findViewById(R.id.bg_imageView);
    }

    public void setData(int position) {
        Set<Integer> positionSet = AlbumListFragment.positionSet;
        if (positionSet.contains(position)) {
            bg.setVisibility(View.VISIBLE);
        } else {
            bg.setVisibility(View.GONE);
        }
    }
}




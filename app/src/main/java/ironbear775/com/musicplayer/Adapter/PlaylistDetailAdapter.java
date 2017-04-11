package ironbear775.com.musicplayer.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;

/**
 * Created by ironbear on 2017/2/4.
 */

public class PlaylistDetailAdapter extends ArrayAdapter {
    private final int resourceId;

    public PlaylistDetailAdapter(Context context, int resource, List<Music> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Music music = (Music) getItem(position);
        ViewHolder viewHolder;
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.albumArt = (ImageView) view.findViewById(R.id.playlist_iv_image);
            viewHolder.title = (TextView) view.findViewById(R.id.playlist_tv_title);
            viewHolder.others = (TextView) view.findViewById(R.id.playlist_tv_others);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        if (music != null) {
            Glide.with(getContext())
                    .load(music.getAlbumArtUri())
                    .asBitmap()
                    .placeholder(R.drawable.default_album_art)
                    .into(viewHolder.albumArt);
        }
        if (music != null) {
            viewHolder.title.setText(music.getTitle());
            viewHolder.others.setText(music.getArtist());
        }
        return view;
    }

    private class ViewHolder {
        ImageView albumArt;
        TextView title;
        TextView others;
    }
}

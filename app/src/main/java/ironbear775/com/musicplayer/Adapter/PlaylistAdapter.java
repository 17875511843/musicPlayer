package ironbear775.com.musicplayer.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ironbear775.com.musicplayer.Class.Playlist;
import ironbear775.com.musicplayer.R;

/**
 * Created by ironbear on 2017/2/3.
 */

public class PlaylistAdapter extends ArrayAdapter<Playlist> {
    private final int resourceId;

    public PlaylistAdapter(Context context, int resource, List<Playlist> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Playlist playlist = getItem(position);
        View view ;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.name =(TextView) view.findViewById(R.id.item_name);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.name.setText(playlist != null ? playlist.getName() : null);
        return view;
    }
    private class ViewHolder {
        TextView name;
    }
}

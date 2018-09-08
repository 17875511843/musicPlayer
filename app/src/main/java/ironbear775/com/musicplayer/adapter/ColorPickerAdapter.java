package ironbear775.com.musicplayer.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ironbear775.com.musicplayer.entity.ColorItem;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.util.MusicUtils;

/**
 * Created by ironbear on 2017/11/7.
 */

public class ColorPickerAdapter extends ArrayAdapter<ColorItem> {
    private int resourceId;

    public ColorPickerAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ColorItem> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ColorItem item = getItem(position);

        View view;

        ColorPickerAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ColorPickerAdapter.ViewHolder();
            viewHolder.name = view.findViewById(R.id.item_name);
            viewHolder.circleImageView = view.findViewById(R.id.item_color);
            viewHolder.checked = view.findViewById(R.id.item_check);

            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ColorPickerAdapter.ViewHolder) view.getTag();
        }
        if (item != null) {
            viewHolder.circleImageView.setBackgroundResource(item.getColor());
            viewHolder.name.setTextColor(view.getResources().getColor(item.getColor()));
            viewHolder.name.setText(view.getResources().getString(item.getName()));

            if (item.getChecked() && item.getName()== MusicUtils.getInstance().themeName)
                viewHolder.checked.setVisibility(View.VISIBLE);
            else{
                viewHolder.checked.setVisibility(View.GONE);
                item.setChecked(false);
            }
        }
        return view;
    }


    private class ViewHolder {
        TextView name;
        CircleImageView circleImageView;
        ImageView checked;
    }
}

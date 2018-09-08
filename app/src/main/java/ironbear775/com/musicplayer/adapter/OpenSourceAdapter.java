package ironbear775.com.musicplayer.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ironbear775.com.musicplayer.activity.BaseActivity;
import ironbear775.com.musicplayer.entity.OpenSourceItem;
import ironbear775.com.musicplayer.R;

/**
 * Created by ironbear on 2017/3/31.
 */

public class OpenSourceAdapter extends ArrayAdapter<OpenSourceItem> {
    private int resourceId;

    public OpenSourceAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<OpenSourceItem> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        OpenSourceItem item = getItem(position);

        View view;

        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.name = view.findViewById(R.id.open_source_name);
            viewHolder.detail = view.findViewById(R.id.open_source_url);

            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        if (item != null) {
            viewHolder.name.setText(item.getName());
            if (BaseActivity.isNight)
                viewHolder.detail.setTextColor(view.getResources().getColor(R.color.nightSubTextColor));
            viewHolder.detail.setText(item.getDetail());
        }
        return view;
    }


    private class ViewHolder {
        TextView name;
        TextView detail;
    }
}

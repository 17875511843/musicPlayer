package ironbear775.com.musicplayer.Util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ListView;

import java.util.ArrayList;

import ironbear775.com.musicplayer.Adapter.OpenSourceAdapter;
import ironbear775.com.musicplayer.Class.OpenSourceItem;
import ironbear775.com.musicplayer.R;

/**
 * Created by ironbear on 2017/3/31.
 */

public class OpenSource extends Dialog {

    private ArrayList<OpenSourceItem> openSource = new ArrayList<>();
    public OpenSource(@NonNull Context context) {
        super(context);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_source_layout);
        OpenSourceItem item =new OpenSourceItem();
        item.setName("Glide");
        item.setDetail(getContext().getResources().getString(R.string.project_code) + "https://github.com/bumptech/glide"
                + "\n" + getContext().getResources().getString(R.string.license) + "https://github.com/bumptech/glide#license");
        openSource.add(0,item);

        OpenSourceItem item1 =new OpenSourceItem();
        item1.setName("RecyclerView-FastScroll");
        item1.setDetail(getContext().getResources().getString(R.string.project_code) + "https://github.com/timusus/RecyclerView-FastScroll"
                + "\n" + getContext().getResources().getString(R.string.license) + "http://www.apache.org/licenses/LICENSE-2.0");
        openSource.add(1,item1);

        OpenSourceItem item2 =new OpenSourceItem();
        item2.setName("SlideUp-Android");
        item2.setDetail(getContext().getResources().getString(R.string.project_code) + "https://github.com/mancj/SlideUp-Android"
                + "\n" + getContext().getResources().getString(R.string.license) + "https://github.com/mancj/SlideUp-Android/blob/master/LICENSE.txt");
        openSource.add(2,item2);

        OpenSourceItem item3 =new OpenSourceItem();
        item3.setName("MaterialDrawer");
        item3.setDetail(getContext().getResources().getString(R.string.project_code) + "https://github.com/mikepenz/MaterialDrawer"
                + "\n" + getContext().getResources().getString(R.string.license) + "https://github.com/mikepenz/MaterialDrawer#license");
        openSource.add(3,item3);

        OpenSourceItem item4 =new OpenSourceItem();
        item4.setName("TextDrawable");
        item4.setDetail(getContext().getResources().getString(R.string.project_code) + "https://github.com/amulyakhare/TextDrawable"
                + "\n" + getContext().getResources().getString(R.string.license) + "https://github.com/amulyakhare/TextDrawable/blob/master/LICENSE");
        openSource.add(4,item4);

        OpenSourceItem item5 =new OpenSourceItem();
        item5.setName("RecyclerViewHelper");
        item5.setDetail(getContext().getResources().getString(R.string.project_code) + "https://github.com/nisrulz/recyclerviewhelper"
                + "\n" + getContext().getResources().getString(R.string.license) + "https://github.com/nisrulz/recyclerviewhelper/blob/develop/LICENSE.txt");
        openSource.add(5,item5);

        OpenSourceItem item6 =new OpenSourceItem();
        item6.setName("RecyclerView-FlexibleDivider");
        item6.setDetail(getContext().getResources().getString(R.string.project_code) + "https://github.com/yqritc/RecyclerView-FlexibleDivider"
                + "\n" + getContext().getResources().getString(R.string.license) + "https://github.com/yqritc/RecyclerView-FlexibleDivider/blob/master/LICENSE");
        openSource.add(6,item6);

        OpenSourceItem item7 =new OpenSourceItem();
        item7.setName("SlideAndDragListView");
        item7.setDetail(getContext().getResources().getString(R.string.project_code) + "https://github.com/yydcdut/SlideAndDragListView"
                + "\n" + getContext().getResources().getString(R.string.license) + "https://github.com/yydcdut/SlideAndDragListView/blob/master/LICENSE");
        openSource.add(7,item7);

        OpenSourceItem item8 =new OpenSourceItem();
        item8.setName("AppIntro");
        item8.setDetail(getContext().getResources().getString(R.string.project_code) + "https://github.com/apl-devs/AppIntro"
                + "\n" + getContext().getResources().getString(R.string.license) + "https://github.com/apl-devs/AppIntro/blob/master/LICENSE");
        openSource.add(8,item8);

        OpenSourceItem item9 =new OpenSourceItem();
        item9.setName("OkHttp");
        item9.setDetail(getContext().getResources().getString(R.string.project_code) + "https://github.com/square/okhttp"
                + "\n" + getContext().getResources().getString(R.string.license) + "https://github.com/square/okhttp/blob/master/LICENSE.txt");
        openSource.add(9,item9);

        OpenSourceItem item10 =new OpenSourceItem();
        item10.setName("Mp3Agic");
        item10.setDetail(getContext().getResources().getString(R.string.project_code) + "https://github.com/mpatric/mp3agic"
                + "\n" + getContext().getResources().getString(R.string.license) + "https://github.com/mpatric/mp3agic/blob/master/mit-license.txt");
        openSource.add(10,item10);



        ListView listView = (ListView) findViewById(R.id.open_source_iv);

        OpenSourceAdapter adapter = new OpenSourceAdapter(getContext(),R.layout.open_source_item,openSource);
        listView.setAdapter(adapter);
    }
}

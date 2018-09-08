package ironbear775.com.musicplayer.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;

import ironbear775.com.musicplayer.adapter.ColorPickerAdapter;
import ironbear775.com.musicplayer.entity.ColorItem;
import ironbear775.com.musicplayer.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ironbear on 2017/11/7.
 */

public class ColorPickerDialog extends Dialog {
    private Context mContext;
    private ArrayList<ColorItem> colorList = new ArrayList<>();
    private int checkPosition = 0;

    public ColorPickerDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_layout);

        SharedPreferences sp = mContext.getSharedPreferences("data", MODE_PRIVATE);
        MusicUtils.getInstance().checkPosition = sp.getInt("checkPosition", 0);

        checkPosition = MusicUtils.getInstance().checkPosition;

        Log.d("TAG", "checkPosition: "+checkPosition);

        ColorItem blue = new ColorItem();
        blue.setColor(R.color.ColorPrimaryBlue);
        blue.setName(R.string.color_Blue);
        colorList.add(0,blue);

        ColorItem green = new ColorItem();
        green.setColor(R.color.ColorPrimaryGreen);
        green.setName(R.string.color_Green);
        colorList.add(1,green);

        ColorItem indigo = new ColorItem();
        indigo.setColor(R.color.ColorPrimaryIndigo);
        indigo.setName(R.string.color_Indigo);
        colorList.add(2,indigo);

        ColorItem pink = new ColorItem();
        pink.setColor(R.color.ColorPrimaryPink);
        pink.setName(R.string.color_Pink);
        colorList.add(3,pink);

        ColorItem purple = new ColorItem();
        purple.setColor(R.color.ColorPrimaryPurple);
        purple.setName(R.string.color_Purple);
        colorList.add(4,purple);

        ColorItem red = new ColorItem();
        red.setColor(R.color.ColorPrimaryRed);
        red.setName(R.string.color_Red);
        colorList.add(5,red);

        ColorItem lightBlue = new ColorItem();
        lightBlue.setColor(R.color.ColorPrimaryLightBlue);
        lightBlue.setName(R.string.color_LightBlue);
        colorList.add(6,lightBlue);

        ColorItem teal = new ColorItem();
        teal.setColor(R.color.ColorPrimaryTeal);
        teal.setName(R.string.color_Teal);
        colorList.add(7,teal);

        ColorItem lime = new ColorItem();
        lime.setColor(R.color.ColorPrimaryLime);
        lime.setName(R.string.color_Lime);
        colorList.add(8,lime);

        ColorItem orange = new ColorItem();
        orange.setColor(R.color.ColorPrimaryOrange);
        orange.setName(R.string.color_Orange);
        colorList.add(9,orange);

        ColorItem deepOrange = new ColorItem();
        deepOrange.setColor(R.color.ColorPrimaryDeepOrange);
        deepOrange.setName(R.string.color_DeepOrange);
        colorList.add(10,deepOrange);

        ColorItem brown = new ColorItem();
        brown.setColor(R.color.ColorPrimaryBrown);
        brown.setName(R.string.color_Brown);
        colorList.add(11,brown);

        ColorItem grey = new ColorItem();
        grey.setColor(R.color.ColorPrimaryGrey);
        grey.setName(R.string.color_Grey);
        colorList.add(12,grey);

        ColorItem blueGrey = new ColorItem();
        blueGrey.setColor(R.color.ColorPrimaryBlueGrey);
        blueGrey.setName(R.string.color_BlueGrey);
        colorList.add(13,blueGrey);

        ColorItem carmine = new ColorItem();
        carmine.setColor(R.color.ColorPrimaryCarmine);
        carmine.setName(R.string.color_Carmine);
        colorList.add(14,carmine);

        ColorItem amber = new ColorItem();
        amber.setColor(R.color.ColorPrimaryAmber);
        amber.setName(R.string.color_Amber);
        colorList.add(15,amber);

        ColorItem darkBlue = new ColorItem();
        darkBlue.setColor(R.color.ColorPrimaryDarkBlue1);
        darkBlue.setName(R.string.color_DarkBlue1);
        colorList.add(16,darkBlue);

        ColorItem sandalwood = new ColorItem();
        sandalwood.setColor(R.color.ColorPrimarySandalwood);
        sandalwood.setName(R.string.color_Sandalwood);
        colorList.add(17,sandalwood);

        ColorItem bambooGreen = new ColorItem();
        bambooGreen.setColor(R.color.ColorPrimaryBambooGreen);
        bambooGreen.setName(R.string.color_BambooGreen);
        colorList.add(18,bambooGreen);

        ListView listView = findViewById(R.id.color_picker_lv);
        ColorPickerAdapter adapter = new ColorPickerAdapter(getContext(),R.layout.color_item_layout,colorList);
        listView.setAdapter(adapter);

        ColorItem colorItem = (ColorItem) listView.getItemAtPosition(checkPosition);
        colorItem.setChecked(true);

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            ColorItem colorItem1 = (ColorItem) listView.getItemAtPosition(i);
            if (!colorItem1.getChecked()){
                colorItem1.setChecked(true);
                ColorItem oldCheckItem = (ColorItem) listView.getItemAtPosition(checkPosition);
                oldCheckItem.setChecked(false);
                checkPosition = i;

                SharedPreferences.Editor editor = getContext().getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putInt("checkPosition", checkPosition);
                editor.putInt("themeName", colorItem1.getName());
                editor.apply();
                editor.commit();
                adapter.notifyDataSetChanged();

                getContext().sendBroadcast(new Intent("restart yourself"));


            }

        });
    }
}

package ironbear775.com.musicplayer.entity;

/**
 * Created by ironbear on 2017/11/7.
 */

public class ColorItem {
    private int name; //主题名称
    private int color; //主题主颜色
    private boolean checked; //主题是否应用

    public void setName(int name) {
        this.name = name;
    }

    public int getName() {
        return name;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public Boolean getChecked(){
        return checked;
    }
}

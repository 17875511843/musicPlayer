package ironbear775.com.musicplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Fragment.MusicListFragment;
import ironbear775.com.musicplayer.Service.MusicService;
import ironbear775.com.musicplayer.Util.GetAlbumArt;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ironbear on 2017/5/5.
 */

public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        context.getApplicationContext().registerReceiver(receiver,filter());

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        if (MusicService.mediaPlayer.isPlaying()) {
            Bitmap bitmap = GetAlbumArt.getAlbumArtBitmap(context, MusicService.music.getAlbumArtUri(), 1);
            remoteViews.setImageViewBitmap(R.id.widget_album_art, bitmap);
            remoteViews.setTextViewText(R.id.widget_title, MusicService.music.getTitle());
            remoteViews.setTextViewText(R.id.widget_others, MusicService.music.getAlbum());
            remoteViews.setImageViewResource(R.id.widget_play_pause, R.drawable.footpausewhite);
        }

        Intent intentLast = new Intent(context, MusicService.class);
        intentLast.setAction("PreMusic");
        PendingIntent lastPIntent = PendingIntent.getService(context, 0, intentLast, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_last, lastPIntent);

        Intent intentPlay = new Intent(context, MusicService.class);
        if (MusicService.mediaPlayer.isPlaying()) {
            intentPlay.setAction("isPlaying");
        } else {
            intentPlay.setAction("isPause");
        }
        PendingIntent PlayPIntent = PendingIntent.getService(context, 1, intentPlay, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_play_pause, PlayPIntent);


        Intent intentAlbumArt = new Intent(context, MusicList.class);
        PendingIntent AlbumArtPIntent = PendingIntent.getActivity(context, 4, intentAlbumArt, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_album_art, AlbumArtPIntent);

        Intent intentNext = new Intent(context, MusicService.class);
        intentNext.setAction("NextMusic");
        PendingIntent NextPIntent = PendingIntent.getService(context, 2, intentNext, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_next, NextPIntent);

        ComponentName provider=new ComponentName(context, WidgetProvider.class);//提供者
        AppWidgetManager manager=AppWidgetManager.getInstance(context);//小部件管理器
        manager.updateAppWidget(provider, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        if (MusicService.mediaPlayer.isPlaying()) {
            Bitmap bitmap = GetAlbumArt.getAlbumArtBitmap(context, MusicService.music.getAlbumArtUri(), 1);
            remoteViews.setImageViewBitmap(R.id.widget_album_art, bitmap);
            remoteViews.setTextViewText(R.id.widget_title, MusicService.music.getTitle());
            remoteViews.setTextViewText(R.id.widget_others, MusicService.music.getAlbum());
            remoteViews.setImageViewResource(R.id.widget_play_pause, R.drawable.footpausewhite);
        }else if (MusicList.flag == 0){
            SharedPreferences sharedPreferences = context.getSharedPreferences("data", MODE_PRIVATE);
            ArrayList<Music> arrayList = new ArrayList<>();
            String json = sharedPreferences.getString("json", null);
            if (json != null)
            {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<Music>>(){}.getType();

                arrayList = gson.fromJson(json, type);

            }
            Bitmap bitmap = GetAlbumArt.getAlbumArtBitmap(context,
                    arrayList.get(sharedPreferences.getInt("position", 0)).getAlbumArtUri(), 1);
            remoteViews.setImageViewBitmap(R.id.widget_album_art, bitmap);
            remoteViews.setTextViewText(R.id.widget_title,
                    arrayList.get(sharedPreferences.getInt("position", 0)).getTitle());
            remoteViews.setTextViewText(R.id.widget_others,
                    arrayList.get(sharedPreferences.getInt("position", 0)).getAlbum());
            remoteViews.setImageViewResource(R.id.widget_play_pause, R.drawable.footplaywhite);
        }else {
            Bitmap bitmap = GetAlbumArt.getAlbumArtBitmap(context, MusicService.music.getAlbumArtUri(), 1);
            remoteViews.setImageViewBitmap(R.id.widget_album_art, bitmap);
            remoteViews.setTextViewText(R.id.widget_title, MusicService.music.getTitle());
            remoteViews.setTextViewText(R.id.widget_others, MusicService.music.getAlbum());
            remoteViews.setImageViewResource(R.id.widget_play_pause, R.drawable.footplaywhite);
        }

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("update")){

                Log.d("DADAS","DADSADAS");
                RemoteViews remoteViews=new RemoteViews(context.getPackageName(),R.layout.widget_layout);

                if (MusicService.mediaPlayer.isPlaying()) {
                    Bitmap bitmap = GetAlbumArt.getAlbumArtBitmap(context, MusicService.music.getAlbumArtUri(), 1);
                    remoteViews.setImageViewBitmap(R.id.widget_album_art, bitmap);
                    remoteViews.setTextViewText(R.id.widget_title, MusicService.music.getTitle());
                    remoteViews.setTextViewText(R.id.widget_others, MusicService.music.getAlbum());
                    remoteViews.setImageViewResource(R.id.widget_play_pause, R.drawable.footpausewhite);
                }else if (MusicList.flag == 0){
                    Bitmap bitmap = GetAlbumArt.getAlbumArtBitmap(context,
                            MusicListFragment.musicList.get(MusicListFragment.pos).getAlbumArtUri(), 1);
                    remoteViews.setImageViewBitmap(R.id.widget_album_art, bitmap);
                    remoteViews.setTextViewText(R.id.widget_title,
                            MusicListFragment.musicList.get(MusicListFragment.pos).getTitle());
                    remoteViews.setTextViewText(R.id.widget_others,
                            MusicListFragment.musicList.get(MusicListFragment.pos).getAlbum());
                    remoteViews.setImageViewResource(R.id.widget_play_pause, R.drawable.footplaywhite);
                }else {
                    remoteViews.setImageViewResource(R.id.widget_play_pause, R.drawable.footplaywhite);

                }

                ComponentName provider=new ComponentName(context, WidgetProvider.class);//提供者
                AppWidgetManager manager=AppWidgetManager.getInstance(context);//小部件管理器
                manager.updateAppWidget(provider, remoteViews);
            }
        }
    };
    private static IntentFilter filter() {
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("update");
        return intentfilter;
    }
}

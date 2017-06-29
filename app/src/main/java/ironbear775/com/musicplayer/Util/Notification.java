package ironbear775.com.musicplayer.Util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;

import java.util.ArrayList;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;

import static ironbear775.com.musicplayer.Service.MusicService.musicPosition;


/**
 * Created by ironbear on 2016/12/31.
 */

public class Notification {
    private NotificationTarget notificationTarget;
    private NotificationTarget smallNotificationTarget;
    private final MusicService musicService;

    public Notification(MusicService musicService) {
        this.musicService = musicService;
    }

    public void createNotification(final Context context, int id, ArrayList<Music> musicList, Message msg) {

        final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        final android.support.v7.app.NotificationCompat.MediaStyle mediaStyle = new android.support.v7.app.NotificationCompat.MediaStyle(builder);

        mediaStyle.setShowActionsInCompactView(0, 1);

        Intent intent = new Intent(context, MusicList.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_layout);

        final RemoteViews smallRemoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_small_layout);

        if (MusicUtils.enableLockscreenNotification) {
            builder.setCustomBigContentView(remoteViews)
                    .setContent(smallRemoteViews)
                    .setStyle(mediaStyle)
                    .setContentIntent(pendingIntent)
                    .setPriority(android.app.Notification.PRIORITY_MAX)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setShowWhen(false)
                    .setOnlyAlertOnce(true)
                    .setCategory(android.app.Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setColor(Color.BLACK);
        } else {
            builder.setCustomBigContentView(remoteViews)
                    .setContent(smallRemoteViews)
                    .setStyle(mediaStyle)
                    .setContentIntent(pendingIntent)
                    .setPriority(android.app.Notification.PRIORITY_MAX)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setShowWhen(false)
                    .setOnlyAlertOnce(true)
                    .setCategory(android.app.Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setColor(Color.BLACK);
        }
        final android.app.Notification notification = builder.build();
        notification.priority = android.app.Notification.PRIORITY_MAX;
        notification.flags |= android.app.Notification.FLAG_ONGOING_EVENT;

        notificationTarget = new NotificationTarget(
                context,
                remoteViews,
                R.id.noti_album_art,
                notification,
                1);

        smallNotificationTarget = new NotificationTarget(
                context,
                smallRemoteViews,
                R.id.noti_album_art,
                notification,
                1);

        Intent intentLast = new Intent(context, MusicService.class);
        intentLast.setAction("PreMusic");
        PendingIntent lastPIntent = PendingIntent.getService(context, 0, intentLast, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_last, lastPIntent);
        smallRemoteViews.setOnClickPendingIntent(R.id.noti_last, lastPIntent);

        Intent intentPlay = new Intent(context, MusicService.class);
        if (MusicService.mediaPlayer.isPlaying()) {
            intentPlay.setAction("isPlaying");
        } else {
            intentPlay.setAction("isPause");
        }
        PendingIntent PlayPIntent = PendingIntent.getService(context, 1, intentPlay, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_play_pause, PlayPIntent);
        smallRemoteViews.setOnClickPendingIntent(R.id.noti_play_pause, PlayPIntent);


        Intent intentAlbumArt = new Intent(context, MusicList.class);
        PendingIntent AlbumArtPIntent = PendingIntent.getActivity(context, 4, intentAlbumArt, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_album_art, AlbumArtPIntent);
        smallRemoteViews.setOnClickPendingIntent(R.id.album_art, AlbumArtPIntent);

        Intent intentNext = new Intent(context, MusicService.class);
        intentNext.setAction("NextMusic");
        PendingIntent NextPIntent = PendingIntent.getService(context, 2, intentNext, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_next, NextPIntent);
        smallRemoteViews.setOnClickPendingIntent(R.id.noti_next, NextPIntent);


        Intent intentClear = new Intent(context, MusicService.class);
        intentClear.setAction("ClearMusic");
        PendingIntent ClearPIntent = PendingIntent.getService(context, 3, intentClear, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_clear, ClearPIntent);

        if (musicList.size() >= 1) {

            if (MusicUtils.enableColorNotification && msg.obj.equals(MusicUtils.messageGood)) {
                remoteViews.setTextColor(R.id.noti_title, msg.arg1);
                remoteViews.setTextColor(R.id.noti_others, msg.arg2);
                smallRemoteViews.setTextColor(R.id.noti_title, msg.arg1);
                smallRemoteViews.setTextColor(R.id.noti_others, msg.arg2);
            } else if (MusicUtils.isFlyme){
                remoteViews.setTextColor(R.id.noti_title, ContextCompat.getColor(context, R.color.md_white_1000));
                remoteViews.setTextColor(R.id.noti_others, ContextCompat.getColor(context, R.color.md_white_1000));
                smallRemoteViews.setTextColor(R.id.noti_title, ContextCompat.getColor(context, R.color.md_white_1000));
                smallRemoteViews.setTextColor(R.id.noti_others, ContextCompat.getColor(context, R.color.md_white_1000));
            }else {
                remoteViews.setTextColor(R.id.noti_title, ContextCompat.getColor(context, R.color.black));
                remoteViews.setTextColor(R.id.noti_others, ContextCompat.getColor(context, R.color.black));
                smallRemoteViews.setTextColor(R.id.noti_title, ContextCompat.getColor(context, R.color.black));
                smallRemoteViews.setTextColor(R.id.noti_others, ContextCompat.getColor(context, R.color.black));
            }

            remoteViews.setTextViewText(R.id.noti_title, musicList.get(musicPosition).getTitle());
            smallRemoteViews.setTextViewText(R.id.noti_title, musicList.get(musicPosition).getTitle());
            remoteViews.setTextViewText(R.id.noti_others,
                    musicList.get(musicPosition).getArtist());

            smallRemoteViews.setTextViewText(R.id.noti_others,
                    musicList.get(musicPosition).getArtist());

        }
        if (MusicUtils.enableColorNotification){
            remoteViews.setImageViewResource(R.id.noti_play_pause, id);
            smallRemoteViews.setImageViewResource(R.id.noti_play_pause, id);
        }else {
            if (MusicUtils.isFlyme && id == R.drawable.footplay) {
                remoteViews.setImageViewResource(R.id.noti_play_pause, R.drawable.footplaywhite);
                smallRemoteViews.setImageViewResource(R.id.noti_play_pause, R.drawable.footplaywhite);
            } else if (MusicUtils.isFlyme && id == R.drawable.footpause) {
                remoteViews.setImageViewResource(R.id.noti_play_pause, R.drawable.footpausewhite);
                smallRemoteViews.setImageViewResource(R.id.noti_play_pause, R.drawable.footpausewhite);
            } else {
                remoteViews.setImageViewResource(R.id.noti_play_pause, id);
                smallRemoteViews.setImageViewResource(R.id.noti_play_pause, id);
            }
        }

        if (MusicService.mediaPlayer.isPlaying()) {

            if (MusicUtils.enableColorNotification &&
                    msg.obj.equals(MusicUtils.messageGood)) {
                Bitmap image = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565);
                image.eraseColor(msg.what);

                remoteViews.setImageViewBitmap(R.id.noti_background, image);
                smallRemoteViews.setImageViewBitmap(R.id.noti_background, image);
                remoteViews.setImageViewResource(R.id.noti_clear, R.drawable.cancel_black);

                remoteViews.setImageViewResource(R.id.noti_next, R.drawable.next);
                remoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous);

                smallRemoteViews.setImageViewResource(R.id.noti_next, R.drawable.next);
                smallRemoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous);

                Glide.with(context.getApplicationContext())
                        .load(MusicService.music.getAlbumArtUri())
                        .asBitmap()
                        .placeholder(R.drawable.default_album_art)
                        .into(notificationTarget);
                Glide.with(context.getApplicationContext())
                        .load(MusicService.music.getAlbumArtUri())
                        .asBitmap()
                        .placeholder(R.drawable.default_album_art)
                        .into(smallNotificationTarget);
            } else if (msg.obj.equals(MusicUtils.messageNull)) {

                remoteViews.setImageViewResource(R.id.noti_background,R.color.transparent_color);
                smallRemoteViews.setImageViewResource(R.id.noti_background, R.color.transparent_color);

                if (MusicUtils.isFlyme){
                    remoteViews.setImageViewResource(R.id.noti_next, R.drawable.next_white);
                    remoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous_white);

                    smallRemoteViews.setImageViewResource(R.id.noti_next, R.drawable.next_white);
                    smallRemoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous_white);
                }else {
                    remoteViews.setImageViewResource(R.id.noti_next, R.drawable.next);
                    remoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous);

                    smallRemoteViews.setImageViewResource(R.id.noti_next, R.drawable.next);
                    smallRemoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous);
                }
                remoteViews.setImageViewResource(R.id.noti_clear, R.drawable.noti_clear);

                Glide.with(context.getApplicationContext())
                        .load(R.drawable.default_album_art)
                        .asBitmap()
                        .error(R.drawable.default_album_art)
                        .into(notificationTarget);
                Glide.with(context.getApplicationContext())
                        .load(R.drawable.default_album_art)
                        .asBitmap()
                        .error(R.drawable.default_album_art)
                        .into(smallNotificationTarget);

            } else {
                remoteViews.setImageViewResource(R.id.noti_background,R.color.transparent_color);
                smallRemoteViews.setImageViewResource(R.id.noti_background, R.color.transparent_color);

                if (MusicUtils.isFlyme){
                    remoteViews.setImageViewResource(R.id.noti_next, R.drawable.next_white);
                    remoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous_white);

                    smallRemoteViews.setImageViewResource(R.id.noti_next, R.drawable.next_white);
                    smallRemoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous_white);
                }else {
                    remoteViews.setImageViewResource(R.id.noti_next, R.drawable.next);
                    remoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous);

                    smallRemoteViews.setImageViewResource(R.id.noti_next, R.drawable.next);
                    smallRemoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous);
                }

                remoteViews.setImageViewResource(R.id.noti_clear, R.drawable.noti_clear);

                Glide.with(context.getApplicationContext())
                        .load(MusicService.music.getAlbumArtUri())
                        .asBitmap()
                        .error(R.drawable.default_album_art)
                        .into(notificationTarget);
                Glide.with(context.getApplicationContext())
                        .load(MusicService.music.getAlbumArtUri())
                        .asBitmap()
                        .error(R.drawable.default_album_art)
                        .into(smallNotificationTarget);

            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(android.app.Notification.CATEGORY_SERVICE);
        }


        musicService.startForeground(1, notification);

        manager.notify(1, notification);
    }

}

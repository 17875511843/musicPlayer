package ironbear775.com.musicplayer.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.signature.ObjectKey;

import java.io.ByteArrayOutputStream;

import ironbear775.com.musicplayer.activity.MusicList;
import ironbear775.com.musicplayer.entity.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.service.MusicService;


/**
 * Created by ironbear on 2016/12/31.
 */

public class Notification {
    private final MusicService musicService;
    private static final String CHANNEL_ID = "MUSIC_CHANNEL_ID";

    public Notification(MusicService musicService) {
        this.musicService = musicService;
    }

    public void createNotification(final Context context, int id,
                                   Music music, Message msg, Bitmap bitmap) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        Intent intent = new Intent(context, MusicList.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_layout);

        final RemoteViews smallRemoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_small_layout);

        Intent intentLast = new Intent(context, MusicService.class);
        intentLast.setAction("PreMusic");
        PendingIntent LastPIntent = PendingIntent.getService(context, 0, intentLast, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_last, LastPIntent);
        smallRemoteViews.setOnClickPendingIntent(R.id.noti_last, LastPIntent);

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

        builder.setCustomBigContentView(remoteViews)
                .setContent(smallRemoteViews)
                .setContentIntent(pendingIntent)
                .setPriority(android.app.Notification.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(Color.BLACK);


        final android.app.Notification notification = builder.build();
        notification.flags |= android.app.Notification.FLAG_ONGOING_EVENT;

        NotificationTarget notificationTarget = new NotificationTarget(
                context,
                R.id.noti_album_art,
                remoteViews,
                notification,
                1
        );

        NotificationTarget smallNotificationTarget = new NotificationTarget(
                context,
                R.id.noti_album_art,
                smallRemoteViews,
                notification,
                1);


        if (MusicUtils.getInstance().enableColorNotification && msg.obj.equals(MusicUtils.getInstance().messageGood)) {
            remoteViews.setTextColor(R.id.noti_title, msg.arg1);
            remoteViews.setTextColor(R.id.noti_others, msg.arg2);
            smallRemoteViews.setTextColor(R.id.noti_title, msg.arg1);
            smallRemoteViews.setTextColor(R.id.noti_others, msg.arg2);
        } else if (MusicUtils.getInstance().isFlyme) {
            remoteViews.setTextColor(R.id.noti_title, ContextCompat.getColor(context, R.color.md_white_1000));
            remoteViews.setTextColor(R.id.noti_others, ContextCompat.getColor(context, R.color.md_white_1000));
            smallRemoteViews.setTextColor(R.id.noti_title, ContextCompat.getColor(context, R.color.md_white_1000));
            smallRemoteViews.setTextColor(R.id.noti_others, ContextCompat.getColor(context, R.color.md_white_1000));
        } else {
            remoteViews.setTextColor(R.id.noti_title, ContextCompat.getColor(context, R.color.black));
            remoteViews.setTextColor(R.id.noti_others, ContextCompat.getColor(context, R.color.black));
            smallRemoteViews.setTextColor(R.id.noti_title, ContextCompat.getColor(context, R.color.black));
            smallRemoteViews.setTextColor(R.id.noti_others, ContextCompat.getColor(context, R.color.black));
        }

        remoteViews.setTextViewText(R.id.noti_title, music.getTitle());
        smallRemoteViews.setTextViewText(R.id.noti_title, music.getTitle());
        remoteViews.setTextViewText(R.id.noti_others,
                music.getArtist());

        smallRemoteViews.setTextViewText(R.id.noti_others,
                music.getArtist());

        if (MusicUtils.getInstance().enableColorNotification) {
            remoteViews.setImageViewResource(R.id.noti_play_pause, id);
            smallRemoteViews.setImageViewResource(R.id.noti_play_pause, id);
        } else {
            if (MusicUtils.getInstance().isFlyme && id == R.drawable.footplay) {
                remoteViews.setImageViewResource(R.id.noti_play_pause, R.drawable.footplaywhite);
                smallRemoteViews.setImageViewResource(R.id.noti_play_pause, R.drawable.footplaywhite);
            } else if (MusicUtils.getInstance().isFlyme && id == R.drawable.footpause) {
                remoteViews.setImageViewResource(R.id.noti_play_pause, R.drawable.footpausewhite);
                smallRemoteViews.setImageViewResource(R.id.noti_play_pause, R.drawable.footpausewhite);
            } else {
                remoteViews.setImageViewResource(R.id.noti_play_pause, id);
                smallRemoteViews.setImageViewResource(R.id.noti_play_pause, id);
            }
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            Log.d("TAG", "createNotification:not null");
        } else {
            stream = null;
        }

        if (MusicService.mediaPlayer.isPlaying()) {

            if (MusicUtils.getInstance().enableColorNotification &&
                    msg.obj.equals(MusicUtils.getInstance().messageGood)) {
                Bitmap image = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565);
                image.eraseColor(msg.what);

                remoteViews.setImageViewBitmap(R.id.noti_background, image);
                smallRemoteViews.setImageViewBitmap(R.id.noti_background, image);
                remoteViews.setImageViewResource(R.id.noti_clear, R.drawable.cancel_black);

                remoteViews.setImageViewResource(R.id.noti_next, R.drawable.next);
                remoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous);

                smallRemoteViews.setImageViewResource(R.id.noti_next, R.drawable.next);
                smallRemoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous);

                if (stream != null) {
                    Glide.with(context.getApplicationContext())
                            .asBitmap()
                            .load(stream.toByteArray())
                            .apply(new RequestOptions()
                                    .centerCrop()
                                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis()))))
                            .into(notificationTarget);
                    Glide.with(context.getApplicationContext())
                            .asBitmap()
                            .load(stream.toByteArray())
                            .apply(new RequestOptions()
                                    .centerCrop()
                                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis()))))
                            .into(smallNotificationTarget);
                } else {
                    Glide.with(context.getApplicationContext())
                            .asBitmap()
                            .load(R.drawable.default_album_art)
                            .into(notificationTarget);
                    Glide.with(context.getApplicationContext())
                            .asBitmap()
                            .load(R.drawable.default_album_art)
                            .into(smallNotificationTarget);
                }
            } else {
                remoteViews.setImageViewResource(R.id.noti_background, R.color.transparent_color);
                smallRemoteViews.setImageViewResource(R.id.noti_background, R.color.transparent_color);

                if (MusicUtils.getInstance().isFlyme) {
                    remoteViews.setImageViewResource(R.id.noti_next, R.drawable.next_white);
                    remoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous_white);

                    smallRemoteViews.setImageViewResource(R.id.noti_next, R.drawable.next_white);
                    smallRemoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous_white);
                } else {
                    remoteViews.setImageViewResource(R.id.noti_next, R.drawable.next);
                    remoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous);

                    smallRemoteViews.setImageViewResource(R.id.noti_next, R.drawable.next);
                    smallRemoteViews.setImageViewResource(R.id.noti_last, R.drawable.previous);
                }

                remoteViews.setImageViewResource(R.id.noti_clear, R.drawable.noti_clear);

                if (stream != null) {
                    Glide.with(context)
                            .asBitmap()
                            .load(stream.toByteArray())
                            .apply(new RequestOptions()
                                    .centerCrop()
                                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis()))))
                            .into(notificationTarget);
                    Glide.with(context)
                            .asBitmap()
                            .load(stream.toByteArray())
                            .apply(new RequestOptions()
                                    .centerCrop()
                                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis()))))
                            .into(smallNotificationTarget);
                } else {
                    Glide.with(context.getApplicationContext())
                            .asBitmap()
                            .load(R.drawable.default_album_art)
                            .into(notificationTarget);
                    Glide.with(context.getApplicationContext())
                            .asBitmap()
                            .load(R.drawable.default_album_art)
                            .into(smallNotificationTarget);
                }
            }
        }


        musicService.startForeground(1, builder.build());

        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    public void createNotification(Context context, int id,
                                   Music music, Bitmap bitmap) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        Intent intent = new Intent(context, MusicList.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_layout);

        final RemoteViews smallRemoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_small_layout);

        Intent intentLast = new Intent(context, MusicService.class);
        intentLast.setAction("PreMusic");
        PendingIntent LastPIntent = PendingIntent.getService(context, 0, intentLast, 0);
        remoteViews.setOnClickPendingIntent(R.id.noti_last, LastPIntent);
        smallRemoteViews.setOnClickPendingIntent(R.id.noti_last, LastPIntent);

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

        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.notification_icon)
                .addAction(R.drawable.previous_white, "Previous", LastPIntent) // #0
                .addAction(id, "Pause", PlayPIntent)  // #1
                .addAction(R.drawable.next_white, "Next", NextPIntent)     // #2
                .addAction(R.drawable.cancel_white, "Clear", ClearPIntent)  // #3
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(new MediaSessionCompat(context, "mbr").getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setShowWhen(false)
                .setContentIntent(pendingIntent)
                .setContentTitle(music.getTitle())
                .setContentText(music.getArtist())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setLargeIcon(bitmap);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context, notificationManager);
        }

        musicService.startForeground(1, builder.build());

        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(Context context, NotificationManager notificationManager) {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID,
                            context.getString(R.string.app_name),
                            NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription(context.getString(R.string.app_name));

            notificationChannel.enableVibration(false);
            notificationChannel.enableLights(false);
            notificationChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}

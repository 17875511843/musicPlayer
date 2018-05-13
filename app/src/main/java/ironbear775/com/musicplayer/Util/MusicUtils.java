package ironbear775.com.musicplayer.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Fragment.MusicListFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ironbear on 2017/3/2.
 */

public class MusicUtils {

    private final String newPath = "http://www.kugou.com/yy/index.php?r=play/getdata&hash=";
    private final String newPathNetease = "http://music.163.com/api/song/lyric?id=";

    public int CLEAR = 0;
    public int FROM_ARTIST_PAGE = 1;
    public int FROM_ARTIST_DETAIl_PAGE = 2;
    public int FROM_ALBUM_PAGE = 3;
    public int fromWhere;
    private final String apkFolder = "MusicPlayer/apk";
    private final String lyricFolder = "MusicPlayer/lyric";


    public final String localPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    public final String artistFolder = "MusicPlayer/artist";
    public final String albumFolder = "MusicPlayer/album";
    public final String MY_ALIPAY_QRCODE = "https://qr.alipay.com/tsx06081gjjlyskuhkvxhef";
    public final int FROM_ADAPTER = 1;
    public final int FROM_MAINIMAGE = 2;
    public final int FROM_SERVICE = 3;
    public final int FROM_FOOTBAR = 4;
    public boolean enableDefaultCover = false;
    public boolean enableColorNotification = false;
    public boolean useOldStyleNotification = false;
    public boolean enableSwipeGesture = true;
    public boolean keepScreenOn = false;
    public boolean loadWebLyric = true;
    public boolean enableEqualizer = false;
    public boolean enableShuffle = true;
    public boolean enableTranslateLyric = true;
    public boolean autoSwitchNightMode = true;
    public boolean isFlyme = false;
    public boolean isPlayed = false;
    public int playPage = 0; //当前点击播放的页面 1.MusicListFragment 2.MusicRecentAddedFragment
    // 3.ArtistDetailFragment 4.AlbumDetailFragment 5.PlaylistDetailFragment 6.FolderDetailFragment 7.MusicList
    public boolean isArtistlistFragmentOpen = false;
    public int sleepTime = 30;
    public int themeName = R.string.color_Pink;
    public int checkPosition = 0;
    public int downloadArtist = 2;//0,1,2 never,data,wifi
    public int downloadAlbum = 0;//0,1,2 never,data,wifi
    public int filterNum = 2; // 0,1,2,3,4,5,6 0,15s,20s,30s,40s,50s,60s
    public int launchPage = 1;//1,2,3,4,5,6 music,artist,album,playlist,recent,folder
    public int updateMusic = 0;//0,1 netease,kugou
    public int loadlyric = 0;//0,1,2 netease frist,netease,kugou
    public int pos = 0;
    public final String messageGood = "good";
    public final String messageBad = "error";
    public final String messageNull = "null";
    public final int[] time = {0, 15000, 20000, 30000, 40000, 50000, 60000};
    public boolean isSelectAll = false;
    private File appDir;
    private OkHttpClient client;
    private Request.Builder requestBuilder;
    private ImageView mImageView;
    private Drawable mPlaceHolder;
    private Request.Builder builder;
    private Call call, call2;

    private static MusicUtils instance;

    private MusicUtils() {
        client = new OkHttpClient();
        requestBuilder = null;
    }

    public static synchronized MusicUtils getInstance() {
        if (MusicUtils.instance == null)
            instance = new MusicUtils();
        return instance;
    }

    public void startMusic(Context context, int position, int progress, ArrayList<Music> list) {
        Intent serviceIntent = new Intent(context, MusicService.class);

        serviceIntent.setAction("musiclist");
        serviceIntent.putExtra("musicList", list);
        serviceIntent.putExtra("musicPosition", position);
        serviceIntent.putExtra("musicProgress", progress);

        context.startService(serviceIntent);
    }

    public void startMusic(Context context, int position, int progress, int from) {
        Intent serviceIntent = new Intent(context, MusicService.class);//intent转跳

        serviceIntent.setAction("musiclist");
        serviceIntent.putExtra("from", from); // 服务通过from判断使用那个页面的列表进行播放
        serviceIntent.putExtra("musicPosition", position); //播放歌曲在列表中的位置
        serviceIntent.putExtra("musicProgress", progress); //播放歌曲当前的进度

        if (from == 10)
            playPage = 7;

        context.startService(serviceIntent); //开启服务

        context.sendBroadcast(new Intent("show footbar"));
    }

    //设置底部栏专辑封面
    public void getFootAlbumArt(Context context, int pos1, ArrayList<Music> musicList, int from) {
        setAlbumCoverToFootAndHeader(context, musicList.get(pos1), from);
    }

    //生成随机数
    public int createRandom(ArrayList<Music> musicList) {
        Random random = new Random();
        int randomInt;
        if (musicList.size() == 1) {
            randomInt = 0;
        } else {
            randomInt = random.nextInt(musicList.size());
        }
        return randomInt;
    }

    //randInt()函数的实现
    private int randInt(int x, int y) {
        //return (int)(Math.random()*100%(y-x+1)+x);//这个算法还是不好
        return (int) (Math.random() * (y - x + 1) + x);
    }

    //实现swap()方法
    private void swap(ArrayList<Music> arrayList, int x, int y) {
        Music temp;
        temp = arrayList.get(x);
        arrayList.set(x, arrayList.get(y));
        arrayList.set(y, temp);
    }

    public ArrayList<Music> createShuffleList(ArrayList<Music> musicList) {
        ArrayList<Music> tempList = (ArrayList<Music>) musicList.clone();

        for (int i = 0; i < tempList.size(); i++) {
            swap(tempList, i, randInt(i, tempList.size() - 1));
        }

        return tempList;
    }

    public ArrayList<Music> arrayList = new ArrayList<>();

    public void shufflePlay(Context context, ArrayList<Music> musicList, int from) {
        if (musicList.size() >= 1) {

            Intent intent2 = new Intent("cycle list");
            intent2.putExtra("from", from);
            context.sendBroadcast(intent2);

            arrayList = createShuffleList(musicList);

            startMusic(context, 0, 0, 9);

            MusicService.isRandom = true;
            SharedPreferences.Editor editor = context.getSharedPreferences("data", MODE_PRIVATE).edit();

            editor.putInt("progress", MusicService.mediaPlayer.getCurrentPosition());
            editor.putBoolean("isRandom", MusicService.isRandom);
            editor.apply();
            Intent intent = new Intent("set footBar");
            intent.putExtra("footTitle", arrayList.get(0).getTitle());
            intent.putExtra("footArtist", arrayList.get(0).getArtist());
            Intent intent1 = new Intent("set PlayOrPause");
            intent1.putExtra("PlayOrPause", R.drawable.pause_to_play_white_anim);
            context.sendBroadcast(intent);
            context.sendBroadcast(intent1);
            getFootAlbumArt(context, 0, arrayList, from);

        }
    }

    public void playAll(Context context, ArrayList<Music> musicList, int from) {
        startMusic(context, 0, 0, from);
        MusicService.isRandom = false;
        Intent intent = new Intent("set footBar");
        Intent intent1 = new Intent("set PlayOrPause");
        intent.putExtra("footTitle", musicList.get(0).getTitle());
        intent.putExtra("footArtist", musicList.get(0).getArtist());
        intent1.putExtra("PlayOrPause", R.drawable.play_to_pause_white_anim);
        context.sendBroadcast(intent);
        context.sendBroadcast(intent1);

        getFootAlbumArt(context, 0, musicList, from);
    }


    public void selectAll(Context context, Set<Integer> positionSet, ArrayList<Music> list) {
        for (int position = 0; position < list.size(); position++) {
            positionSet.add(position);
        }
        if (positionSet.size() == 0) {
            MusicList.actionMode.finish();
        } else {
            String locale = Locale.getDefault().toString();
            if (locale.equals("zh_CN")) {
                MusicList.actionMode.setTitle(context.getResources().getString(R.string.selected) +
                        " " + positionSet.size());
            } else {
                MusicList.actionMode.setTitle(positionSet.size() +
                        " " + context.getResources().getString(R.string.selected));
            }
        }
        isSelectAll = true;
    }

    public void addOrRemoveItem(Context context, int position, Set<Integer> positionSet,
                                RecyclerView.Adapter adapter) {
        if (isSelectAll) {
            positionSet = MusicList.listPositionSet;
        }
        if (positionSet.contains(position)) {
            positionSet.remove(position);
        } else {
            positionSet.add(position);
        }
        if (positionSet.size() == 0) {
            MusicList.actionMode.finish();
        } else {
            String locale = Locale.getDefault().toString();
            if (locale.equals("zh_CN")) {
                MusicList.actionMode.setTitle(context.getResources().getString(R.string.selected) +
                        " " + positionSet.size());
            } else {
                MusicList.actionMode.setTitle(positionSet.size() +
                        " " + context.getResources().getString(R.string.selected));
            }
            adapter.notifyItemChanged(position);
        }
    }

    public void artistImage(ImageView imageView, final Context context,
                            final String keyWord, final Drawable placeHolder,
                            final Activity uiactivity) {
        if (downloadArtist != 0) {
            mImageView = imageView;
            mPlaceHolder = placeHolder;

            appDir = new File(localPath, artistFolder);

            String newKeyWord;
            if (keyWord.contains("/")) {
                newKeyWord = keyWord.replace("/", "_");
            } else {
                newKeyWord = keyWord;
            }

            final File file = new File(appDir, newKeyWord);

            if (file.exists() && !isImageGood(file)) {
                file.delete();
            }
            URL url = null;
            try {
                String apiKey = "0c26dc3c5612fd63122ccf5bf11f78f9";
                String path = "http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=";
                url = new URL(path + keyWord + "&api_key=" + apiKey + "&format=json");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            if (url != null && !file.exists()) {
                requestBuilder = new Request.Builder()
                        .url(url);
            }
            Call call = null;
            if (requestBuilder != null) {

                call = client.newCall(requestBuilder.build());
            }
            if (call != null) {
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.d("Fail", "request failed");
                    }

                    @Override
                    public void onResponse(@NonNull Call call,
                                           @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String result = response.body().string();

                            final String ImageUrl = parseJson(result);

                            if (!appDir.exists()) {
                                appDir.mkdirs();
                            }

                            if (ImageUrl != null && !ImageUrl.equals("")) {
                                InputStream is;

                                URL url = new URL(ImageUrl);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                is = new BufferedInputStream(connection.getInputStream());

                                BitmapFactory.Options opt = new BitmapFactory.Options();

                                opt.inPreferredConfig = Bitmap.Config.RGB_565;
                                Bitmap bitmap = BitmapFactory.decodeStream(is, null, opt);

                                is.close();

                                if (bitmap != null) {

                                    FileOutputStream fos = null;
                                    try {
                                        fos = new FileOutputStream(file);
                                        if (bitmap.getByteCount() > 3000000) {
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, fos);
                                        } else if (bitmap.getByteCount() > 2500000) {
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, fos);
                                        } else if (bitmap.getByteCount() > 2000000) {
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                                        } else if (bitmap.getByteCount() < 1500000) {
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos);
                                        } else if (bitmap.getByteCount() < 1000000) {
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                                        }
                                        fos.flush();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } finally {
                                        try {
                                            if (fos != null) {
                                                fos.close();
                                            }
                                            if (!bitmap.isRecycled()) {
                                                bitmap.recycle();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            uiactivity.runOnUiThread(() -> {
                                if (mImageView.getTag(R.id.artist_url).equals(keyWord)) {
                                    if (file.exists()) {
                                        Glide.with(context)
                                                .asBitmap()
                                                .load(file)
                                                .apply(new RequestOptions()
                                                        .placeholder(mPlaceHolder)
                                                        .centerCrop())
                                                .into(mImageView);
                                    }
                                }
                            });
                        }
                    }

                });
            }
        } else {
            closeDownloadArtistImage();
        }
    }

    public void updateArtist(ImageView imageView, final Context context,
                             final String keyWord, final Drawable placeHolder,
                             final Activity uiactivity) {
        if ((haveWIFI(context) && downloadArtist == 2) ||
                (haveData(context) && downloadArtist == 1) ||
                (haveWIFI(context) && downloadArtist == 1)) {
            appDir = new File(localPath, albumFolder);

            String newKeyWord;
            if (keyWord.contains("/")) {
                newKeyWord = keyWord.replace("/", "_");
            } else {
                newKeyWord = keyWord;
            }

            final File file = new File(appDir, newKeyWord);

            if (file.exists()) {
                file.delete();
            }
            artistImage(imageView, context, keyWord, placeHolder, uiactivity);
        }
    }

    public void closeDownloadArtistImage() {
        client.dispatcher().cancelAll();
    }

    public void deleteDownloadImage(File root) {
        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.isDirectory()) { // 判断是否为文件夹
                    if (f.exists()) { // 判断是否存在
                        deleteDownloadImage(f);
                        try {
                            f.delete();
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    private String parseJson(String json) {
        String imageUrl = null;
        try {
            JSONObject main = new JSONObject(json);
            if (main.has("artist")) {
                JSONObject artist = main.getJSONObject("artist");

                JSONArray image = artist.getJSONArray("image");
                if (image.length() > 0) {
                    JSONObject finalImage = (JSONObject) image.get(4);
                    imageUrl = finalImage.getString("#text");

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return imageUrl;
    }


    private List<String> parseLyricJson(String json) {
        List<String> key = new ArrayList<>();
        String j = json.substring(41, json.length() - 1);

        try {
            JSONObject main = new JSONObject(j);
            if (main.has("data")) {
                JSONObject data = main.getJSONObject("data");

                if (data.has("lists") && !data.get("lists").toString().equals("{}")) {
                    JSONArray lists = data.getJSONArray("lists");
                    if (lists.length() > 0) {

                        JSONObject list = (JSONObject) lists.get(0);

                        if (list.has("Grp") && !list.get("Grp").toString().equals("{}")) {

                            JSONArray grp = list.getJSONArray("Grp");

                            JSONObject object = grp.getJSONObject(0);

                            String album;
                            if (object.getString("AlbumID").equals("")) {
                                album = " ";
                            } else {
                                album = list.getString("AlbumID");
                            }
                            key.add(0, album);
                            key.add(1, object.getString("FileHash"));
                        } else if (list.has("AlbumID") && list.has("FileHash")) {
                            String album;
                            if (list.getString("AlbumID").equals("")) {
                                album = " ";
                            } else {
                                album = list.getString("AlbumID");
                            }
                            key.add(0, album);
                            key.add(1, list.getString("FileHash"));
                        }
                    }
                } else {
                    key.add(0, "");
                    key.add(1, "");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return key;
    }

    public boolean haveWIFI(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = null;
        if (connectivityManager != null) {
            activeNetInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public boolean haveData(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = null;
        if (connectivityManager != null) {
            activeNetInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public boolean isImageGood(File file) {
        BitmapFactory.Options options;
        options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(file.getPath(), options); //filePath代表图片路径
        return !(options.mCancel || options.outWidth == -1
                || options.outHeight == -1);
    }

    public void getWebLyric(Context context, String songTitle, String singer,
                            boolean showLyric, boolean embed) {
        String newSinger, newSongTitle;
        //替换特殊字符
        if (singer.contains("&"))
            newSinger = singer.replaceFirst("&", "%26");
        else
            newSinger = singer;

        if (songTitle.contains("&"))
            newSongTitle = songTitle.replaceFirst("&", "%26");
        else
            newSongTitle = songTitle;
        OkHttpClient client = new OkHttpClient();
        try {

            //接口API
            String Path = "http://music.163.com/api/search/pc?s=";
            URL u = new URL(Path + newSongTitle + " " + newSinger
                    + " &type=1");

            //请求头参数
            FormBody body = new FormBody.Builder()
                    .add("Host", "music.163.com")
                    .add("Connection", "keep-alive")
                    .add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .add("Referer", "http://music.163.com/")
                    .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36")
                    .add("Connection", "keep-alive")
                    .build();

            builder = new Request.Builder()
                    .post(body).url(u);

            cancelNetCall();
            call = client.newCall(builder.build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    sendLyricNotFoundBroadcast(context, showLyric, embed, false);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                    String result = response.body().string();
                    //Log.d("TAG", "getWebLyric: " + result);

                    String id = parseJsonFromNetease(result, songTitle);

                    //Log.d("ID", "" + id);

                    if (id != null) {
                        URL url = new URL(newPathNetease + id + "&lv=1&kv=1&tv=-1");
                        builder = new Request.Builder().url(url);
                        call2 = client.newCall(builder.build());
                        call2.enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call,
                                                  @NonNull IOException e) {
                                getWebLyricFromKugou(context, songTitle, singer, showLyric, embed, false);
                            }

                            @Override
                            public void onResponse(@NonNull Call call,
                                                   @NonNull Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    String webLyric = response.body().string();
                                    try {
                                        JSONObject main = new JSONObject(webLyric);

                                        if (enableTranslateLyric && main.has("tlyric")) {
                                            showTranslateLyric(context, main, songTitle, singer, showLyric, embed, false);
                                        } else if (main.has("lrc")) {
                                            showOriginalLyric(context, main, songTitle, singer, showLyric, embed, false);
                                        } else {
                                            getWebLyricFromKugou(context, songTitle, singer, showLyric, embed, false);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    } else {
                        getWebLyricFromKugou(context, songTitle, singer, showLyric, embed, false);
                    }
                }
            });

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public void getLyricFromNeteaseById(Context context, String id, String songTitle, String singer,
                                        boolean showLyric, boolean embed,
                                        boolean isUpdate) {
        if (id != null) {
            try {
                URL url = new URL(newPathNetease + id + "&lv=1&kv=1&tv=-1");

                builder = new Request.Builder().url(url);
                call2 = client.newCall(builder.build());
                call2.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        sendLyricNotFoundBroadcast(context, showLyric, embed, isUpdate);
                    }

                    @Override
                    public void onResponse(@NonNull Call call,
                                           @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String webLyric = response.body().string();

                            //Log.d("web", webLyric);
                            try {
                                JSONObject main = new JSONObject(webLyric);

                                if (enableTranslateLyric
                                        && main.has("tlyric")) {
                                    showTranslateLyric(context, main, songTitle, singer, showLyric, embed, isUpdate);
                                } else if (main.has("lrc")) {
                                    showOriginalLyric(context, main, songTitle, singer, showLyric, embed, isUpdate);
                                } else {
                                    sendLyricNotFoundBroadcast(context, showLyric, embed, isUpdate);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            sendLyricNotFoundBroadcast(context, showLyric, embed, isUpdate);
        }
    }

    public void getWebLyricFromNetease(Context context, String songTitle, String singer,
                                       boolean showLyric, boolean embed,
                                       boolean isUpdate) {
        OkHttpClient client = new OkHttpClient();

        String newSinger, newSongTitle;
        if (singer.contains("&"))
            newSinger = singer.replaceFirst("&", "%26");
        else
            newSinger = singer;

        if (songTitle.contains("&"))
            newSongTitle = songTitle.replaceFirst("&", "%26");
        else
            newSongTitle = songTitle;

        try {

            String Path = "http://music.163.com/api/search/pc?s=";
            URL u = new URL(Path + newSongTitle + " " + newSinger
                    + " &type=1");
            FormBody body = new FormBody.Builder()
                    .add("Host", "music.163.com")
                    .add("Connection", "keep-alive")
                    .add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .add("Referer", "http://music.163.com/")
                    .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36")
                    .add("Connection", "keep-alive")
                    .build();
            builder = new Request.Builder()
                    .post(body).url(u);

            cancelNetCall();
            call = client.newCall(builder.build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Intent intent = new Intent("load lyric failed");
                    context.sendBroadcast(intent);
                }

                @Override
                public void onResponse(@NonNull Call call,
                                       @NonNull Response response) throws IOException {
                    String result = response.body().string();

                    //Log.d("Result", "Result: " + result);
                    String id = parseJsonFromNetease(result, songTitle);

                    //Log.d("ID", "" + id);
                    getLyricFromNeteaseById(context, id, songTitle, singer, showLyric, embed, isUpdate);
                }
            });

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public void getWebLyricFromKugou(Context context, String songTitle, String singer,
                                     boolean showLyric, boolean embed,
                                     boolean isUpdate) {
        OkHttpClient client = new OkHttpClient();
        String newSinger, newSongTitle;
        if (singer.contains("&"))
            newSinger = singer.replaceFirst("&", "%26");
        else
            newSinger = singer;

        if (songTitle.contains("&"))
            newSongTitle = songTitle.replaceFirst("&", "%26");
        else
            newSongTitle = songTitle;
        try {

            String hashPath = "http://songsearch.kugou.com/song_search_v2?callback=jQuery19102275292550172583_1493445518059&keyword=";
            URL u = new URL(hashPath + newSongTitle + " " + newSinger
                    + "&page=1&pagesize=30&userid=-1&clientver=&platform=WebFilter&tag=em&filter=2&iscorrection=1&privilege_filter=0&_=1493445518061");
            builder = new Request.Builder().url(u);

            cancelNetCall();
            call = client.newCall(builder.build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    sendLyricNotFoundBroadcast(context, showLyric, embed, isUpdate);
                }

                @Override
                public void onResponse(@NonNull Call call,
                                       @NonNull Response response) throws IOException {
                    String result = response.body().string();
                    String hashkey;
                    String album_id;

                    List<String> key = parseLyricJson(result);

                    if (key.size() > 0) {
                        album_id = key.get(0);
                        hashkey = key.get(1);

                        if (album_id != null && hashkey != null) {

                            URL url = new URL(newPath + hashkey + "&album_id=" + album_id);
                            Request.Builder builder = new Request.Builder().url(url);
                            Call call2 = client.newCall(builder.build());
                            call2.enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call,
                                                      @NonNull IOException e) {
                                    sendLyricNotFoundBroadcast(context, showLyric, embed, isUpdate);
                                }

                                @Override
                                public void onResponse(@NonNull Call call,
                                                       @NonNull Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        String webLyric = response.body().string();

                                        String lyric;
                                        try {
                                            JSONObject main = new JSONObject(webLyric);
                                            if (main.has("data") && !main.get("data").toString().equals("[]")) {
                                                JSONObject data = main.getJSONObject("data");

                                                if (data.has("lyrics") && !data.get("lyrics").toString().equals("")) {
                                                    lyric = data.getString("lyrics");

                                                    if (lyric != null) {
                                                        saveLyricFile(context, songTitle, singer, showLyric, embed, lyric, false, isUpdate);
                                                    } else {
                                                        sendLyricNotFoundBroadcast(context, showLyric, embed, isUpdate);
                                                    }
                                                } else {
                                                    sendLyricNotFoundBroadcast(context, showLyric, embed, isUpdate);

                                                }

                                            } else {
                                                sendLyricNotFoundBroadcast(context, showLyric, embed, isUpdate);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            });
                        } else {
                            sendLyricNotFoundBroadcast(context, showLyric, embed, isUpdate);
                        }
                    }
                }
            });

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void sendLyricNotFoundBroadcast(Context context, boolean showLyric, boolean embed, boolean isUpdate) {
        if (showLyric && isUpdate) {
            Intent intent = new Intent("update lyric failed");
            context.sendBroadcast(intent);
        }
        if (showLyric) {
            Intent intent = new Intent("load lyric failed");
            context.sendBroadcast(intent);
        }

        if (embed) {
            Intent intent = new Intent("embed lyric");
            intent.putExtra("lyric", "");
            context.sendBroadcast(intent);
        }
    }

    private void showTranslateLyric(Context context, JSONObject main, String songTitle, String singer,
                                    boolean showLyric, boolean embed,
                                    boolean isUpdate) {
        try {

            String oLyric;
            String tLyric;
            JSONObject tLrc = main.getJSONObject("tlyric");

            if (tLrc.has("lyric")
                    && !tLrc.get("lyric").toString().equals("")
                    && !tLrc.get("lyric").toString().equalsIgnoreCase("null")) {
                tLyric = tLrc.getString("lyric");
            } else {
                showOriginalLyric(context, main, songTitle, singer, showLyric, embed, isUpdate);
                return;
            }

            JSONObject oLrc = main.getJSONObject("lrc");
            if (oLrc.has("lyric") && !oLrc.get("lyric").toString().equals("")) {

                oLyric = oLrc.getString("lyric");

                String[] tArray = tLyric.split("\\n");
                String[] oArray = oLyric.split("\\n");
                ArrayList<String> oArrayList = formatLyric(oArray);
                ArrayList<String> tArrayList = formatLyric(tArray);

                saveLyricFile(context, songTitle, singer, showLyric,
                        embed, mergeLyric(oArrayList, tArrayList), true, isUpdate);
            } else {
                saveLyricFile(context, songTitle, singer, showLyric,
                        embed, tLyric, true, isUpdate);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //切分一行中含有多个时间轴的歌词
    private void splitMultiTime(String lyric, ArrayList<String> arrayList, int count) {
        int p = lyric.indexOf("]");
        if (count > 0) {
            if (p + 1 < lyric.length()) {
                char c = lyric.charAt(0);
                if (c == '[') {
                    count--;
                    arrayList.add(lyric.substring(0, p + 1) + lyric.substring(lyric.lastIndexOf("]") + 1));
                    splitMultiTime(lyric.substring(p + 1), arrayList, count);
                }
            }
        }
    }

    private void showOriginalLyric(Context context, JSONObject main, String songTitle, String singer,
                                   boolean showLyric, boolean embed,
                                   boolean isUpdate) {
        String oLyric;
        try {
            JSONObject lrc = main.getJSONObject("lrc");
            if (lrc.has("lyric") && !lrc.get("lyric").toString().equals("")) {
                oLyric = lrc.getString("lyric");

                if (oLyric != null) {
                    String[] oArray = oLyric.split("\\n");

                    boolean canScroll = false;
                    for (String anOArray : oArray) {
                        if (anOArray.contains("[")
                                && anOArray.contains("]")
                                && parseLine(anOArray) != 0) {
                            canScroll = true;
                            break;
                        }
                    }
                    if (canScroll) {

                        ArrayList<String> arrayList = formatLyric(oArray);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j < arrayList.size(); j++) {
                            stringBuilder.append(arrayList.get(j)).append("\n");
                        }

                        saveLyricFile(context, songTitle, singer, showLyric, embed,
                                stringBuilder.toString(), false, isUpdate);

                    } else
                        sendLyricNotFoundBroadcast(context, showLyric, embed, isUpdate);
                } else {
                    sendLyricNotFoundBroadcast(context, showLyric, embed, isUpdate);
                }
            } else {
                sendLyricNotFoundBroadcast(context, showLyric, embed, isUpdate);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> formatLyric(String[] array) {

        ArrayList<String> lyricList = new ArrayList<>(Arrays.asList(array));
        ListIterator<String> iterator = lyricList.listIterator();
        //去除空行
        while (iterator.hasNext()) {
            if (iterator.next().equals("")) {
                iterator.remove();
            }
        }

        //去除[[为[
        for (int i = 0; i < lyricList.size(); i++) {
            if (lyricList.get(i) != null) {
                int index = lyricList.get(i).indexOf("[") + 1;
                if (lyricList.get(i).charAt(index) == '[') {
                    lyricList.set(i, lyricList.get(i).substring(index));
                }
            }
        }

        //将有换行符的歌词合并，负责换行符会使歌词读取失败
        for (int i = 0; i < lyricList.size(); i++) {
            if (i < lyricList.size() - 1)
                if (!lyricList.get(i + 1).contains("[")
                        && !lyricList.get(i + 1).contains("]")
                        && lyricList.get(i).contains("[")
                        && lyricList.get(i).contains("]")
                        && parseLine(lyricList.get(i)) != 0) {
                    lyricList.set(i, lyricList.get(i) + " " + lyricList.get(i + 1));
                    lyricList.remove(i + 1);
                }
        }

        //将一行中含有多个时间轴的歌词分行处理
        for (int i = 0; i < lyricList.size(); i++) {
            if (i < lyricList.size() - 1) {
                if (lyricList.get(i).contains("[")
                        && lyricList.get(i).contains("]")
                        && parseLine(lyricList.get(i)) != 0) {
                    int count = 0;

                    for (int j = -1; j <= lyricList.get(i).lastIndexOf("["); ++j) {
                        j = lyricList.get(i).indexOf("[", j);
                        count++;
                    }

                    if (count > 1) {
                        splitMultiTime(lyricList.get(i), lyricList, count);
                        lyricList.remove(i);
                        i--;
                    }
                }
            }
        }

        //将歌词规整化 统一为[00:00.000]或[00:00:000]的形式
        for (int i = 0; i < lyricList.size(); i++) {
            if (parseLine(lyricList.get(i)) == 2) {//[00:00.00]
                if (lyricList.get(i).contains("]")) {
                    int flag = lyricList.get(i).lastIndexOf("]");
                    lyricList.set(i, lyricList.get(i).substring(0, flag)
                            + "0" + lyricList.get(i).substring(flag));
                }
            } else if (parseLine(lyricList.get(i)) == 3) {//[00:000]
                if (lyricList.get(i).contains("]")) {
                    int flag = lyricList.get(i).lastIndexOf("]");
                    lyricList.set(i, lyricList.get(i).substring(0, flag - 1)
                            + ".000" + lyricList.get(i).substring(flag));
                }
            } else if (parseLine(lyricList.get(i)) == 5) {//[00:00:00]
                if (lyricList.get(i).contains("]")) {
                    int flag = lyricList.get(i).lastIndexOf("]");
                    lyricList.set(i, lyricList.get(i).substring(0, flag)
                            + "0" + lyricList.get(i).substring(flag));
                }
            } else if (parseLine(lyricList.get(i)) == 6) {//[00:00]
                if (lyricList.get(i).contains("]")) {
                    int flag = lyricList.get(i).lastIndexOf("]");
                    lyricList.set(i, lyricList.get(i).substring(0, flag)
                            + ".000" + lyricList.get(i).substring(flag));
                }
            }

        }

        return lyricList;
    }

    //合并生成双语歌词
    private String mergeLyric(ArrayList<String> oArrayList, ArrayList<String> tArrayList) {
        StringBuilder stringBuffer = new StringBuilder();
        boolean getTLine = false;

        //去除无时间轴的歌词
        switch (parseLine(oArrayList.get(oArrayList.size() - 1))) {
            case 1:
                for (int i = 0; i < oArrayList.size(); i++) {
                    Matcher lineMatcher = Pattern.compile(
                            "((\\[\\d\\d:\\d\\d\\.\\d\\d\\d])+)(.+)").matcher(oArrayList.get(i));
                    Matcher timeMatcher = Pattern.compile(
                            "\\[(\\d\\d):(\\d\\d)\\.(\\d\\d\\d)]").matcher(oArrayList.get(i));

                    if (!lineMatcher.matches() && !timeMatcher.matches()) {
                        oArrayList.remove(i);
                        i--;
                    }

                }

                for (int i = 0; i < tArrayList.size(); i++) {
                    Matcher lineMatcher = Pattern.compile(
                            "((\\[\\d\\d:\\d\\d\\.\\d\\d\\d])+)(.+)").matcher(tArrayList.get(i));
                    Matcher timeMatcher = Pattern.compile(
                            "\\[(\\d\\d):(\\d\\d)\\.(\\d\\d\\d)]").matcher(tArrayList.get(i));

                    if (!lineMatcher.matches() && !timeMatcher.matches()) {
                        tArrayList.remove(i);
                        i--;
                    }

                }
                break;
            case 4:
                for (int i = 0; i < oArrayList.size(); i++) {
                    Matcher lineMatcher = Pattern.compile(
                            "((\\[\\d\\d:\\d\\d:\\d\\d\\d])+)(.+)").matcher(oArrayList.get(i));
                    Matcher timeMatcher = Pattern.compile(
                            "\\[(\\d\\d):(\\d\\d):(\\d\\d\\d)]").matcher(oArrayList.get(i));

                    if (!lineMatcher.matches() && !timeMatcher.matches()) {
                        oArrayList.remove(i);
                        i--;
                    }
                }
                for (int i = 0; i < tArrayList.size(); i++) {
                    Matcher lineMatcher = Pattern.compile(
                            "((\\[\\d\\d:\\d\\d:\\d\\d\\d])+)(.+)").matcher(tArrayList.get(i));
                    Matcher timeMatcher = Pattern.compile(
                            "\\[(\\d\\d):(\\d\\d):(\\d\\d\\d)]").matcher(tArrayList.get(i));

                    if (!lineMatcher.matches() && !timeMatcher.matches()) {
                        tArrayList.remove(i);
                        i--;
                    }
                }
                break;
        }

        //合并歌词
        for (int i = 0; i < oArrayList.size(); i++) {
            for (int j = 0; j < tArrayList.size(); j++) {
                //根据原歌词的时间轴时间匹配翻译歌词相应的歌词行并合并
                if (oArrayList.get(i).substring(0, oArrayList.get(i).lastIndexOf("]"))
                        .equals(tArrayList.get(j)
                                .substring(0, tArrayList.get(j).lastIndexOf("]")))) {
                    stringBuffer.append(oArrayList.get(i)).append("\n");
                    stringBuffer.append(tArrayList.get(j)).append("\n");
                    getTLine = true;
                    break;
                } else {
                    getTLine = false;
                }
            }
            //未找到相应时间轴的翻译歌词，则只添加原歌词
            if (!getTLine) {
                stringBuffer.append(oArrayList.get(i)).append("\n");
            }
        }
        return stringBuffer.toString();
    }


    private void saveLyricFile(Context context, String songTitle, String singer,
                               boolean showLyric, boolean embed, String lyric,
                               boolean isTlyric, boolean isUpdate) {
        try {
            String newSongTitle, newSinger;

            newSongTitle = songTitle;

            if (songTitle.contains("/")) {
                newSongTitle = songTitle.replace("/", "_");
            }

            newSinger = singer;

            if (singer.contains("/")) {
                newSinger = singer.replace("/", "_");
            }

            File dir = new File(localPath, lyricFolder);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file;
            if (isTlyric)
                file = new File(dir, newSongTitle + "_" + newSinger + "_translate" + ".lrc");
            else
                file = new File(dir, newSongTitle + "_" + newSinger + ".lrc");

            FileOutputStream fos;
            fos = new FileOutputStream(file);
            fos.write(lyric.getBytes());
            fos.close();

            if (showLyric && isUpdate) {
                Intent intent = new Intent("update lyric");
                intent.putExtra("lyric", lyric);
                context.sendBroadcast(intent);
            }
            if (showLyric) {
                Intent intent = new Intent("show lyric");
                intent.putExtra("lyric", lyric);
                context.sendBroadcast(intent);
            }
            if (embed) {
                Intent intent = new Intent("embed lyric");
                intent.putExtra("lyric", lyric);
                context.sendBroadcast(intent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int parseLine(String line) {
        if (TextUtils.isEmpty(line)) {
            return 0;
        }

        line = line.trim();
        //1
        Matcher threeMsLineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d\\d\\d])+)(.+)").matcher(line);
        //2
        Matcher twoMsLineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d\\d])+)(.+)").matcher(line);
        //3
        Matcher noMSThreeSecondLineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d\\d])+)(.+)").matcher(line);
        //4
        Matcher threesWithColonLineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d:\\d\\d\\d])+)(.+)").matcher(line);
        //5
        Matcher twoMsWithColonLineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d:\\d\\d])+)(.+)").matcher(line);
        //6
        Matcher noMSTwoSecondLineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d])+)(.+)").matcher(line);

        Matcher threeMsTimeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d\\d\\d)]").matcher(line);
        Matcher twoMsTimeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d\\d)]").matcher(line);
        Matcher noMsThreeSecondTimeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d\\d)]").matcher(line);
        Matcher threesWithColonTimeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d):(\\d\\d\\d)]").matcher(line);
        Matcher twoMsWithColonTimeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d):(\\d\\d)]").matcher(line);
        Matcher noMsTwoSecondTimeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)]").matcher(line);

        if (threeMsLineMatcher.matches() || threeMsTimeMatcher.matches())
            return 1;
        else if (twoMsLineMatcher.matches() || twoMsTimeMatcher.matches())
            return 2;
        else if (noMSThreeSecondLineMatcher.matches() || noMsThreeSecondTimeMatcher.matches())
            return 3;
        else if (threesWithColonLineMatcher.matches() || threesWithColonTimeMatcher.matches())
            return 4;
        else if (twoMsWithColonLineMatcher.matches() || twoMsWithColonTimeMatcher.matches())
            return 5;
        else if (noMSTwoSecondLineMatcher.matches() || noMsTwoSecondTimeMatcher.matches())
            return 6;
        else {
            return 0;
        }
    }

    private String parseJsonFromNetease(String json, String title) {
        String id = null;
        try {
            JSONObject main = new JSONObject(json);
            if (main.has("result")) {

                JSONObject result = main.getJSONObject("result");

                String newTitle;

                if (result.has("songs")) {
                    JSONArray songs = result.getJSONArray("songs");

                    if (songs.length() > 0) {
                        JSONObject song;

                        for (int i = 0; i < songs.length(); i++) {
                            song = (JSONObject) songs.get(i);
                            if (song.get("name").toString().equalsIgnoreCase(title)) {
                                return song.getString("id");
                            }
                        }

                        if (title.contains("(") && title.contains(")")) {
                            newTitle = title.substring(0, title.lastIndexOf("("));
                            for (int i = 0; i < songs.length(); i++) {
                                song = (JSONObject) songs.get(i);
                                if (song.get("name").toString().equalsIgnoreCase(newTitle)) {
                                    return song.getString("id");
                                }
                            }
                        }

                        if (title.contains("（") && title.contains("）")) {
                            newTitle = title.substring(0, title.lastIndexOf("（"));
                            for (int i = 0; i < songs.length(); i++) {
                                song = (JSONObject) songs.get(i);
                                if (song.get("name").toString().equalsIgnoreCase(newTitle)) {
                                    return song.getString("id");
                                }
                            }
                        }

                        song = (JSONObject) songs.get(0);
                        id = song.getString("id");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

    public void updateLyricFromNetease(Context context, String songTitle, String singer,
                                       boolean showLyric) {
        deleteLyricFile(songTitle, singer);
        getWebLyricFromNetease(context, songTitle, singer, showLyric, false, true);
    }

    public void updateLyricFromKugou(Context context, String songTitle, String singer,
                                     boolean showLyric) {
        deleteLyricFile(songTitle, singer);
        getWebLyricFromKugou(context, songTitle, singer, showLyric, false, true);
    }

    private void deleteLyricFile(String songTitle, String singer) {
        File dir = new File(localPath, lyricFolder);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String newSongTitle, newSinger;

        newSongTitle = songTitle;

        if (songTitle.contains("/")) {
            newSongTitle = songTitle.replace("/", "_");
        }
        newSinger = singer;

        if (singer.contains("/")) {
            newSinger = singer.replace("/", "_");
        }
        File file = new File(dir, newSongTitle + "_" + newSinger + ".lrc");

        if (file.exists()) {
            file.delete();
        }
        File tFile = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/MusicPlayer/lyric",
                newSongTitle + "_" + newSinger + "_translate" + ".lrc");
        if (tFile.exists()) {
            tFile.delete();
        }
    }

    public void cancelNetCall() {
        if (call != null && call.isExecuted())
            call.cancel();
        if (call2 != null && call2.isExecuted())
            call2.cancel();
    }

    public void getAlbumCover(Context context, String singer, String albumTitle, int from) {

        OkHttpClient client = new OkHttpClient();
        try {

            String Path = "http://music.163.com/api/search/pc?s=";
            URL u = new URL(Path + albumTitle + " " + singer
                    + "&type=10");
            FormBody body = new FormBody.Builder()
                    .add("Host", "music.163.com")
                    .add("Connection", "keep-alive")
                    .add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .add("Referer", "http://music.163.com/")
                    .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36")
                    .add("Connection", "keep-alive")
                    .build();
            builder = new Request.Builder()
                    .post(body).url(u);

            cancelNetCall();

            call = client.newCall(builder.build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    sendLoadCoverFailedBroadcast(context, from);
                }

                @Override
                public void onResponse(@NonNull Call call,
                                       @NonNull Response response) throws IOException {
                    String json = response.body().string();
                    //Log.d("Result", "onResponse: " + json);

                    try {
                        JSONObject main = new JSONObject(json);
                        if (main.has("result")) {
                            JSONObject result = main.getJSONObject("result");
                            if (result.has("albums")) {
                                parseAlbumJson(context, result, singer, albumTitle, from);
                            } else if (result.has("albumCount")
                                    && result.getString("albumCount").equalsIgnoreCase("0")) {
                                if (singer.contains("-") || singer.contains("/"))
                                    getAlbumCoverWithSingleSinger(context, singer, albumTitle, from);
                                else
                                    getAlbumCoverWithoutSinger(context, singer, albumTitle, from);
                            } else {
                                sendLoadCoverFailedBroadcast(context, from);
                            }
                        } else {
                            sendLoadCoverFailedBroadcast(context, from);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    private void getAlbumCoverWithSingleSinger(Context context, String singer, String albumTitle, int from) {

        OkHttpClient client = new OkHttpClient();
        try {

            String newSinger;
            if (singer.contains("-")) {
                newSinger = singer.substring(0, singer.indexOf("-"));
            } else if (singer.contains("/")) {
                newSinger = singer.substring(0, singer.indexOf("/"));
            } else
                newSinger = singer;

            String Path = "http://music.163.com/api/search/pc?s=";
            URL u = new URL(Path + albumTitle + " " + newSinger + "&type=10");
            FormBody body = new FormBody.Builder()
                    .add("Host", "music.163.com")
                    .add("Connection", "keep-alive")
                    .add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .add("Referer", "http://music.163.com/")
                    .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36")
                    .add("Connection", "keep-alive")
                    .build();
            builder = new Request.Builder()
                    .post(body).url(u);

            cancelNetCall();
            call = client.newCall(builder.build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    sendLoadCoverFailedBroadcast(context, from);
                }

                @Override
                public void onResponse(@NonNull Call call,
                                       @NonNull Response response) throws IOException {

                    String json = response.body().string();
                    //Log.d("Result", "onResponse: " + json);

                    try {

                        JSONObject main = new JSONObject(json);
                        if (main.has("result")) {
                            JSONObject result = main.getJSONObject("result");
                            if (result.has("albums")) {
                                parseAlbumJson(context, result, singer, albumTitle, from);
                            } else if (result.has("albumCount")
                                    && result.getString("albumCount").equalsIgnoreCase("0")) {
                                getAlbumCoverWithoutSinger(context, singer, albumTitle, from);
                            } else {
                                sendLoadCoverFailedBroadcast(context, from);
                            }
                        } else {
                            sendLoadCoverFailedBroadcast(context, from);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    private void getAlbumCoverWithoutSinger(Context context, String singer, String albumTitle, int from) {

        OkHttpClient client = new OkHttpClient();
        try {

            String Path = "http://music.163.com/api/search/pc?s=";
            URL u = new URL(Path + albumTitle + "&type=10");
            FormBody body = new FormBody.Builder()
                    .add("Host", "music.163.com")
                    .add("Connection", "keep-alive")
                    .add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .add("Referer", "http://music.163.com/")
                    .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36")
                    .add("Connection", "keep-alive")
                    .build();
            builder = new Request.Builder()
                    .post(body).url(u);

            cancelNetCall();
            call = client.newCall(builder.build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    sendLoadCoverFailedBroadcast(context, from);
                }

                @Override
                public void onResponse(@NonNull Call call,
                                       @NonNull Response response) throws IOException {
                    String json = response.body().string();
                    //Log.d("Result", "onResponse: " + json);

                    try {
                        JSONObject main = new JSONObject(json);
                        if (main.has("result")) {
                            JSONObject result = main.getJSONObject("result");
                            if (result.has("albums")) {
                                parseAlbumJson(context, result, singer, albumTitle, from);
                            } else {
                                sendLoadCoverFailedBroadcast(context, from);
                            }
                        } else {
                            sendLoadCoverFailedBroadcast(context, from);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    private void sendLoadCoverFailedBroadcast(Context context, int from) {
        if (from == FROM_MAINIMAGE || from == FROM_SERVICE) {
            Intent intent = new Intent("load album cover failed");
            context.sendBroadcast(intent);

            if (MusicService.musicService != null) {
                Intent intent2 = new Intent("refresh notification");
                intent2.putExtra("file", "");
                context.sendBroadcast(intent2);
            }
        }
        if (from == FROM_FOOTBAR) {
            Intent intent = new Intent("load footbar cover failed");
            context.sendBroadcast(intent);
        }
    }

    private void parseAlbumJson(Context context, JSONObject result, String singer, String albumTitle, int from) {
        String picUrl;
        try {
            JSONArray albums = result.getJSONArray("albums");
            for (int i = 0; i < albums.length(); i++) {
                JSONObject album = albums.getJSONObject(i);
                if (album.has("name")) {
                    if (album.getString("name").equalsIgnoreCase(albumTitle)) {
                        if (album.has("picUrl")) {

                            picUrl = downloadAlbumCover(album.getString("picUrl"), albumTitle,
                                    singer);

                            if (from == FROM_FOOTBAR) {
                                Intent intent = new Intent("load footbar cover");
                                intent.putExtra("picUrl", picUrl);
                                context.sendBroadcast(intent);

                                if (MusicService.musicService != null) {
                                    Intent intent2 = new Intent("refresh notification");
                                    intent2.putExtra("file", picUrl);
                                    context.sendBroadcast(intent2);
                                }
                            }

                            if (from == FROM_MAINIMAGE || from == FROM_SERVICE) {
                                Intent intent = new Intent("load album cover");
                                intent.putExtra("picUrl", picUrl);
                                context.sendBroadcast(intent);

                                Intent intent1 = new Intent("get blurBG visibility");
                                intent1.putExtra("file", picUrl);
                                context.sendBroadcast(intent1);

                                if (MusicService.musicService != null) {
                                    Intent intent2 = new Intent("refresh notification");
                                    intent2.putExtra("file", picUrl);
                                    context.sendBroadcast(intent2);
                                }
                            }

                            return;
                        }
                    }
                }
            }
            JSONObject album = albums.getJSONObject(0);
            if (album.has("name")) {
                if (album.has("picUrl")) {
                    picUrl = downloadAlbumCover(album.getString("picUrl"), albumTitle,
                            singer);
                    Intent intent = new Intent("load album cover");
                    intent.putExtra("picUrl", picUrl);
                    context.sendBroadcast(intent);


                    Intent intent1 = new Intent("get blurBG visibility");
                    intent1.putExtra("file", picUrl);
                    context.sendBroadcast(intent1);

                    if (MusicService.musicService != null) {
                        Intent intent2 = new Intent("refresh notification");
                        intent2.putExtra("file", picUrl);
                        context.sendBroadcast(intent2);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String downloadAlbumCover(String uri, String album, String singer) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                "MusicPlayer/album");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String newAlbumTitle, newSinger;

        newAlbumTitle = album;

        if (album != null && album.contains("/")) {
            newAlbumTitle = album.replace("/", "_");
        }

        newSinger = singer;

        if (singer.contains("/")) {
            newSinger = singer.replace("/", "_");
        }

        File albumFile = new File(dir, newAlbumTitle + "_" + newSinger);

        InputStream is;

        try {
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(connection.getInputStream());

            BitmapFactory.Options opt = new BitmapFactory.Options();

            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, opt);


            if (bitmap != null) {

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(albumFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return albumFile.getAbsolutePath();
    }

    public void setAlbumCoverToService(Context context, Music music, int from) {
        if (music != null)
            if (downloadAlbum == 2 && haveWIFI(context)
                    || downloadAlbum == 1) {
                Bitmap bitmap = GetAlbumArt.getAlbumArtBitmap(context, music.getAlbumArtUri(), 1);

                if (bitmap != null) {

                    Intent intent = new Intent("load image with uri");
                    intent.putExtra("uri", music.getAlbumArtUri());
                    context.sendBroadcast(intent);
                } else {
                    File file = getAlbumCoverFile(music.getArtist(), music.getAlbum());

                    if (file.exists()) {

                        Intent intent1 = new Intent("load image with uri");
                        intent1.putExtra("uri", file.getAbsolutePath());
                        context.sendBroadcast(intent1);

                        Intent intent = new Intent("refresh notification");
                        intent.putExtra("file", file.getAbsolutePath());
                        context.sendBroadcast(intent);
                    } else {
                        getAlbumCover(context, music.getArtist(),
                                music.getAlbum(), from);
                    }
                }
            } else {

                Intent intent = new Intent("load image with uri");
                File file = getAlbumCoverFile(music.getArtist(), music.getAlbum());
                if (file.exists())
                    intent.putExtra("uri", file.getAbsolutePath());
                else
                    intent.putExtra("uri", music.getAlbumArtUri());
                context.sendBroadcast(intent);

            }

    }

    public void setAlbumCoverToAdapter(Context context, Music music, ImageView imageView, int from) {
        if (music != null)
            if (downloadAlbum == 2 && haveWIFI(context)
                    || downloadAlbum == 1) {

                Glide.with(context)
                        .load(music.getAlbumArtUri())
                        .apply(new RequestOptions()
                                .centerCrop()
                                .placeholder(R.drawable.default_album_art))
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onLoadStarted(@Nullable Drawable placeholder) {
                                imageView.setImageResource(R.drawable.default_album_art);
                                super.onLoadStarted(placeholder);
                            }

                            @Override
                            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                if ((music.getTitle() + music.getArtist())
                                        .equals(imageView.getTag(R.id.item_url)))
                                    imageView.setImageDrawable(resource);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                File file = getAlbumCoverFile(music.getArtist(), music.getAlbum());

                                if (file.exists() && (music.getTitle() + music.getArtist())
                                        .equals(imageView.getTag(R.id.item_url))) {
                                    Glide.with(context)
                                            .load(file)
                                            .apply(new RequestOptions()
                                                    .centerCrop()
                                                    .placeholder(R.drawable.default_album_art))
                                            .into(imageView);
                                } else {
                                    getAlbumCover(context, music.getArtist(),
                                            music.getAlbum(), from);
                                }
                            }
                        });
            } else {
                File file = getAlbumCoverFile(music.getArtist(), music.getAlbum());
                if (file.exists())
                    loadImageUseGlide(context, imageView, file);
                else
                    loadImageUseGlide(context, imageView, music.getAlbumArtUri());
            }
    }

    public void setAlbumCoverToFootAndHeader(Context context, Music music, int from) {
        if (music != null)
            if (downloadAlbum == 2 && haveWIFI(context)
                    || downloadAlbum == 1) {

                Bitmap bitmap = GetAlbumArt.getAlbumArtBitmap(context, music.getAlbumArtUri(), 1);

                if (bitmap != null) {

                    Intent intent = new Intent("load image with uri");
                    intent.putExtra("uri", music.getAlbumArtUri());
                    context.sendBroadcast(intent);

                } else {
                    File file = getAlbumCoverFile(music.getArtist(), music.getAlbum());

                    if (file.exists()) {
                        Intent intent = new Intent("load image with uri");
                        intent.putExtra("uri", file.getAbsolutePath());
                        context.sendBroadcast(intent);

                    } else {
                        getAlbumCover(context, music.getArtist(),
                                music.getAlbum(), from);
                    }
                }
            } else {
                Intent intent = new Intent("load image with uri");
                File file = getAlbumCoverFile(music.getArtist(), music.getAlbum());
                if (file.exists())
                    intent.putExtra("uri", file.getAbsolutePath());
                else
                    intent.putExtra("uri", music.getAlbumArtUri());
                context.sendBroadcast(intent);

            }

    }

    public void loadImageUseGlide(Context context, ImageView imageView,
                                  String uri) {
        Glide.with(context)
                .load(uri)
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.default_album_art)
                        .error(R.drawable.default_album_art)
                        .signature(new ObjectKey(String.valueOf(System.currentTimeMillis()))))
                .transition(new DrawableTransitionOptions().crossFade(500))
                .into(imageView);
    }

    public void loadImageUseGlide(Context context, ImageView imageView, File file) {
        Glide.with(context)
                .load(file)
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.default_album_art)
                        .error(R.drawable.default_album_art)
                        .signature(new ObjectKey(String.valueOf(System.currentTimeMillis()))))
                .transition(new DrawableTransitionOptions().crossFade(500))
                .into(imageView);
    }

    public void loadImageUseGlide(Context context, ImageView imageView, Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        Glide.with(context)
                .load(stream.toByteArray())
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.default_album_art)
                        .error(R.drawable.default_album_art)
                        .signature(new ObjectKey(String.valueOf(System.currentTimeMillis()))))
                .transition(new DrawableTransitionOptions().crossFade(500))
                .into(imageView);
    }

    public File getAlbumCoverFile(String singer, String album) {
        String newAlbumTitle, newSinger;

        newAlbumTitle = album;

        if (album != null && album.contains("/")) {
            newAlbumTitle = album.replace("/", "_");
        }

        newSinger = singer;

        if (singer != null && singer.contains("/")) {
            newSinger = singer.replace("/", "_");
        }
        return new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/MusicPlayer/album",
                newAlbumTitle + "_" + newSinger);
    }

    private void clearImageDiskCache(Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(() -> Glide.get(context).clearDiskCache()).start();
            } else {
                Glide.get(context).clearDiskCache();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearImageMemoryCache(Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
                Glide.get(context).clearMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearImageAllCache(Context context) {
        clearImageDiskCache(context);
        clearImageMemoryCache(context);
        String ImageExternalCatchDir = context.getExternalCacheDir() + ExternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR;
        deleteFolderFile(ImageExternalCatchDir);
    }

    private void deleteFolderFile(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {
                    File files[] = file.listFiles();
                    for (File file1 : files) {
                        deleteFolderFile(file1.getAbsolutePath());
                    }
                }
                if (!file.isDirectory()) {
                    file.delete();
                } else {
                    if (file.listFiles().length == 0) {
                        file.delete();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getCacheSize(Context context) {
        try {
            return getFormatSize(getFolderSize(new File(context.getCacheDir() + "/" + InternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getFormatSize(double size) {

        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);

        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }

    private long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    size = size + getFolderSize(aFileList);
                } else {
                    size = size + aFileList.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public boolean isFlyme(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);

        String name;
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo pak = packageInfoList.get(i);

            if ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                name = pak.packageName;
                if (name.contains("meizu")) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setLaunchPage(Context context, int from) {
        if (MusicList.flag == 1 && MusicService.mediaPlayer.isPlaying()) {
            setAlbumCoverToFootAndHeader(context, MusicService.music, from);

            Intent intent = new Intent("set footBar");
            Intent intent1 = new Intent("set PlayOrPause");
            intent.putExtra("footTitle", MusicService.music.getTitle());
            intent.putExtra("footArtist", MusicService.music.getArtist());
            intent1.putExtra("PlayOrPause", R.drawable.pause_to_play_white_anim);
            context.sendBroadcast(intent);
            context.sendBroadcast(intent1);

        } else {

            if (MusicService.isRandom) {
                if (MusicList.shufflelist != null
                        && MusicList.shufflelist.size() > 0) {

                    if (pos > MusicList.shufflelist.size())
                        pos = 0;

                    Intent intent = new Intent("set footBar");
                    Intent intent1 = new Intent("set PlayOrPause");
                    intent.putExtra("footTitle", MusicList.shufflelist.get(pos).getTitle());
                    intent.putExtra("footArtist", MusicList.shufflelist.get(pos).getArtist());
                    intent1.putExtra("PlayOrPause", R.drawable.play_to_pause_white_anim);
                    context.sendBroadcast(intent);
                    context.sendBroadcast(intent1);

                    getFootAlbumArt(context, pos, MusicList.shufflelist, from);
                }
            } else {
                if (MusicList.list != null
                        && MusicList.list.size() > 0) {

                    if (pos > MusicList.list.size())
                        pos = 0;

                    Intent intent = new Intent("set footBar");
                    Intent intent1 = new Intent("set PlayOrPause");
                    intent.putExtra("footTitle", MusicList.list.get(pos).getTitle());
                    intent.putExtra("footArtist", MusicList.list.get(pos).getArtist());
                    intent1.putExtra("PlayOrPause", R.drawable.play_to_pause_white_anim);
                    context.sendBroadcast(intent);
                    context.sendBroadcast(intent1);

                    getFootAlbumArt(context, pos, MusicList.list, from);
                }
            }
        }
    }

    //保存本次应用播放的的播放列表
    public void saveArray(Context context, ArrayList<Music> list) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(list);
        SharedPreferences.Editor editor = context.getSharedPreferences("data", MODE_PRIVATE).edit();

        editor.putString("Musiclist", jsonString);
        editor.apply();
        editor.commit();
    }


    //保存本次播放的随机播放列表
    public void saveShuffleArray(Context context, ArrayList<Music> list) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(list); //使用GSON序列化列表，转化为SharedPreferences可以保存的字符串类型
        //使用SharedPreferences可以将数据保存在应用本地，使用键值对进行存储和获取
        SharedPreferences.Editor editor = context.getSharedPreferences("data", MODE_PRIVATE).edit();

        editor.putString("Shufflelist", jsonString);
        editor.apply();
        editor.commit();
    }

    //获取上次应用保存的播放列表
    public ArrayList<Music> getArray(Context context) {
        SharedPreferences sp = context.getSharedPreferences("data", MODE_PRIVATE);

        Gson gson = new Gson();
        //默认值
        String jsonString = gson.toJson(MusicList.list);

        //通过GSON将存储的字符串转换为列表项
        return gson.fromJson(sp.getString("Musiclist", jsonString),
                new TypeToken<ArrayList<Music>>() {
                }.getType());
    }

    //获取上次应用保存的随机播放列表
    public ArrayList<Music> getShuffleArray(Context context) {
        SharedPreferences sp = context.getSharedPreferences("data", MODE_PRIVATE);

        Gson gson = new Gson();
        //默认值
        String jsonString = gson.toJson(MusicList.list);

        return gson.fromJson(sp.getString("Shufflelist", jsonString),
                new TypeToken<ArrayList<Music>>() {
                }.getType());
    }

    public void checkUpdate(Context context) {
        PackageManager packageManager = context.getPackageManager();
        int versionCode;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;

            String UPDATE_URL = "http://www.wanandroid.com/tools/mockapi/5636/iornbear775_musicplayer_check_update";
            requestBuilder = new Request.Builder()
                    .url(new URL(UPDATE_URL));

            Call call;
            if (requestBuilder != null) {

                call = client.newCall(requestBuilder.build());
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Toast.makeText(context, "检查更新失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String result = response.body().string();
                        try {
                            JSONObject object = new JSONObject(result);
                            JSONObject data = object.getJSONObject("data");
                            int newVersionCode = data.getInt("versionCode");

                            if (newVersionCode > versionCode) {
                                String versionName = data.getString("versionName");
                                String downloadUrl = data.getString("downloadUrl");
                                String intro = data.getString("intro");

                                Intent intent = new Intent("new version");
                                intent.putExtra("versionName", versionName);
                                intent.putExtra("downloadUrl", downloadUrl);
                                intent.putExtra("intro", intro);
                                context.sendBroadcast(intent);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        } catch (PackageManager.NameNotFoundException | MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public void downloadApk(String url, String filename, OnDownloadListener listener) {
        requestBuilder = new Request.Builder()
                .url(url);

        Call call;
        if (requestBuilder != null) {

            call = client.newCall(requestBuilder.build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    listener.onDownloadFailed();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    InputStream is = null;
                    byte[] buf = new byte[2048];
                    int len;
                    FileOutputStream fos = null;

                    File dir = new File(Environment.getExternalStorageDirectory(), apkFolder);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    try {
                        is = response.body() != null ? response.body().byteStream() : null;
                        long total = response.body() != null ? response.body().contentLength() : 0;
                        File downloadFile = new File(dir, filename + ".apk");
                        fos = new FileOutputStream(downloadFile);
                        long sum = 0;
                        if (is != null) {
                            while ((len = is.read(buf)) != -1) {
                                fos.write(buf, 0, len);
                                sum += len;
                                int progress = (int) (sum * 1.0f / total * 100);
                                listener.onDownloading(progress);
                            }
                            fos.flush();
                            listener.onDownloadSuccess();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onDownloadFailed();
                    } finally {
                        try {
                            if (is != null)
                                is.close();
                        } catch (IOException ignored) {
                        }
                        try {
                            if (fos != null)
                                fos.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            });
        }
    }

    public interface OnDownloadListener {
        /**
         * 下载成功
         */
        void onDownloadSuccess();

        /**
         * @param progress 下载进度
         */
        void onDownloading(int progress);

        /**
         * 下载失败
         */
        void onDownloadFailed();
    }

}
package ironbear775.com.musicplayer.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.SquareImageView;

/**
 * Created by ironbear on 2017/4/29.
 */

public class TagEditActivty extends BaseActivity{
    private Toolbar toolbar;
    private EditText songTitle;
    private EditText albumTitle;
    private EditText artistTitle;
    private EditText trackTitle;
    private EditText yearTitle;
    private EditText lyricTitle;
    private SquareImageView albumArt;
    private Music music;
    private Mp3File file;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Uri imageUri;
    private FloatingActionButton save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        music = getIntent().getParcelableExtra("music");
        String path = music.getUri();
        try {
            file = new Mp3File(music.getUri());
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_editor_layout);

        findView();

        //collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        //collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);

        toolbar.setTitle(getResources().getString(R.string.tag_editor));
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.listView_bg_color));

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void findView() {
        //collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        //albumArt = (SquareImageView) findViewById(R.id.tag_album_art);
        save = (FloatingActionButton) findViewById(R.id.save_change);
        toolbar = (Toolbar) findViewById(R.id.tag_edit_toolbar);
        songTitle = (EditText) findViewById(R.id.song_edit_text);
        albumTitle = (EditText) findViewById(R.id.album_edit_text);
        artistTitle = (EditText) findViewById(R.id.artist_edit_text);
        trackTitle = (EditText) findViewById(R.id.genre_edit_text);
        yearTitle = (EditText) findViewById(R.id.year_edit_text);

        //lyricTitle = (EditText) findViewById(R.id.lyric_edit_text);

        setTextFields(file.getId3v2Tag());


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveExifData();

                Snackbar.make(save, R.string.set_success, Snackbar.LENGTH_LONG).show();
            }
        });

//        albumArt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                File outputImage = new File(Environment.getExternalStorageDirectory(), "change_image");
//                try {
//                    if (outputImage.exists()) {
//                        outputImage.delete();
//                    }
//                    outputImage.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                if (Build.VERSION.SDK_INT < 24) {
//                    imageUri = Uri.fromFile(outputImage);
//                    Intent intent = new Intent("android.intent.action.PICK");
//                    intent.setType("image/*");
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                    startActivityForResult(intent, 775);
//                } else {
//                    String authorities = "ironbear775.com.musicplayer";
//                    Uri imageUri = FileProvider.getUriForFile(getApplicationContext(), authorities, outputImage);
//
//                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                    intent.setType("image/*");
//                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//将拍取的照片保存到指定URI
//                    startActivityForResult(intent, 775);
//
//                }
//
//            }
//        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case 775:
                if (resultCode == RESULT_OK) {

                    Uri uri = data.getData();

                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream
                                (getContentResolver().openInputStream(uri));

                        albumArt.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    /*Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
                    cursor.moveToFirst();
                    int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    String fileSrc = cursor.getString(idx);
                    Bitmap albumBitmap = BitmapFactory.decodeFile(fileSrc);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    albumBitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
                    byte[] b = baos.toByteArray();

                    file.getId3v2Tag().setAlbumImage(b, "image/png");*/

                }
                break;
            default:
                break;
        }
    }


    private void setTextFields(ID3v2 tag) {
        songTitle.setText(tag.getTitle());
        artistTitle.setText(tag.getArtist());
        albumTitle.setText(tag.getAlbum());
        yearTitle.setText(tag.getYear());
        trackTitle.setText(tag.getTrack());

        /*if (tag.getAlbumImage() != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(tag.getAlbumImage(), 0, tag.getAlbumImage().length);
            albumArt.setImageBitmap(bmp);
        }*/

        /*Glide.with(this)
                .load(music.getAlbumArtUri())
                .asBitmap()
                .into(albumArt);*/
    }

    private void saveExifData() {
        ID3v2 tag;
        if (file.hasId3v2Tag()) {
            tag = file.getId3v2Tag();
        } else {
            tag = new ID3v24Tag();
        }

        tag.setTitle(songTitle.getText().toString());
        tag.setArtist(artistTitle.getText().toString());
        tag.setAlbum(albumTitle.getText().toString());
        tag.setYear(yearTitle.getText().toString());
        tag.setTrack(trackTitle.getText().toString());

        Bitmap bitmap = ((BitmapDrawable) albumArt.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imageInByte = stream.toByteArray();

        tag.setAlbumImage(imageInByte,"image/jpg");

        file.setId3v2Tag(tag);
        try {
            file.save(file.getFilename() + "_1");
            new File(file.getFilename()).delete();
            new File(file.getFilename() + "_1").renameTo(new File(file.getFilename()));

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(file.getFilename()))));

        } catch (IOException | NotSupportedException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        Intent in = new Intent("notifyDataSetChanged");
        sendBroadcast(in);
    }

}

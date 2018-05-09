package ironbear775.com.musicplayer.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;

import com.beaglebuddy.mp3.MP3;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;

import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;

/**
 * Created by ironbear on 2017/4/29.
 */

public class TagEditActivity extends BaseActivity{
    private Toolbar toolbar;
    private EditText songTitle;
    private EditText albumTitle;
    private EditText artistTitle;
    private EditText trackTitle;
    private EditText yearTitle;
    private EditText lyricTitle;
    private Music music;
    private Mp3File file;
    private FloatingActionButton save;
    private boolean isSongTitleChange = false;
    private boolean isAlbumTitleChange = false;
    private boolean isArtistTitleChange = false;
    private boolean isTrackTitleChange = false;
    private boolean isYearTitleChange = false;
    private boolean isLyricTitleChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        music = getIntent().getParcelableExtra("music");


        try {
            file = new Mp3File(music.getUri());
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_editor_layout);

        findView();

        toolbar.setTitle(getResources().getString(R.string.tag_editor));
        toolbar.setTitleTextColor(Color.WHITE);

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
        save = findViewById(R.id.save_change);
        toolbar = findViewById(R.id.tag_edit_toolbar);
        songTitle = findViewById(R.id.song_edit_text);
        albumTitle = findViewById(R.id.album_edit_text);
        artistTitle = findViewById(R.id.artist_edit_text);
        trackTitle = findViewById(R.id.genre_edit_text);
        yearTitle = findViewById(R.id.year_edit_text);
        lyricTitle = findViewById(R.id.lyric_edit_text);

        save.hide();

        setTextFields(file.getId3v2Tag());

        songTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isSongTitleChange = true;
                if (!save.isShown())
                    save.show();
            }
        });

        albumTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isAlbumTitleChange = true;
                if (!save.isShown())
                    save.show();
            }
        });
        artistTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                isArtistTitleChange = true;
                if (!save.isShown())
                    save.show();
            }
        });
        lyricTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                isLyricTitleChange = true;
                if (!save.isShown())
                    save.show();
            }
        });
        trackTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                isTrackTitleChange = true;
                if (!save.isShown())
                    save.show();
            }
        });
        yearTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                isYearTitleChange = true;
                if (!save.isShown())
                    save.show();
            }
        });

        save.setOnClickListener(v -> {

            saveExifData();

            Snackbar.make(save, R.string.set_success, Snackbar.LENGTH_LONG).show();
        });

    }


    private void setTextFields(ID3v2 tag) {
        if (tag!=null) {
            if (tag.getTitle() != null)
                songTitle.setText(tag.getTitle());
            if (tag.getArtist() != null)
                artistTitle.setText(tag.getArtist());
            if (tag.getAlbum() != null)
                albumTitle.setText(tag.getAlbum());
            if (tag.getYear() != null)
                yearTitle.setText(tag.getYear());
            if (tag.getTrack() != null)
                trackTitle.setText(tag.getTrack());
            if (tag.getLyrics() != null)
                lyricTitle.setText(tag.getLyrics());

        }
    }

    private void saveExifData() {
        ID3v2 tag;
        if (file.hasId3v2Tag()) {
            tag = file.getId3v2Tag();
        } else {
            tag = new ID3v24Tag();
        }

        if (isSongTitleChange)
            tag.setTitle(songTitle.getText().toString());
        if (isArtistTitleChange)
            tag.setArtist(artistTitle.getText().toString());
        if (isAlbumTitleChange)
            tag.setAlbum(albumTitle.getText().toString());

        file.setId3v2Tag(tag);

        try {
            file.save(file.getFilename() + "_1");
            new File(file.getFilename()).delete();
            new File(file.getFilename() + "_1").renameTo(new File(file.getFilename()));

            MP3 mp3 = new MP3(music.getUri());

            if (isTrackTitleChange)
                if (trackTitle.getText().toString().equals(""))
                    mp3.removeTrack();
                else
                    mp3.setTrack(Integer.parseInt(trackTitle.getText().toString()));

            if (isLyricTitleChange)
                if (lyricTitle.getText().toString().equals(""))
                    mp3.removeLyrics();
                else
                    mp3.setLyrics(lyricTitle.getText().toString());

            if (isYearTitleChange)
                if (yearTitle.getText().toString().equals(""))
                    mp3.removeYear();
                else
                    mp3.setYear(Integer.parseInt(yearTitle.getText().toString()));

            mp3.save();

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

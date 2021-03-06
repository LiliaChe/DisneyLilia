package com.example.disney_time02;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Vector;

public class Movie_Page extends AppCompatActivity {
    RecyclerView recyclerView;
    Vector<YouTubeVideos> youtubeVideos = new Vector<>();
    private String movieId;
    private String movieName;
    boolean switchChecked;
    SwitchCompat switchCompat;
    AlertDialog dialog;
    Runnable runnableSave = () -> {
        Looper.prepare();
        saveMovie();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_page);
        setActionBar("");
        //Set text box Genre
        String genre = getIntent().getExtras().getString("genre", "");
        TextView edGenre = findViewById(R.id.genres);
        edGenre.setText(genre);

        //Set text box Movie name
        movieName = getIntent().getExtras().getString("movieName", "");
        TextView edMovieName = findViewById(R.id.TextId);
        edMovieName.setText(movieName);

        //Save movie ID
        movieId = getIntent().getExtras().getString("movieId", "");
        switchCompat = findViewById(R.id.save);

        //Get movie saved in database
        String savedMovie = getIntent().getExtras().getString("savedMovie");
        switchCompat.setChecked(savedMovie != null);
        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> save(isChecked));

        //Create and show spinner dialog
        dialog = new AlertDialog.Builder(this)
                .setView(R.layout.layout_loading_dialog)
                .create();
        dialog.show();
        //Insert move to movie box with spinner
        recyclerView = findViewById(R.id.trailerView);
        recyclerView.requestFocus();
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        new Thread(this::prepareMovieView).start();
    }
    public void setActionBar(String heading) {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(androidx.cardview.R.color.cardview_dark_background, null)));
        actionBar.setTitle(heading);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(final MenuItem item) {
        MenuClass menuClass = new MenuClass(this);
        return menuClass.getItemSelected(item);
    }
    private void prepareMovieView() {
        String movieUrl = getIntent().getExtras().getString("url", "");
        String url = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/" +
                getId(movieUrl) + "\" frameborder=\"0\" allowfullscreen></iframe>";
        youtubeVideos.add(new YouTubeVideos(url));
        VideoAdapter videoAdapter = new VideoAdapter(youtubeVideos, this);
        recyclerView.setAdapter(videoAdapter);
        dialog.dismiss();
    }

    public void save(boolean isChecked) {
        this.switchChecked = isChecked;
        dialog.show();
        new Thread(runnableSave).start();
    }

    private String getId(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private void saveMovie() {
        String sql;
        if (switchChecked) {
            sql = "insert into usersmovie (name, id) values ('" + LoginActivity.userName + "', " + movieId + ")";
        } else {
            sql = "delete from usersmovie where name='" + LoginActivity.userName + "' and id=" + movieId;
        }
        MysqlConnect mysqlConnect = new MysqlConnect();
        if (mysqlConnect.executeSql(sql, this) > 0) {
            Toast.makeText(this,
                    "Movie " + movieName + (switchChecked ? " added to" : " removed from") + " your saves",
                    Toast.LENGTH_SHORT).show();
        }
        dialog.dismiss();
    }

    public void goBack(View view) {
        finish();
    }
}

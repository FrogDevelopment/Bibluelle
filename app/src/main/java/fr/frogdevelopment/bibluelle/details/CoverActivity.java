package fr.frogdevelopment.bibluelle.details;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.entities.Book;

public class CoverActivity extends AppCompatActivity {

    public static final String ARG_BOOK = "BOOK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cover);

        Book book = (Book) getIntent().getSerializableExtra(ARG_BOOK);

        if (book.dominantRgb != 0) {
            getWindow().getDecorView().setBackgroundColor(book.dominantRgb);
            getWindow().setStatusBarColor(book.dominantRgb);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (book.dominantRgb != 0) {
                actionBar.setBackgroundDrawable(new ColorDrawable(book.dominantRgb));
            }
        }

        ImageView viewById = findViewById(R.id.fullscreen_content);

        Object load;
        if (TextUtils.isEmpty(book.coverUrl)) {
            load = getFileStreamPath(book.getCoverFile());
        } else {
            load = book.coverUrl;
        }

        supportPostponeEnterTransition();

        GlideApp.with(this)
                .asDrawable()
                .load(load)
                .listener(new RequestListener<Drawable>() {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(viewById);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

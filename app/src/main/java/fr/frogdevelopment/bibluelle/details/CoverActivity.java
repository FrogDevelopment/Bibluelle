package fr.frogdevelopment.bibluelle.details;

import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.data.entities.Book;

public class CoverActivity extends AppCompatActivity {

    public static final String ARG_BOOK = "BOOK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageView imageView = new ImageView(this);
        imageView.setTransitionName("cover");

        setContentView(imageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Book book = (Book) getIntent().getSerializableExtra(ARG_BOOK);
        if (book.dominantRgb != 0) {
            getWindow().setStatusBarColor(book.dominantRgb);
        }

        supportPostponeEnterTransition();

        GlideApp.with(this)
                .asDrawable()
                .load(TextUtils.isEmpty(book.coverUrl) ? getFileStreamPath(book.getCoverFile()) : book.coverUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(new SimpleTarget<Drawable>() {

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        supportStartPostponedEnterTransition();
                    }

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
                        final float imageWidth = resource.getIntrinsicWidth();
                        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
                        final float scaleRatio = screenWidth / imageWidth;

                        final Matrix matrix = imageView.getImageMatrix();
                        matrix.postScale(scaleRatio, scaleRatio);

                        imageView.setScaleType(ImageView.ScaleType.MATRIX);
                        imageView.setImageMatrix(matrix);

                        imageView.setImageDrawable(resource);

                        supportStartPostponedEnterTransition();
                    }
                });
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

package fr.frogdevelopment.bibluelle.cover;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionInflater;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.entities.Book;

public class CoverFragment extends Fragment {

    private ImageView coverView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        coverView = new ImageView(requireContext());
        coverView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        coverView.setScaleType(ImageView.ScaleType.MATRIX);
        coverView.setBackgroundColor(Color.LTGRAY);

        android.support.transition.Transition transition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.image_shared_element_transition);
        transition.setDuration(375);

        setSharedElementEnterTransition(transition);

        return coverView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            return;
        }

        Bundle arguments = getArguments();

        if (arguments == null) {
            return;
        }


        Book book = (Book) arguments.getSerializable(CoverActivity.ARG_BOOK);

        if (book == null) {
            return;
        }

        int topBackgroundColor = arguments.getInt("topBackgroundColor");
        requireActivity().getWindow().setStatusBarColor(topBackgroundColor);

        GlideApp.with(this)
                .asDrawable()
                .load(TextUtils.isEmpty(book.coverUrl) ? requireContext().getFileStreamPath(book.getCoverFile()) : book.coverUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(new DrawableImageViewTarget(coverView) {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Drawable> transition) {
                        setBackgroundColors(resource);

                        final float imageWidth = resource.getIntrinsicWidth();
                        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
                        final float scaleRatio = screenWidth / imageWidth;

                        final Matrix matrix = coverView.getImageMatrix();
                        matrix.postScale(scaleRatio, scaleRatio);

                        coverView.setImageMatrix(matrix);

                        super.onResourceReady(resource, transition);
                    }

                });
    }

    private void setBackgroundColors(@NonNull Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        Palette.from(bitmap).generate(palette -> {

            Palette.Swatch mutedSwatch = palette.getMutedSwatch();

            if (mutedSwatch != null) {
                int bottomBackgroundColor = mutedSwatch.getRgb();

                if (bottomBackgroundColor != 0) {
                    coverView.setBackgroundColor(bottomBackgroundColor);
                }
            }
        });
    }
}

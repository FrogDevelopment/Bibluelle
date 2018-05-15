package fr.frogdevelopment.bibluelle.cover;

import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionInflater;
import android.support.v4.app.Fragment;
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

    public void prepareSharedElementTransition() {
        android.support.transition.Transition transition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.image_shared_element_transition);
        transition.setDuration(375);

        setSharedElementEnterTransition(transition);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Avoid a postponeEnterTransition on orientation change, and postpone only of first creation.
        if (savedInstanceState == null) {
//            postponeEnterTransition();
        }

        prepareSharedElementTransition();

        coverView = new ImageView(requireContext());
        coverView.setTransitionName("cover");
        coverView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        coverView.setScaleType(ImageView.ScaleType.MATRIX);

        return coverView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Book book = (Book) getArguments().getSerializable(CoverActivity.ARG_BOOK);

        GlideApp.with(this)
                .asDrawable()
                .load(TextUtils.isEmpty(book.coverUrl) ? requireContext().getFileStreamPath(book.getCoverFile()) : book.coverUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(new DrawableImageViewTarget(coverView) {

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        startPostponedEnterTransition();

                        super.onLoadFailed(errorDrawable);
                    }

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Drawable> transition) {
                        startPostponedEnterTransition();

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
}

package fr.frogdevelopment.bibluelle.details;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.transition.TransitionInflater;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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

		AppCompatActivity appCompatActivity = (AppCompatActivity) requireActivity();
		appCompatActivity.setSupportActionBar(null);

		CoordinatorLayout layout = new CoordinatorLayout(requireContext());
		layout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		layout.setFitsSystemWindows(true);

		coverView = new ImageView(requireContext());
		coverView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		coverView.setScaleType(ImageView.ScaleType.MATRIX);
		coverView.setBackgroundColor(Color.LTGRAY);
		coverView.setTransitionName("cover");

		layout.addView(coverView);

		android.support.transition.Transition transition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.image_shared_element_transition);
		transition.setDuration(375);

		setSharedElementEnterTransition(transition);

		return layout;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (savedInstanceState == null) {
			postponeEnterTransition();
		}

		DetailViewModel viewModel = ViewModelProviders.of(requireActivity()).get(DetailViewModel.class);

		viewModel.getBook().observe(this, new Observer<Book>() {
			@Override
			public void onChanged(@Nullable Book book) {
				GlideApp.with(CoverFragment.this)
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
								setBackgroundColors(resource);

								final float imageWidth = resource.getIntrinsicWidth();
								final int screenWidth = getResources().getDisplayMetrics().widthPixels;
								final float scaleRatio = screenWidth / imageWidth;

								final Matrix matrix = coverView.getImageMatrix();
								matrix.postScale(scaleRatio, scaleRatio);

								coverView.setImageMatrix(matrix);

								startPostponedEnterTransition();

								super.onResourceReady(resource, transition);
							}
						});
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

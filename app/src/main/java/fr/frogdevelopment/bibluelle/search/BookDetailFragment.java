package fr.frogdevelopment.bibluelle.search;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.Book;

public class BookDetailFragment extends Fragment {

	public static final String ARG_KEY = "book";

	private Book mBook;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBook = (Book) getArguments().getSerializable(ARG_KEY);

		CollapsingToolbarLayout collapseToolbar = getActivity().findViewById(R.id.toolbar_layout);
		collapseToolbar.setTitle(mBook.getTitle());

		ImageView toolbarCover = getActivity().findViewById(R.id.toolbar_cover);
		GlideApp.with(this)
				.asDrawable()
				.load(mBook.getImage())
				.into(new SimpleTarget<Drawable>() {

					@Override
					public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
						final float imageWidth = resource.getIntrinsicWidth();
						final int screenWidth = getResources().getDisplayMetrics().widthPixels;
						final float scaleRatio = screenWidth / imageWidth;

						final Matrix matrix = toolbarCover.getImageMatrix();
						matrix.postScale(scaleRatio, scaleRatio);
						toolbarCover.setImageMatrix(matrix);

						toolbarCover.setImageDrawable(resource);

						Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();

						Palette palette = Palette.from(bitmap).generate();

						Palette.Swatch dominantSwatch = palette.getDominantSwatch();
						if (dominantSwatch != null) {
							int dominantRgb = dominantSwatch.getRgb();
							collapseToolbar.setBackgroundColor(dominantRgb);
							collapseToolbar.setStatusBarScrimColor(dominantRgb);
							collapseToolbar.setContentScrimColor(dominantRgb);

							Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
							if (vibrantSwatch != null) {
								int vibrantRgb = vibrantSwatch.getRgb();
								if (dominantRgb != vibrantRgb) {
									collapseToolbar.setExpandedTitleColor(vibrantRgb);
									collapseToolbar.setCollapsedTitleTextColor(vibrantRgb);
								}
							}
						}

					}
				});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.book_detail, container, false);

		// todo if present, show subTitle

		ImageView background = rootView.findViewById(R.id.detail_background);
		GlideApp.with(this).load(mBook.getImage()).into(background);

		TextView author = rootView.findViewById(R.id.detail_author);
		author.setText(mBook.getAuthor());

		TextView publisher = rootView.findViewById(R.id.detail_publisher);
		publisher.setText(mBook.getPublisher());

		TextView publishedDate = rootView.findViewById(R.id.detail_published_date);
		publishedDate.setText(mBook.getPublishedDate());

		TextView description = rootView.findViewById(R.id.book_description);
		description.setText(mBook.getDescription());

		// todo show pageCount
		// todo show categories


		return rootView;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				getActivity().supportFinishAfterTransition();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

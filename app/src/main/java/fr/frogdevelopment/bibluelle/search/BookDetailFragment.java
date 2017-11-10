package fr.frogdevelopment.bibluelle.search;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import at.blogc.android.views.ExpandableTextView;
import fr.frogdevelopment.bibluelle.AppBarStateChangeListener;
import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.Book;

public class BookDetailFragment extends Fragment {

	public static final String ARG_KEY = "book";
	private static final DateTimeFormatter LONG_DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);

	private Book mBook;
	private int dominantRgb;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBook = (Book) getArguments().getSerializable(ARG_KEY);
		CollapsingToolbarLayout collapseToolbar = getActivity().findViewById(R.id.toolbar_layout);
		if (mBook != null && collapseToolbar != null) {
			collapseToolbar.setTitle(mBook.title);
			AppBarLayout appBarLayout = getActivity().findViewById(R.id.app_bar);
			appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
				@Override
				public void onStateChanged(AppBarLayout appBarLayout, @AppBarStateChangeListener.State int state) {
					collapseToolbar.setTitleEnabled(state == AppBarStateChangeListener.COLLAPSED);
				}
			});

			dominantRgb = getArguments().getInt("dominantRgb", 0);
			if (dominantRgb != 0) {
				collapseToolbar.setBackgroundColor(dominantRgb);
				collapseToolbar.setStatusBarScrimColor(dominantRgb);
				collapseToolbar.setContentScrimColor(dominantRgb);
			}

			int collapsedTitleColor = getArguments().getInt("collapsedTitleColor", 0);
			if (collapsedTitleColor != 0) {
				collapseToolbar.setCollapsedTitleTextColor(collapsedTitleColor);
			}

			ImageView toolbarCover = getActivity().findViewById(R.id.toolbar_cover);
			GlideApp.with(this)
					.asDrawable()
					.load(mBook.image)
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
						}
					});
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.book_detail, container, false);

		ImageView background = rootView.findViewById(R.id.detail_cover);
		GlideApp.with(this).load(mBook.image).into(background);
		background.setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), CoverActivity.class);
			intent.putExtra("url", mBook.image);
			intent.putExtra("dominantRgb", dominantRgb);
			// cf https://guides.codepath.com/android/Shared-Element-Activity-Transition#3-start-activity
			ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), background, "cover");

			startActivity(intent, options.toBundle());
		});

		TextView title = rootView.findViewById(R.id.detail_title);
		title.setText(mBook.title);

		TextView subTitle = rootView.findViewById(R.id.detail_sub_title);
		if (TextUtils.isEmpty(mBook.subTitle)) {
			subTitle.setVisibility(View.GONE);
		} else {
			subTitle.setVisibility(View.VISIBLE);
			subTitle.setText(mBook.subTitle);
		}

		TextView author = rootView.findViewById(R.id.detail_author);
		author.setText(mBook.author);

		TextView publisher = rootView.findViewById(R.id.detail_publisher);
		publisher.setText(mBook.publisher);

		TextView publishedDate = rootView.findViewById(R.id.detail_publication_date);
		LocalDate localDate = LocalDate.parse(mBook.publishedDate, DateTimeFormatter.ISO_DATE);
		publishedDate.setText("Publié le " + localDate.format(LONG_DATE_FORMATTER));

		TextView pageCount = rootView.findViewById(R.id.detail_nb_pages);
		pageCount.setText(mBook.pageCount + " pages");

		ExpandableTextView description = rootView.findViewById(R.id.detail_description);
		description.setText(mBook.description);
		// set interpolators for both expanding and collapsing animations
		description.setInterpolator(new OvershootInterpolator());

		View showMore = rootView.findViewById(R.id.detail_show_more);
		showMore.setOnClickListener(v -> {
			showMore.setVisibility(View.GONE);
			description.expand();
		});

		TextView isbn = rootView.findViewById(R.id.detail_isbn);
		isbn.setText(mBook.isbn);

		TextView categories = rootView.findViewById(R.id.detail_categories);
		categories.setText(mBook.categories);

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

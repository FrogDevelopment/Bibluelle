package fr.frogdevelopment.bibluelle.search;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.Swatch;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import at.blogc.android.views.ExpandableTextView;
import fr.frogdevelopment.bibluelle.AppBarStateChangeListener;
import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.Book;

public class BookDetailFragment extends Fragment {

	public static final String ARG_KEY = "book";
	private static final DateTimeFormatter LONG_DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);

	private Book mBook;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBook = (Book) getArguments().getSerializable(ARG_KEY);
		CollapsingToolbarLayout collapseToolbar = getActivity().findViewById(R.id.toolbar_layout);
		if (collapseToolbar != null) {
			collapseToolbar.setTitle(mBook.getTitle());
			AppBarLayout appBarLayout = getActivity().findViewById(R.id.app_bar);
			appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
				@Override
				public void onStateChanged(AppBarLayout appBarLayout, @AppBarStateChangeListener.State int state) {
					collapseToolbar.setTitleEnabled(state == AppBarStateChangeListener.COLLAPSED);
				}
			});

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
							Swatch dominantSwatch = palette.getDominantSwatch();
							if (dominantSwatch != null) {
								int dominantRgb = dominantSwatch.getRgb();
								double[] dominantLab = new double[3];
								ColorUtils.colorToLAB(dominantRgb, dominantLab);

								Map<Double, double[]> distances = new HashMap<>();
								Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();
								if (lightVibrantSwatch != null) {
									double[] lightVibrantLab = new double[3];
									ColorUtils.colorToLAB(lightVibrantSwatch.getRgb(), lightVibrantLab);
									distances.put(ColorUtils.distanceEuclidean(dominantLab, lightVibrantLab), lightVibrantLab);
								}

								Swatch vibrantSwatch = palette.getVibrantSwatch();
								if (vibrantSwatch != null) {
									double[] vibrantLab = new double[3];
									ColorUtils.colorToLAB(vibrantSwatch.getRgb(), vibrantLab);
									distances.put(ColorUtils.distanceEuclidean(dominantLab, vibrantLab), vibrantLab);
								}

								Swatch lightMutedSwatch = palette.getLightMutedSwatch();
								if (lightMutedSwatch != null) {
									double[] lightMutedLab = new double[3];
									ColorUtils.colorToLAB(lightMutedSwatch.getRgb(), lightMutedLab);
									distances.put(ColorUtils.distanceEuclidean(dominantLab, lightMutedLab), lightMutedLab);
								}

								Swatch mutedSwatch = palette.getMutedSwatch();
								if (mutedSwatch != null) {
									double[] mutedLab = new double[3];
									ColorUtils.colorToLAB(mutedSwatch.getRgb(), mutedLab);
									distances.put(ColorUtils.distanceEuclidean(dominantLab, mutedLab), mutedLab);
								}

								Swatch darkMutedSwatch = palette.getDarkMutedSwatch();
								if (darkMutedSwatch != null) {
									double[] darkMutedLab = new double[3];
									ColorUtils.colorToLAB(darkMutedSwatch.getRgb(), darkMutedLab);
									distances.put(ColorUtils.distanceEuclidean(dominantLab, darkMutedLab), darkMutedLab);
								}

								Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
								if (darkVibrantSwatch != null) {
									double[] darkVibrantLab = new double[3];
									ColorUtils.colorToLAB(darkVibrantSwatch.getRgb(), darkVibrantLab);
									distances.put(ColorUtils.distanceEuclidean(dominantLab, darkVibrantLab), darkVibrantLab);
								}

								collapseToolbar.setBackgroundColor(dominantRgb);
								collapseToolbar.setStatusBarScrimColor(dominantRgb);
								collapseToolbar.setContentScrimColor(dominantRgb);
								double[] max = distances.get(Collections.max(distances.keySet()));
								collapseToolbar.setCollapsedTitleTextColor(ColorUtils.LABToColor(max[0], max[1], max[2]));

							}
						}
					});
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.book_detail, container, false);

		ImageView background = rootView.findViewById(R.id.detail_cover);
		GlideApp.with(this).load(mBook.getImage()).into(background);
		background.setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), CoverActivity.class);
			intent.putExtra("url", mBook.getImage());
			// cf https://guides.codepath.com/android/Shared-Element-Activity-Transition#3-start-activity
			ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), background, "cover");

			startActivity(intent, options.toBundle());
		});

		TextView title = rootView.findViewById(R.id.detail_title);
		title.setText(mBook.getTitle());

		TextView subTitle = rootView.findViewById(R.id.detail_sub_title);
		if (TextUtils.isEmpty(mBook.getSubTitle())) {
			subTitle.setVisibility(View.GONE);
		} else {
			subTitle.setVisibility(View.VISIBLE);
			subTitle.setText(mBook.getSubTitle());
		}

		TextView author = rootView.findViewById(R.id.detail_author);
		author.setText(mBook.getAuthor());

		TextView publisher = rootView.findViewById(R.id.detail_publisher);
		publisher.setText(mBook.getPublisher());

		TextView publishedDate = rootView.findViewById(R.id.detail_publication_date);
		LocalDate localDate = LocalDate.parse(mBook.getPublishedDate(), DateTimeFormatter.ISO_DATE);
		publishedDate.setText("Publié le " + localDate.format(LONG_DATE_FORMATTER));

		TextView pageCount = rootView.findViewById(R.id.detail_nb_pages);
		pageCount.setText(mBook.getPageCount() + " pages");

		ExpandableTextView description = rootView.findViewById(R.id.detail_description);
		description.setText(mBook.getDescription());
		// set interpolators for both expanding and collapsing animations
		description.setInterpolator(new OvershootInterpolator());

		View showMore = rootView.findViewById(R.id.detail_show_more);
		showMore.setOnClickListener(v -> {
			showMore.setVisibility(View.GONE);
			description.expand();
		});

		TextView isbn = rootView.findViewById(R.id.detail_isbn);
		isbn.setText(mBook.getIsbn());

		TextView categories = rootView.findViewById(R.id.detail_categories);
		categories.setText(TextUtils.join(" / ", mBook.getCategories()));

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

package fr.frogdevelopment.bibluelle.search;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.Swatch;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.adapter.SimpleBooksAdapter;
import fr.frogdevelopment.bibluelle.data.Book;
import fr.frogdevelopment.bibluelle.rest.google.GoogleRestHelper;

public class BookListActivity extends AppCompatActivity {

	private boolean mTwoPane;

	private String mUrlParameters;
	private SimpleBooksAdapter mAdapter;
	private View mSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_book_list);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setTitle(getTitle());

		// Show the Up button in the action bar.
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		if (findViewById(R.id.book_detail_container) != null) {
			mTwoPane = true;
		}

		mSpinner = findViewById(R.id.spinner);

		RecyclerView recyclerView = findViewById(R.id.book_list);
		mAdapter = new SimpleBooksAdapter(new ArrayList<>(), (v, book) -> showDetails(v.findViewById(R.id.item_cover), book));
		recyclerView.setAdapter(mAdapter);

		String title = getIntent().getStringExtra("title");
		List<String> parameters = new ArrayList<>();
		if (!TextUtils.isEmpty(title)) {
			parameters.add("intitle:" + title);
		}

		String author = getIntent().getStringExtra("author");
		if (!TextUtils.isEmpty(author)) {
			parameters.add("inauthor:" + author);
		}

		String publisher = getIntent().getStringExtra("publisher");
		if (!TextUtils.isEmpty(publisher)) {
			parameters.add("inpublisher:" + publisher);
		}

		if (!parameters.isEmpty()) {
			mUrlParameters = TextUtils.join("+", parameters);
			searchBooks(0);
		}

		recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(recyclerView.getLayoutManager()) {
			@Override
			public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
				searchBooks(page);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onNavigateUpFromChild(this);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void searchBooks(int page) {
		mSpinner.setVisibility(View.VISIBLE);

		GoogleRestHelper.searchBooks(this, mUrlParameters, page, "en,fr", books -> {
			mSpinner.setVisibility(View.GONE);

			if (books != null) {
				if (page == 0 && books.size() == 1) {
					showDetails(null, books.get(0));
				} else {
					mAdapter.addBooks(books);
				}
			}
		});
	}

	private void showDetails(ImageView coverView, Book book) {
		mSpinner.setVisibility(View.VISIBLE);

		int collapsedTitleColor = 0;
		int dominantRgb = 0;

		if (coverView != null) {
			Bitmap bitmap = ((BitmapDrawable) coverView.getDrawable()).getBitmap();
			Palette palette = Palette.from(bitmap).generate();
			Swatch dominantSwatch = palette.getDominantSwatch();
			if (dominantSwatch != null) {
				dominantRgb = dominantSwatch.getRgb();
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

				double[] max = distances.get(Collections.max(distances.keySet()));
				collapsedTitleColor = ColorUtils.LABToColor(max[0], max[1], max[2]);
			}
		}

		int finalDominantRgb = dominantRgb;
		int finalCollapsedTitleColor = collapsedTitleColor;
		GoogleRestHelper.searchDetails(this, book, details -> {
			mSpinner.setVisibility(View.GONE);

			if (details != null) {
				Bundle arguments = new Bundle();
				arguments.putInt("dominantRgb", finalDominantRgb);
				arguments.putInt("collapsedTitleColor", finalCollapsedTitleColor);
				arguments.putSerializable(BookDetailFragment.ARG_KEY, details);
				if (mTwoPane) {
					BookDetailFragment fragment = new BookDetailFragment();
					fragment.setArguments(arguments);
					getSupportFragmentManager().beginTransaction()
							.replace(R.id.book_detail_container, fragment)
							.commit();
				} else {
					Intent intent = new Intent(BookListActivity.this, BookDetailActivity.class);
					intent.putExtras(arguments);
					if (coverView != null) {
						// cf https://guides.codepath.com/android/Shared-Element-Activity-Transition#3-start-activity
						ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(BookListActivity.this, coverView, "cover");

						startActivity(intent, options.toBundle());
					} else {
						startActivity(intent);
					}
				}
			}
		});
	}
}

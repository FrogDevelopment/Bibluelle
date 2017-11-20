package fr.frogdevelopment.bibluelle.details;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.format.FormatStyle;

import at.blogc.android.views.ExpandableTextView;
import fr.frogdevelopment.bibluelle.AppBarStateChangeListener;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.Book;
import fr.frogdevelopment.bibluelle.databinding.BookDetailBinding;

public class BookDetailFragment extends Fragment {

	public static final String ARG_KEY = "book";
	private static final DateTimeFormatter LONG_DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);

	private Book mBook;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBook = (Book) getArguments().getSerializable(ARG_KEY);
		CollapsingToolbarLayout collapseToolbar = getActivity().findViewById(R.id.toolbar_layout);
		if (mBook != null && collapseToolbar != null) {
			AppBarLayout appBarLayout = getActivity().findViewById(R.id.app_bar);
			appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
				@Override
				public void onStateChanged(AppBarLayout appBarLayout, @AppBarStateChangeListener.State int state) {
					collapseToolbar.setTitleEnabled(state == AppBarStateChangeListener.COLLAPSED);
				}
			});

			if (mBook.collapsedTitleColor != 0) {
				collapseToolbar.setCollapsedTitleTextColor(mBook.collapsedTitleColor);
			}

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		BookDetailBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.book_detail, container, false);

		dataBinding.setBook(mBook);

		View rootView = dataBinding.getRoot();

		View background = rootView.findViewById(R.id.detail_cover);
		background.setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), CoverActivity.class);
			intent.putExtra("book", mBook);
			// cf https://guides.codepath.com/android/Shared-Element-Activity-Transition#3-start-activity
			ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), background, "cover");

			startActivity(intent, options.toBundle());
		});

		// fixme via databing
		TextView publishedDate = rootView.findViewById(R.id.detail_publication_date);
		try {
			LocalDate localDate = LocalDate.parse(mBook.publishedDate, DateTimeFormatter.ISO_DATE);
			publishedDate.setText("Publié le " + localDate.format(LONG_DATE_FORMATTER));
		} catch (DateTimeParseException e) {
			e.printStackTrace(); // fixme
			publishedDate.setText("Publié le " + mBook.publishedDate);
		}

		ExpandableTextView description = rootView.findViewById(R.id.detail_description);
		// set interpolators for both expanding and collapsing animations
		description.setInterpolator(new OvershootInterpolator());

		rootView.findViewById(R.id.detail_show_more)
				.setOnClickListener(v -> {
					v.setVisibility(View.GONE);
					description.expand();
				});

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

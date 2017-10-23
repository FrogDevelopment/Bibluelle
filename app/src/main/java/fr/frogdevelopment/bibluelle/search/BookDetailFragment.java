package fr.frogdevelopment.bibluelle.search;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

		CollapsingToolbarLayout appBarLayout = getActivity().findViewById(R.id.toolbar_layout);
		if (appBarLayout != null) {
			appBarLayout.setTitle(mBook.getTitle());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.book_detail, container, false);

		// todo if present, show subTitle

		ImageView background = rootView.findViewById(R.id.detail_background);
		GlideApp.with(container).load(mBook.getImage()).into(background);

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

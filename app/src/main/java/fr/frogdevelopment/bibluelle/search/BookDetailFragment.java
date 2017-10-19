package fr.frogdevelopment.bibluelle.search;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.Book;
import fr.frogdevelopment.bibluelle.data.Origin;

public class BookDetailFragment extends Fragment {

	public static final String ARG_KEY = "book";

	private Book mBook;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_KEY)) {
			mBook = (Book) getArguments().getSerializable(ARG_KEY);

			CollapsingToolbarLayout appBarLayout = getActivity().findViewById(R.id.toolbar_layout);
			if (appBarLayout != null) {
				appBarLayout.setTitle(mBook.getTitle());
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.book_detail, container, false);

		if (mBook != null) {
			ImageView background = rootView.findViewById(R.id.detail_background);
			GlideApp.with(this)
					.load(mBook.getThumbnail())
					.into(background);

			ImageView origin = rootView.findViewById(R.id.detail_origin);
			origin.setImageResource(Origin.getResource(mBook.getOrigin()));

			TextView author = rootView.findViewById(R.id.detail_author);
			author.setText(mBook.getAuthor());

			TextView publisher = rootView.findViewById(R.id.detail_publisher);
			publisher.setText(mBook.getPublisher());

			TextView publishedDate = rootView.findViewById(R.id.detail_published_date);
			publishedDate.setText(mBook.getPublishedDate());

			TextView description = rootView.findViewById(R.id.book_description);
			description.setText(mBook.getDescription());
		}

		return rootView;
	}
}

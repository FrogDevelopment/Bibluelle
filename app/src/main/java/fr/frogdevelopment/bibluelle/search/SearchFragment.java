package fr.frogdevelopment.bibluelle.search;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.frogdevelopment.bibluelle.R;

public class SearchFragment extends Fragment {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchFragment.class);

	private TextInputEditText searchByTitle;
	private TextInputEditText searchByAuthor;
	private TextInputEditText searchByPublisher;
	private TextInputEditText searchByIsbn;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_search, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		searchByTitle = view.findViewById(R.id.search_by_title);
		searchByAuthor = view.findViewById(R.id.search_by_author);
		searchByPublisher = view.findViewById(R.id.search_by_publisher);
		searchByIsbn = view.findViewById(R.id.search_by_isbn);
		view.findViewById(R.id.search_scan).setOnClickListener(v ->
				getFragmentManager()
						.beginTransaction()
						.replace(R.id.content_frame, new ScanFragment(), "SCAN")
						.addToBackStack(null)
						.commit()
		);
		view.findViewById(R.id.search_button).setOnClickListener(v -> onSearch());
	}

	private void onSearch() {
		String title = searchByTitle.getText().toString();
		String author = searchByAuthor.getText().toString();
		String publisher = searchByPublisher.getText().toString();
//		String isbn = searchByIsbn.getText().toString();

		Intent intent = new Intent(getActivity(), BookListActivity.class);
		intent.putExtra("title", title);
		intent.putExtra("author", author);
		intent.putExtra("publisher", publisher);

		startActivity(intent);
	}


	String isbn;

	void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!TextUtils.isEmpty(isbn)) {
			searchByIsbn.setText(isbn);
		}
	}
}

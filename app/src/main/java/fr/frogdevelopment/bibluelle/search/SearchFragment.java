package fr.frogdevelopment.bibluelle.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.lang3.ArrayUtils;

import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.details.BookDetailActivity;
import fr.frogdevelopment.bibluelle.details.BookDetailFragment;
import fr.frogdevelopment.bibluelle.search.rest.google.GoogleRestHelper;
import fr.frogdevelopment.bibluelle.widget.MultiSpinner;

public class SearchFragment extends Fragment {

	private TextInputEditText searchByTitle;
	private TextInputEditText searchByAuthor;
	private TextInputEditText searchByPublisher;
	private TextInputEditText searchByIsbn;
	private String[] selectedCodes;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_search, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		searchByTitle = view.findViewById(R.id.search_by_title);
		searchByAuthor = view.findViewById(R.id.search_by_author);
		searchByPublisher = view.findViewById(R.id.search_by_publisher);
		MultiSpinner multiSpinner = view.findViewById(R.id.search_languages);
		multiSpinner.setMultiSpinnerListener(selected -> {
			String[] codes = getResources().getStringArray(R.array.search_language_codes);
			selectedCodes = null;
			for (int i = 0; i < selected.length; i++) {
				if (selected[i]) {
					selectedCodes = ArrayUtils.add(selectedCodes, codes[i]);
				}
			}
		});
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
		String isbn = searchByIsbn.getText().toString();
		if (!TextUtils.isEmpty(isbn)) {
			showDetails(isbn);
		} else {
			String title = searchByTitle.getText().toString();
			String author = searchByAuthor.getText().toString();
			String publisher = searchByPublisher.getText().toString();

			Intent intent = new Intent(getActivity(), BookListActivity.class);
			intent.putExtra("title", title);
			intent.putExtra("author", author);
			intent.putExtra("publisher", publisher);

			if (selectedCodes == null || selectedCodes.length == 0) {
				selectedCodes = new String[]{"en"}; // fixme
			}
			intent.putExtra("languages", selectedCodes);

			startActivity(intent);
		}
	}

	private void showDetails(String isbn) {
		// fixme show Spinner
		GoogleRestHelper.searchBook(getActivity(), isbn, book -> {
			// fixme hide Spinner
			if (book != null) {
				Intent intent = new Intent(getActivity(), BookDetailActivity.class);
				intent.putExtra(BookDetailFragment.ARG_KEY, book);
				startActivity(intent);
			}
		});
	}

	void setIsbn(String isbn) {
		if (!TextUtils.isEmpty(isbn)) {
			showDetails(isbn);
		}
	}

}

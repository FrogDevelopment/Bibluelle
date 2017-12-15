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
import android.widget.Toast;

import org.apache.commons.lang3.ArrayUtils;

import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.details.BookDetailActivity;
import fr.frogdevelopment.bibluelle.details.BookDetailFragment;
import fr.frogdevelopment.bibluelle.search.rest.google.GoogleRestHelper;
import fr.frogdevelopment.bibluelle.widget.MultiSpinner;

public class SearchFragment extends Fragment {

	private TextInputEditText mSearchByTitle;
	private TextInputEditText mSearchByAuthor;
	private TextInputEditText mSearchByPublisher;
	private TextInputEditText mSearchByIsbn;
	private MultiSpinner mLangRestrict;
	private boolean[] mSelectedLang = null;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_search, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mSearchByTitle = view.findViewById(R.id.search_by_title);
		mSearchByAuthor = view.findViewById(R.id.search_by_author);
		mSearchByPublisher = view.findViewById(R.id.search_by_publisher);
		mSearchByIsbn = view.findViewById(R.id.search_by_isbn);

		mLangRestrict = view.findViewById(R.id.search_languages);
		mLangRestrict.setMultiSpinnerListener(selected -> mSelectedLang = selected);

		view.findViewById(R.id.search_scan).setOnClickListener(v -> onSearchByIsbn());
		view.findViewById(R.id.search_button).setOnClickListener(v -> onSearch());
	}

	private int onSearchByIsbn() {
		return getFragmentManager()
				.beginTransaction()
				.replace(R.id.content_frame, new ScanFragment(), "SCAN")
				.addToBackStack(null)
				.commit();
	}

	private void onSearch() {
		String isbn = mSearchByIsbn.getText().toString();
		if (!TextUtils.isEmpty(isbn)) {
			showDetails(isbn);
		} else {

			String title = mSearchByTitle.getText().toString();
			String author = mSearchByAuthor.getText().toString();
			String publisher = mSearchByPublisher.getText().toString();

			if (TextUtils.isEmpty(title) && TextUtils.isEmpty(author) && TextUtils.isEmpty(publisher)) {
				Toast.makeText(getContext(), "At least 1 field is required", Toast.LENGTH_SHORT).show();
				return;
			}

			String[] codes = getResources().getStringArray(R.array.search_language_codes);
			String[] selectedCodes = null;
			if (mSelectedLang != null) {
				for (int i = 0; i < mSelectedLang.length; i++) {
					if (mSelectedLang[i]) {
						selectedCodes = ArrayUtils.add(selectedCodes, codes[i]);
					}
				}
			}

			if (ArrayUtils.isEmpty(selectedCodes)) {
				Toast.makeText(getContext(), "At least 1 language is required", Toast.LENGTH_SHORT).show();
				return;
			}

			Intent intent = new Intent(getActivity(), BookListActivity.class);
			intent.putExtra("title", title);
			intent.putExtra("author", author);
			intent.putExtra("publisher", publisher);
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

	@Override
	public void onResume() {
		super.onResume();
		mLangRestrict.setSelected(mSelectedLang);
	}
}

package fr.frogdevelopment.bibluelle;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

public class SearchFragment extends Fragment {

	private TextInputEditText searchByTitle;
	private TextInputEditText searchByAuthor;
	private TextInputEditText searchByEditor;
	private TextInputEditText searchByIsbn;
	private ImageButton searchScan;

	public SearchFragment() {
		// Required empty public constructor
	}

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
		searchByEditor = view.findViewById(R.id.search_by_editor);
		searchByIsbn = view.findViewById(R.id.search_by_isbn);
		searchScan = view.findViewById(R.id.search_scan);
		searchScan.setOnClickListener(v ->
				getFragmentManager()
						.beginTransaction()
						.replace(R.id.content_frame, new ScanFragment(), "SCAN")
						.addToBackStack(null)
						.commit()
		);
		Button searchButton = view.findViewById(R.id.search_button);
		searchButton.setOnClickListener(v -> {
			// https://www.googleapis.com/books/v1/volumes?q=isbn:9780134092669
		});
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

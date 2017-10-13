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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.frogdevelopment.bibluelle.rest.google.GoogleApisRestService;
import fr.frogdevelopment.bibluelle.rest.google.GoogleApisRestServiceFactory;
import fr.frogdevelopment.bibluelle.rest.google.GoogleBooks;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchFragment.class);

	private TextInputEditText searchByTitle;
	private TextInputEditText searchByAuthor;
	private TextInputEditText searchByPublisher;
	private TextInputEditText searchByIsbn;
	private ImageButton searchScan;
	private GoogleApisRestService mGoogleApisRestService;

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
		searchByPublisher = view.findViewById(R.id.search_by_publisher);
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

			mGoogleApisRestService.getBooks("isbn:9780134092669").enqueue(new Callback<GoogleBooks>() {
				@Override
				public void onResponse(Call<GoogleBooks> call, Response<GoogleBooks> response) {
					LOGGER.debug(response.message());

					GoogleBooks googleBooks = response.body();
				}

				@Override
				public void onFailure(Call<GoogleBooks> call, Throwable t) {
					LOGGER.error("", t);
				}
			});
		});


		mGoogleApisRestService = GoogleApisRestServiceFactory.getSingleton().getGoogleApisRestService();
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

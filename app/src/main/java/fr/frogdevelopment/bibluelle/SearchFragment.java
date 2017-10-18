package fr.frogdevelopment.bibluelle;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import fr.frogdevelopment.bibluelle.data.Book;
import fr.frogdevelopment.bibluelle.rest.RestServiceFactory;
import fr.frogdevelopment.bibluelle.rest.google.GoogleApisRestService;
import fr.frogdevelopment.bibluelle.rest.google.GoogleBook;
import fr.frogdevelopment.bibluelle.rest.google.GoogleBooks;
import fr.frogdevelopment.bibluelle.rest.google.VolumeInfo;
import fr.frogdevelopment.bibluelle.rest.isbndb.ISBNDBApisRestService;
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
	private ISBNDBApisRestService mIsbnDbApisRestService;

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
		searchButton.setOnClickListener(v -> onSearch());

		mGoogleApisRestService = RestServiceFactory.getGoogleApisRestService();
		mIsbnDbApisRestService = RestServiceFactory.getISBNDBApisRestService();
	}

	private void onSearch() {
		String title = searchByTitle.getText().toString();
		String author = searchByAuthor.getText().toString();
		String publisher = searchByPublisher.getText().toString();
		String isbn = searchByIsbn.getText().toString();

		// GOOGLE API
		callGoogleApi(title, author, publisher, isbn);
	}

	private void callGoogleApi(String title, String author, String publisher, String isbn) {
		List<String> parameters = new ArrayList<>();
		if (!TextUtils.isEmpty(title)) {
			parameters.add("intitle:" + title);
		}

		if (!TextUtils.isEmpty(author)) {
			parameters.add("inauthor:" + author);
		}

		if (!TextUtils.isEmpty(publisher)) {
			parameters.add("inpublisher:" + publisher);
		}

		if (!TextUtils.isEmpty(isbn)) {
			parameters.add("isbn:" + isbn);
		}

		if (!parameters.isEmpty()) {
			String urlParameters = TextUtils.join("+", parameters);

			String fields = "kind,totalItems,items/volumeInfo(title,subtitle,authors,publisher,publishedDate,description,imageLinks)";

			mGoogleApisRestService.getBooks(urlParameters, fields, 40, "books", "en,fr").enqueue(new Callback<GoogleBooks>() {
				@Override
				public void onResponse(@NonNull Call<GoogleBooks> call, @NonNull Response<GoogleBooks> response) {
					if (response.code() == HttpURLConnection.HTTP_OK) {
						GoogleBooks googleBooks = response.body();

						ArrayList<Book> books = new ArrayList<>();
						for (GoogleBook googleBook : googleBooks.getItems()) {
							Book book = new Book();
							VolumeInfo volumeInfo = googleBook.getVolumeInfo();
							book.setTitle(volumeInfo.getTitle());
							book.setAuthor(volumeInfo.getAuthors().toString());
							if (volumeInfo.getImageLinks() != null) {
								book.setThumbnail(volumeInfo.getImageLinks().getSmallThumbnail());
							}

							books.add(book);
						}

						Bundle args = new Bundle();
						args.putSerializable("books", books);

						Fragment fragment = new GalleryFragment();
						fragment.setArguments(args);

						String tag = "GALLERY_FRAGMENT";
						getFragmentManager().beginTransaction()
								.replace(R.id.content_frame, fragment, tag)
								.addToBackStack(null)
								.commit();
					}
				}

				@Override
				public void onFailure(@NonNull Call<GoogleBooks> call, @NonNull Throwable t) {
					LOGGER.error("", t);
				}
			});
		}
	}

	private void callIsbnDbApi(String title, String author, String publisher, String isbn) {
		// http://isbndb.com/api/v2/json/SI7AC64Q/book/9780134092669
		mIsbnDbApisRestService.getBook("SI7AC64Q", "9780134092669").enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
				LOGGER.debug(response.message());

				JsonObject book = response.body();

				LOGGER.debug(book.toString());
			}

			@Override
			public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {

			}
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

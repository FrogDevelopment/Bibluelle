package fr.frogdevelopment.bibluelle.search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.rest.google.GoogleBook;
import fr.frogdevelopment.bibluelle.rest.google.GoogleBooks;
import fr.frogdevelopment.bibluelle.rest.google.GoogleRestService;
import fr.frogdevelopment.bibluelle.rest.google.GoogleRestServiceFactory;
import fr.frogdevelopment.bibluelle.rest.google.VolumeInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailFragment extends Fragment {

	private static final Logger LOGGER = LoggerFactory.getLogger(BookDetailFragment.class);

	public static final String ARG_KEY = "isbn";

	private GoogleRestService mGoogleRestService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGoogleRestService = GoogleRestServiceFactory.getGoogleRestService();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.book_detail, container, false);

		CollapsingToolbarLayout appBarLayout = getActivity().findViewById(R.id.toolbar_layout);
		ImageView background = rootView.findViewById(R.id.detail_background);
		TextView author = rootView.findViewById(R.id.detail_author);
		TextView publisher = rootView.findViewById(R.id.detail_publisher);
		TextView publishedDate = rootView.findViewById(R.id.detail_published_date);
		TextView description = rootView.findViewById(R.id.book_description);

		String isbn = getArguments().getString(ARG_KEY);

		// https://www.googleapis.com/books/v1/volumes?q=isbn:9781473209367&fields=kind,totalItems,items/volumeInfo(title,subtitle,authors,publisher,publishedDate,description,imageLinks,pageCount,categories)
		mGoogleRestService.getDetailForBook("isbn:" + isbn, GoogleRestService.DETAIL_FIELDS).enqueue(new Callback<GoogleBooks>() {
			@Override
			public void onResponse(@NonNull Call<GoogleBooks> call, @NonNull Response<GoogleBooks> response) {
				if (response.code() == HttpURLConnection.HTTP_OK) {
					GoogleBooks googleBooks = response.body();

					if (googleBooks != null && googleBooks.getTotalItems() > 0) {
						if (googleBooks.getItems() != null) {
							GoogleBook googleBook = googleBooks.getItems().get(0);
							VolumeInfo volumeInfo = googleBook.getVolumeInfo();

							if (appBarLayout != null) {
								appBarLayout.setTitle(volumeInfo.getTitle());
							}
							// todo if present, show subTitle
							//https://books.google.com/books/content/images/frontcover/3Cjz7DKv74MC?fife=w200-rw
							String image = String.format("https://books.google.com/books/content/images/frontcover/%s?fife=w200-rw", googleBook.getId());
							GlideApp.with(container).load(image).into(background);
							if (volumeInfo.getAuthors() != null) {
								author.setText(TextUtils.join(",", volumeInfo.getAuthors()));
							}
							publisher.setText(volumeInfo.getPublisher());
							publishedDate.setText(volumeInfo.getPublishedDate());
							description.setText(volumeInfo.getDescription());

							// todo show pageCount
							// todo show categories
						} else {
							// fixme
							Toast.makeText(getActivity(), "No data", Toast.LENGTH_LONG).show();
						}
					} else {
						// fixme
						Toast.makeText(getActivity(), "Error code : " + response.code(), Toast.LENGTH_LONG).show();
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<GoogleBooks> call, @NonNull Throwable t) {
				LOGGER.error("", t);
				Toast.makeText(getActivity(), "Failure : " + t.getMessage(), Toast.LENGTH_LONG).show();
			}
		});

		return rootView;
	}
}

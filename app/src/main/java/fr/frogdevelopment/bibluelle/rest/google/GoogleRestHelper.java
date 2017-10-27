package fr.frogdevelopment.bibluelle.rest.google;


import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import fr.frogdevelopment.bibluelle.data.Book;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoogleRestHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(GoogleRestHelper.class);

	private static final GoogleRestService mGoogleRestService = GoogleRestServiceFactory.getGoogleRestService();

	private static final String PRINT_TYPE = "books";
	private static final int MAX_RESULTS = 40;
	private static final String SEARCH_FIELDS = "kind,totalItems,items/volumeInfo(title,authors,imageLinks(thumbnail),industryIdentifiers)";

	public interface OnSearchBooksListener {
		void onDone(List<Book> books);
	}

	public static void searchBooks(Context context, String parameters, int page, String langRestrict, OnSearchBooksListener listener) {
		int startIndex = page * GoogleRestHelper.MAX_RESULTS;
		mGoogleRestService.searchBooks(parameters, SEARCH_FIELDS, startIndex, MAX_RESULTS, PRINT_TYPE, langRestrict).enqueue(new Callback<GoogleBooks>() {
			@Override
			public void onResponse(@NonNull Call<GoogleBooks> call, @NonNull Response<GoogleBooks> response) {
				ArrayList<Book> books = null;
				if (response.code() == HttpURLConnection.HTTP_OK) {
					GoogleBooks googleBooks = response.body();

					if (googleBooks != null && googleBooks.getTotalItems() > 0) {
						books = new ArrayList<>();
						if (googleBooks.getItems() != null) {
							for (GoogleBook googleBook : googleBooks.getItems()) {
								Book book = new Book();
								VolumeInfo volumeInfo = googleBook.getVolumeInfo();
								book.setTitle(volumeInfo.getTitle());
								if (volumeInfo.getAuthors() != null) {
									book.setAuthor(TextUtils.join(",", volumeInfo.getAuthors()));
								}
								if (volumeInfo.getImageLinks() != null) {
									String thumbnail = volumeInfo.getImageLinks().getThumbnail();
									book.setThumbnail(thumbnail.replaceAll("&edge=curl", ""));
								}

								if (volumeInfo.getIndustryIdentifiers() != null) {
									for (IndustryIdentifiers i : volumeInfo.getIndustryIdentifiers()) {
										if (IndustryIdentifiers.Type.ISBN_13.equals(i.getType())) {
											book.setIsbn(i.getIdentifier());
											books.add(book);
										}
									}
								}
							}
						} else {
							// fixme
							Toast.makeText(context, "No data", Toast.LENGTH_LONG).show();
						}
					} else {
						// fixme
						Toast.makeText(context, "No data", Toast.LENGTH_LONG).show();
					}
				} else {
					// fixme
					Toast.makeText(context, "Error code : " + response.code(), Toast.LENGTH_LONG).show();
				}

				listener.onDone(books);
			}

			@Override
			public void onFailure(@NonNull Call<GoogleBooks> call, @NonNull Throwable t) {
				LOGGER.error("", t);
				Toast.makeText(context, "Failure : " + t.getMessage(), Toast.LENGTH_LONG).show();
				listener.onDone(null);
			}
		});
	}

	private static final String DETAIL_FIELDS = "kind,totalItems,items(id,volumeInfo(title,subtitle,authors,publisher,publishedDate,description,pageCount,categories))";

	public interface OnShowDetailsListener {
		void onDone(Book book);
	}

	public static void showDetails(Context context, String isbn, OnShowDetailsListener listener) {
		// https://www.googleapis.com/books/v1/volumes?q=isbn:9781473209367&fields=kind,totalItems,items/volumeInfo(title,subtitle,authors,publisher,publishedDate,description,imageLinks,pageCount,categories)
		mGoogleRestService.getDetailForBook("isbn:" + isbn, DETAIL_FIELDS).enqueue(new Callback<GoogleBooks>() {
			@Override
			public void onResponse(@NonNull Call<GoogleBooks> call, @NonNull Response<GoogleBooks> response) {
				Book book = null;
				if (response.code() == HttpURLConnection.HTTP_OK) {
					GoogleBooks googleBooks = response.body();

					if (googleBooks != null && googleBooks.getItems() != null) {
						GoogleBook googleBook = googleBooks.getItems().get(0);
						VolumeInfo volumeInfo = googleBook.getVolumeInfo();

						book = new Book();
						book.setTitle(volumeInfo.getTitle());
						book.setSubTitle(volumeInfo.getSubtitle());
						//https://books.google.com/books/content/images/frontcover/3Cjz7DKv74MC?fife=w200-rw
						String image = String.format("https://books.google.com/books/content/images/frontcover/%s?fife=w300-rw", googleBook.getId());
						book.setImage(image);

						if (volumeInfo.getAuthors() != null) {
							book.setAuthor(TextUtils.join(",", volumeInfo.getAuthors()));
						}
						book.setPublisher(volumeInfo.getPublisher());
						book.setPublishedDate(volumeInfo.getPublishedDate());
						book.setDescription(volumeInfo.getDescription());
						book.setPageCount(volumeInfo.getPageCount());
						book.setCategories(volumeInfo.getCategories());
					} else {
						// fixme
						Toast.makeText(context, "No data", Toast.LENGTH_LONG).show();
					}
				} else {
					// fixme
					Toast.makeText(context, "Error code : " + response.code(), Toast.LENGTH_LONG).show();
				}

				listener.onDone(book);
			}

			@Override
			public void onFailure(@NonNull Call<GoogleBooks> call, @NonNull Throwable t) {
				LOGGER.error("", t);
				Toast.makeText(context, "Failure : " + t.getMessage(), Toast.LENGTH_LONG).show();
				listener.onDone(null);
			}
		});
	}

}

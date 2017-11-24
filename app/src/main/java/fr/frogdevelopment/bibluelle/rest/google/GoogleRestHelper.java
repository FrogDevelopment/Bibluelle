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

import fr.frogdevelopment.bibluelle.data.entities.Book;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoogleRestHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(GoogleRestHelper.class);

	private static final GoogleRestService mGoogleRestService = GoogleRestServiceFactory.getGoogleRestService();

	private static final String PRINT_TYPE = "books";
	private static final int MAX_RESULTS = 40;
	private static final String PREVIEW_FIELDS = "totalItems,items/volumeInfo(title,authors,imageLinks(thumbnail),industryIdentifiers)";
	private static final String FULL_FIELDS = "totalItems,items(id,volumeInfo(title,subtitle,authors,imageLinks(thumbnail),publisher,publishedDate,description,pageCount,categories))";
	private static final String DETAIL_FIELDS = "totalItems,items(id,volumeInfo(subtitle,publisher,publishedDate,description,pageCount,categories))";

	public interface OnSearchBooksListener {
		void onDone(List<BookPreview> previews);
	}

	public static void searchBooks(Context context, String parameters, int page, String langRestrict, List<String> isbn, OnSearchBooksListener listener) {
		int startIndex = page * GoogleRestHelper.MAX_RESULTS;
		mGoogleRestService.searchBooks(parameters, PREVIEW_FIELDS, startIndex, MAX_RESULTS, PRINT_TYPE, langRestrict).enqueue(new Callback<GoogleBooks>() {
			@Override
			public void onResponse(@NonNull Call<GoogleBooks> call, @NonNull Response<GoogleBooks> response) {
				ArrayList<BookPreview> previews = null;
				if (response.code() == HttpURLConnection.HTTP_OK) {
					GoogleBooks googleBooks = response.body();

					if (googleBooks != null && googleBooks.getTotalItems() > 0) {
						previews = new ArrayList<>();
						if (googleBooks.getItems() != null) {
							for (GoogleBook googleBook : googleBooks.getItems()) {
								BookPreview preview = new BookPreview();
								VolumeInfo volumeInfo = googleBook.getVolumeInfo();
								preview.title = volumeInfo.getTitle();
								if (volumeInfo.getAuthors() != null) {
									preview.author = TextUtils.join(",", volumeInfo.getAuthors());
								}
								if (volumeInfo.getImageLinks() != null) {
									String thumbnail = volumeInfo.getImageLinks().getThumbnail();
									preview.thumbnailUrl = thumbnail.replaceAll("&edge=curl", "");
								}

								if (volumeInfo.getIndustryIdentifiers() != null) {
									for (IndustryIdentifiers i : volumeInfo.getIndustryIdentifiers()) {
										if (IndustryIdentifiers.Type.ISBN_13.equals(i.getType())) {
											preview.isbn = i.getIdentifier();
											preview.alreadySaved = isbn.contains(preview.isbn);
											previews.add(preview);
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

				listener.onDone(previews);
			}

			@Override
			public void onFailure(@NonNull Call<GoogleBooks> call, @NonNull Throwable t) {
				LOGGER.error("", t);
				Toast.makeText(context, "Failure : " + t.getMessage(), Toast.LENGTH_LONG).show();
				listener.onDone(null);
			}
		});
	}

	public interface OnShowDetailsListener {
		void onDone(Book book);
	}

	public static void searchBook(Context context, String isbn, OnShowDetailsListener listener) {
		mGoogleRestService.getDetailForBook("isbn:" + isbn, FULL_FIELDS).enqueue(new Callback<GoogleBooks>() {
			@Override
			public void onResponse(@NonNull Call<GoogleBooks> call, @NonNull Response<GoogleBooks> response) {
				Book book = null;
				if (response.code() == HttpURLConnection.HTTP_OK) {
					GoogleBooks googleBooks = response.body();

					if (googleBooks != null && googleBooks.getItems() != null) {
						GoogleBook googleBook = googleBooks.getItems().get(0);
						VolumeInfo volumeInfo = googleBook.getVolumeInfo();

						book = new Book();
						book.title = volumeInfo.getTitle();
						book.subTitle = volumeInfo.getSubtitle();

						if (volumeInfo.getAuthors() != null) {
							book.author = TextUtils.join(",", volumeInfo.getAuthors());
						}

						if (volumeInfo.getImageLinks() != null) {
							String thumbnail = volumeInfo.getImageLinks().getThumbnail();
							book.thumbnailUrl = thumbnail.replaceAll("&edge=curl", "");
						}

						book.isbn = isbn;

						//https://books.google.com/books/content/images/frontcover/3Cjz7DKv74MC?fife=w600-rw
						book.coverUrl = String.format("https://books.google.com/books/content/images/frontcover/%s?fife=w600-rw", googleBook.getId());

						if (volumeInfo.getAuthors() != null) {
							book.author = TextUtils.join(",", volumeInfo.getAuthors());
						}
						book.publisher = volumeInfo.getPublisher();
						book.publishedDate = volumeInfo.getPublishedDate();
						book.description = volumeInfo.getDescription();
						book.pageCount = volumeInfo.getPageCount();
						book.categories = TextUtils.join(" / ", volumeInfo.getCategories());
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

	public static void searchDetails(Context context, @NonNull final BookPreview preview, OnShowDetailsListener listener) {
		// https://www.googleapis.com/books/v1/volumes?q=isbn:9781473209367&fields=kind,totalItems,items/volumeInfo(title,subtitle,authors,publisher,publishedDate,description,imageLinks,pageCount,categories)
		mGoogleRestService.getDetailForBook("isbn:" + preview.isbn, DETAIL_FIELDS).enqueue(new Callback<GoogleBooks>() {
			@Override
			public void onResponse(@NonNull Call<GoogleBooks> call, @NonNull Response<GoogleBooks> response) {
				Book book = null;
				if (response.code() == HttpURLConnection.HTTP_OK) {
					GoogleBooks googleBooks = response.body();

					if (googleBooks != null && googleBooks.getItems() != null) {
						GoogleBook googleBook = googleBooks.getItems().get(0);
						VolumeInfo volumeInfo = googleBook.getVolumeInfo();

						book = new Book();
						book.isbn = preview.isbn;
						book.title = preview.title;
						book.author = preview.author;
						book.thumbnailUrl = preview.thumbnailUrl;
						book.subTitle = volumeInfo.getSubtitle();
						//https://books.google.com/books/content/images/frontcover/3Cjz7DKv74MC?fife=w200-rw
						book.coverUrl = String.format("https://books.google.com/books/content/images/frontcover/%s?fife=w300-rw", googleBook.getId());

						if (volumeInfo.getAuthors() != null) {
							book.author = TextUtils.join(",", volumeInfo.getAuthors());
						}
						book.publisher = volumeInfo.getPublisher();
						book.publishedDate = volumeInfo.getPublishedDate();
						book.description = volumeInfo.getDescription();
						book.pageCount = volumeInfo.getPageCount();
						if (volumeInfo.getCategories() != null) {
							book.categories = TextUtils.join(" / ", volumeInfo.getCategories());
						}
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

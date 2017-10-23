package fr.frogdevelopment.bibluelle.rest.google;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleRestService {

	String SEARCH_FIELDS = "kind,totalItems,items/volumeInfo(title,authors,imageLinks(thumbnail),industryIdentifiers)";
	String DETAIL_FIELDS = "kind,totalItems,items(id,volumeInfo(title,subtitle,authors,publisher,publishedDate,description,pageCount,categories))";
	String PRINT_TYPE = "books";
	int MAX_RESULTS = 40;

	// cf https://developers.google.com/books/docs/v1/using#query-params
	// cf https://developers.google.com/books/docs/v1/using#PerformingSearch
	// https://www.googleapis.com/books/v1/volumes?q=inauthor:brandon%20sanderson&fields=kind,totalItems,items/volumeInfo(title,subtitle,authors,publisher,publishedDate,description,imageLinks)&langRestrict=fr,en&startIndex=0&maxResults=40&printType=books

	@GET("https://www.googleapis.com/books/v1/volumes")
	Call<GoogleBooks> searchBooks(@Query("q") String parameter, @Query("fields") String fields, @Query("startIndex") int startIndex, @Query("maxResults") int maxResults, @Query("printType") String printType, @Query("langRestrict") String langRestrict);

	@GET("https://www.googleapis.com/books/v1/volumes")
	Call<GoogleBooks> getDetailForBook(@Query("q") String parameter, @Query("fields") String fields);
}

package fr.frogdevelopment.bibluelle.rest.google;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleApisRestService {

	@GET("https://www.googleapis.com/books/v1/volumes")
	Call<GoogleBooks> getBooks(@Query("q") String parameter);
}

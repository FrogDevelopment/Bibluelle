package fr.frogdevelopment.bibluelle.rest.isbndb;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ISBNDBApisRestService {

	@GET("http://isbndb.com/api/v2/json/{api_key}/book/{isbn}")
	Call<JsonObject> getBook(@Path("api_key") String apiKey, @Path("isbn") String isbn);

	@GET("http://isbndb.com/api/v2/json/{api_key}/books")
	Call<JsonObject> getBooks(@Path("api_key") String apiKey, @Query("q") String parameter);
}

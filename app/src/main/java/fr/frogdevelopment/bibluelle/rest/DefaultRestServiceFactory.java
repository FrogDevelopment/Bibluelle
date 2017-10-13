package fr.frogdevelopment.bibluelle.rest;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DefaultRestServiceFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRestServiceFactory.class);

	// ******************************
	// Common to all Retrofit factory
	// ******************************
	private static Gson gson;
	private static GsonConverterFactory gsonConverterFactory;

	public static void init() {
		if (gson == null) {
			gson = new GsonBuilder()
					.setDateFormat("yyyy-MM-dd\\'T\\'HH:mm:ss")
					.create();

			gsonConverterFactory = GsonConverterFactory.create(gson);
		}
	}

	public static String toJson(Object src) {
		return gson.toJson(src);
	}

	public static <T> T fromJson(String json, Class<T> classOfT) {
		return gson.fromJson(json, classOfT);
	}

	// ******************************
	// Specific to each Retrofit Factory
	// ******************************

	private Retrofit retrofit;

	protected DefaultRestServiceFactory(String baseUrl) {
		init();
		retrofit = new Retrofit.Builder()
				.baseUrl(baseUrl)
				.addConverterFactory(gsonConverterFactory)
				.client(getClientBuilder().build())
				.build();
	}

	public <T> T getService(Class<T> clazz) {
		return retrofit.create(clazz);
	}

	@NonNull
	private OkHttpClient.Builder getClientBuilder() {
		return new OkHttpClient.Builder()
				.readTimeout(0, TimeUnit.SECONDS)
				.connectTimeout(0, TimeUnit.SECONDS)
				.addInterceptor(new LogInterceptor());
	}

	private class LogInterceptor implements Interceptor {

		@Override
		public Response intercept(@NonNull Chain chain) throws IOException {
			Request request = chain.request();

			// add GTS
			Request.Builder requestBuilder = request.newBuilder();

			// set json header
			requestBuilder.addHeader("Content-Type", "application/json; charset=utf-8");
			request = requestBuilder.build();

			// LOG call
			LOGGER.trace(" - Request : {}", request);
			Response response = chain.proceed(request);
			LOGGER.trace(" - Response : {}", response);

			return response;
		}

	}
}

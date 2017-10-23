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

public abstract class RestServiceFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestServiceFactory.class);

	// ******************************
	// Specific to each Retrofit Factory
	// ******************************
	private static final GsonConverterFactory GSON_CONVERTER_FACTORY = buildGsonConverterFactory();

	private static GsonConverterFactory buildGsonConverterFactory() {
		Gson gson = new GsonBuilder()
				.setDateFormat("yyyy-MM-dd\\'T\\'HH:mm:ss")
				.create();

		return GsonConverterFactory.create(gson);
	}

	private Retrofit retrofit;

	protected RestServiceFactory(String baseUrl) {
		retrofit = new Retrofit.Builder()
				.baseUrl(baseUrl)
				.addConverterFactory(GSON_CONVERTER_FACTORY)
				.client(getClientBuilder().build())
				.build();
	}

	protected <T> T getService(Class<T> clazz) {
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

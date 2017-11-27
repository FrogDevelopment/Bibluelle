package fr.frogdevelopment.bibluelle.search.rest.google;

import fr.frogdevelopment.bibluelle.search.rest.RestServiceFactory;

public class GoogleRestServiceFactory extends RestServiceFactory {

	private static final GoogleRestServiceFactory INSTANCE = new GoogleRestServiceFactory();

	public static GoogleRestService getGoogleRestService() {
		return INSTANCE.getService(GoogleRestService.class);
	}

	private GoogleRestServiceFactory() {
		super("https://www.googleapis.com/");
	}
}

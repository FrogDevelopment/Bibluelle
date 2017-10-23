package fr.frogdevelopment.bibluelle.rest.google;

import fr.frogdevelopment.bibluelle.rest.RestServiceFactory;

public class GoogleRestServiceFactory extends RestServiceFactory {

	private static final GoogleRestServiceFactory INSTANCE = new GoogleRestServiceFactory();

	public static GoogleRestService getGoogleRestService() {
		return INSTANCE.getService(GoogleRestService.class);
	}

	private GoogleRestServiceFactory() {
		super("https://www.googleapis.com/");
	}
}

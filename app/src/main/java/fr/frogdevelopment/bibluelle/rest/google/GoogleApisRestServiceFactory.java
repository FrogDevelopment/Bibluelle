package fr.frogdevelopment.bibluelle.rest.google;

import fr.frogdevelopment.bibluelle.rest.DefaultRestServiceFactory;

public class GoogleApisRestServiceFactory extends DefaultRestServiceFactory {

	private static final GoogleApisRestServiceFactory INSTANCE = new GoogleApisRestServiceFactory();

	private GoogleApisRestServiceFactory() {
		super("https://www.googleapis.com");
	}

	public static GoogleApisRestServiceFactory getSingleton() {
		return INSTANCE;
	}

	public GoogleApisRestService getGoogleApisRestService() {
		return getService(GoogleApisRestService.class);
	}
}

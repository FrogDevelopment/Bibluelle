package fr.frogdevelopment.bibluelle.data;

import fr.frogdevelopment.bibluelle.R;

public enum Origin {

	GOOGLE, AMAZON, ISBNDB;

	public static int getResource(Origin origin) {
		switch (origin) {
			case GOOGLE:
				return R.drawable.ic_google;
			case AMAZON:
				return R.drawable.ic_amazon;
			default:
				return -1;
		}
	}
	}

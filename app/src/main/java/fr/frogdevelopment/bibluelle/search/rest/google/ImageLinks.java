package fr.frogdevelopment.bibluelle.search.rest.google;

import java.io.Serializable;

public class ImageLinks implements Serializable{

	private String thumbnail;
	private String small;
	private String medium;

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getSmall() {
		return small;
	}

	public void setSmall(String small) {
		this.small = small;
	}

	public String getMedium() {
		return medium;
	}

	public void setMedium(String medium) {
		this.medium = medium;
	}
}

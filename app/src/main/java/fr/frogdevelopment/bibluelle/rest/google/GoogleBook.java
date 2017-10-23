package fr.frogdevelopment.bibluelle.rest.google;

import java.io.Serializable;

public class GoogleBook implements Serializable {

	private String id;
	private VolumeInfo volumeInfo;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public VolumeInfo getVolumeInfo() {
		return volumeInfo;
	}

	public void setVolumeInfo(VolumeInfo volumeInfo) {
		this.volumeInfo = volumeInfo;
	}

}

package fr.frogdevelopment.bibluelle.rest.google;

import java.io.Serializable;

public class GoogleBook implements Serializable {

	private VolumeInfo volumeInfo;

	public VolumeInfo getVolumeInfo() {
		return volumeInfo;
	}

	public void setVolumeInfo(VolumeInfo volumeInfo) {
		this.volumeInfo = volumeInfo;
	}

}

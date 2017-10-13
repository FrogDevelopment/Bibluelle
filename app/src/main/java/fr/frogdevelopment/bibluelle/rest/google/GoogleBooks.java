package fr.frogdevelopment.bibluelle.rest.google;

import java.io.Serializable;
import java.util.List;

public class GoogleBooks implements Serializable {

	private String kind;
	private int totalItems;
	private List<GoogleBook> items;

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public int getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}

	public List<GoogleBook> getItems() {
		return items;
	}

	public void setItems(List<GoogleBook> items) {
		this.items = items;
	}


}

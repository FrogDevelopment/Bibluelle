package fr.frogdevelopment.bibluelle.data;

import java.io.Serializable;

public class Book implements Serializable {

	private String title;
	private String author;
	private String isbn;
	private String thumbnail;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail.replaceAll("&edge=curl","");
	}
}

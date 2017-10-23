package fr.frogdevelopment.bibluelle.data;

import java.io.Serializable;

public class Book implements Serializable {

	private String title;
	private String author;
	private String publisher;
	private String publishedDate;
	private String isbn;
	private String thumbnail;
	private String image;
	private String description;

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

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(String publishedDate) {
		this.publishedDate = publishedDate;
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
		if (thumbnail != null) {
			this.thumbnail = thumbnail.replaceAll("&edge=curl", "");
		} else {
			this.thumbnail = null;
		}
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		if (image != null) {
			this.image = image.replaceAll("&edge=curl", "");
		} else {
			this.image = null;
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}

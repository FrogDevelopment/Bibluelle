package fr.frogdevelopment.bibluelle.data;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

public class Book implements Serializable {

	private static final long serialVersionUID = 1270210172748954066L;

	private String title;
	private String subTitle;
	private String author;
	private String publisher;
	private String publishedDate;
	private String isbn;
	private String thumbnail;
	private String image;
	private String description;
	private String pageCount;
	private List<String> categories;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
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
		this.thumbnail = thumbnail;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPageCount() {
		return pageCount;
	}

	public void setPageCount(String pageCount) {
		this.pageCount = pageCount;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		Book book = (Book) o;

		return new EqualsBuilder()
				.append(pageCount, book.pageCount)
				.append(title, book.title)
				.append(subTitle, book.subTitle)
				.append(author, book.author)
				.append(publisher, book.publisher)
				.append(publishedDate, book.publishedDate)
				.append(isbn, book.isbn)
				.append(thumbnail, book.thumbnail)
				.append(image, book.image)
				.append(description, book.description)
				.append(categories, book.categories)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(isbn)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("title", title)
				.append("subTitle", subTitle)
				.append("author", author)
				.append("publisher", publisher)
				.append("publishedDate", publishedDate)
				.append("isbn", isbn)
				.append("thumbnail", thumbnail)
				.append("image", image)
				.append("description", description)
				.append("pageCount", pageCount)
				.append("categories", categories)
				.toString();
	}
}

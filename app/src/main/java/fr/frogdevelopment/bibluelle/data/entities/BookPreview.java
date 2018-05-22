package fr.frogdevelopment.bibluelle.data.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class BookPreview implements Serializable {

	private static final long serialVersionUID = -8431787685034051996L;

	@ColumnInfo(name = "isbn")
	public String isbn;

	@ColumnInfo(name = "title")
	public String title;

	@ColumnInfo(name = "sub_title")
	public String subTitle;

	@ColumnInfo(name = "author")
	public String author;

	@ColumnInfo(name = "published_date")
	public String publishedDate;

	@Ignore
	public String publisher;

	@Ignore
	public String thumbnailUrl;

	@Ignore
	public boolean alreadySaved = false;

	public String getThumbnailFile() {
		return isbn + "_thumbnail";
	}

	public String getCoverFile() {
		return isbn + "_cover";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		BookPreview that = (BookPreview) o;

		return new EqualsBuilder()
				.append(isbn, that.isbn)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(isbn)
				.toHashCode();
	}
}

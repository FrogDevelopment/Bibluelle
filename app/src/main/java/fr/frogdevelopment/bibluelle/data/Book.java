package fr.frogdevelopment.bibluelle.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

@Entity(tableName = "book", indices = {@Index(value = "isbn", unique = true)})
public class Book implements Serializable {

	private static final long serialVersionUID = 1270210172748954066L;

	@PrimaryKey(autoGenerate = true)
	public int id;

	@ColumnInfo(name = "title")
	public String title;

	@ColumnInfo(name = "sub_title")
	public String subTitle;

	@ColumnInfo(name = "author")
	public String author;

	@ColumnInfo(name = "publisher")
	public String publisher;

	@ColumnInfo(name = "published_date")
	public String publishedDate;

	@ColumnInfo(name = "isbn")
	public String isbn;

	@ColumnInfo(name = "thumbnail")
	public String thumbnailFile;

	@Ignore
	public String thumbnailUrl;

	@ColumnInfo(name = "cover")
	public String coverFile;

	@Ignore
	public String coverUrl;

	@ColumnInfo(name = "description")
	public String description;

	@ColumnInfo(name = "page_count")
	public String pageCount;

	@ColumnInfo(name = "categories")
	public String categories;

	@Ignore
	public boolean alreadySaved = false;

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
				.append(thumbnailFile, book.thumbnailFile)
				.append(thumbnailUrl, book.thumbnailUrl)
				.append(coverFile, book.coverFile)
				.append(coverUrl, book.coverUrl)
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
				.append("thumbnailFile", thumbnailFile)
				.append("thumbnailUrl", thumbnailUrl)
				.append("coverFile", coverFile)
				.append("coverUrl", coverUrl)
				.append("description", description)
				.append("pageCount", pageCount)
				.append("categories", categories)
				.append("alreadySaved", alreadySaved)
				.toString();
	}
}

package fr.frogdevelopment.bibluelle.data.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;

import java.io.Serializable;

public class BookPreview implements Serializable{

	private static final long serialVersionUID = -8431787685034051996L;

	@ColumnInfo(name = "isbn")
	public String isbn;

	@ColumnInfo(name = "title")
	public String title;

	@ColumnInfo(name = "author")
	public String author;

	@ColumnInfo(name = "thumbnail")
	public String thumbnailFile;

	@Ignore
	public String thumbnailUrl;

	@ColumnInfo(name = "cover")
	public String coverFile;

	@Ignore
	public boolean alreadySaved = false;

}

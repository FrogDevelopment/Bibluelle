package fr.frogdevelopment.bibluelle.data;

import android.arch.persistence.room.ColumnInfo;

public class PreviewTuple {

	@ColumnInfo(name = "title")
	public String title;

	@ColumnInfo(name = "author")
	public String author;

	@ColumnInfo(name = "thumbnail")
	public String thumbnail;
}

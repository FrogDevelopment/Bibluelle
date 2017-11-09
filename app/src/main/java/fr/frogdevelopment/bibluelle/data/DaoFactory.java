package fr.frogdevelopment.bibluelle.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

// https://developer.android.com/training/data-storage/room/migrating-db-versions.html
@Database(entities = {Book.class}, version = 1)
public abstract class DaoFactory extends RoomDatabase {

	static final String DATABASE_NAME = "bibluelle-db";

	public abstract BookDao bookDao();
}

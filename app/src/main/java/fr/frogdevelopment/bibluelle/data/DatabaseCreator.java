package fr.frogdevelopment.bibluelle.data;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import fr.frogdevelopment.bibluelle.data.dao.BookDao;
import fr.frogdevelopment.bibluelle.data.entities.Book;
import fr.frogdevelopment.bibluelle.data.entities.Borrowing;

/**
 * Creates the {@link DaoFactory} asynchronously, exposing a LiveData object to notify of creation.
 * https://github.com/googlesamples/android-architecture-components/tree/master/BasicSample
 */
public class DatabaseCreator {

	// https://developer.android.com/training/data-storage/room/migrating-db-versions.html
	@Database(
			entities = {Book.class, Borrowing.class},
			version = 1
	)
	@TypeConverters({LocalDateConverters.class})
	static abstract class DaoFactory extends RoomDatabase {

		static final String DATABASE_NAME = "bibluelle-db";

		abstract BookDao bookDao();
	}

	private static DatabaseCreator sInstance;

	private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

	private DaoFactory mDb;

	private final AtomicBoolean mInitializing = new AtomicBoolean(true);

	// For Singleton instantiation
	private static final Object LOCK = new Object();

	public synchronized static DatabaseCreator getInstance() {
		if (sInstance == null) {
			synchronized (LOCK) {
				if (sInstance == null) {
					sInstance = new DatabaseCreator();
				}
			}
		}
		return sInstance;
	}

	/**
	 * Used to observe when the database initialization is done
	 */
	public LiveData<Boolean> isDatabaseCreated() {
		return mIsDatabaseCreated;
	}

	/**
	 * Creates or returns a previously-created database.
	 * <p>
	 * Although this uses an AsyncTask which currently uses a serial executor, it's thread-safe.
	 */
	@SuppressLint("StaticFieldLeak")
	public void createDb(Context context) {

		Log.d("DatabaseCreator", "Creating DB from " + Thread.currentThread().getName());

		if (!mInitializing.compareAndSet(true, false)) {
			return; // Already initializing
		}

		mIsDatabaseCreated.setValue(false);// Trigger an update to show a loading screen.
		new AsyncTask<Context, Void, DaoFactory>() {

			@Override
			protected DaoFactory doInBackground(Context... params) {
				Log.d("DatabaseCreator", "Starting bg job " + Thread.currentThread().getName());

				Context context = params[0].getApplicationContext();

				// Build the database!
				DaoFactory db = Room.databaseBuilder(context.getApplicationContext(), DaoFactory.class, DaoFactory.DATABASE_NAME)
//						.addMigrations(MIGRATION_1_2)
						.build();

				// Add some data to the database
				Log.d("DatabaseCreator", "DB was populated in thread " + Thread.currentThread().getName());

				return db;
			}

			@Override
			protected void onPostExecute(DaoFactory db) {
				if (db != null) {
					mDb = db;
				} else {
					// fixme
				}

				// Now on the main thread, notify observers that the db is created and ready.
				mIsDatabaseCreated.setValue(true);
			}
		}.execute(context.getApplicationContext());
	}

	public BookDao getBookDao() {
		return mDb.bookDao();
	}

	static final Migration MIGRATION_1_2 = new Migration(1, 2) {

		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
//			database.execSQL("CREATE TABLE `Fruit` (`id` INTEGER, `name` TEXT, PRIMARY KEY(`id`))");
		}
	};
}

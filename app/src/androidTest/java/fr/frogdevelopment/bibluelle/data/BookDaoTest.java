package fr.frogdevelopment.bibluelle.data;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

// https://developer.android.com/training/data-storage/room/testing-db.html
@RunWith(AndroidJUnit4.class)
public class BookDaoTest {

	private DaoFactory mDb;
	private BookDao mBookDao;

	@Before
	public void createDb() {
		Context context = InstrumentationRegistry.getTargetContext();
		mDb = Room.inMemoryDatabaseBuilder(context, DaoFactory.class).build();
		mBookDao = mDb.bookDao();
	}

	@After
	public void closeDb() throws IOException {
		mDb.close();
	}

	@Test
	public void writeUserAndReadInList() throws Exception {
		// data
		Book book = new Book();
		book.title = "test_title";
		book.author = "test_author";
		book.isbn = "132456";

		// call
		mBookDao.insertBook(book);

		// assert
		List<Book> books = mBookDao.loadAllBooks();
		Assert.assertThat(books, IsCollectionWithSize.hasSize(1));
	}
}

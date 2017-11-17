package fr.frogdevelopment.bibluelle.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface BookDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertBook(Book book);

	@Update
	void updateBook(Book book);

	@Delete
	void deleteBook(Book book);

	@Query("select author, title, thumbnail from book")
	List<PreviewTuple> loadAllPreviews();

	@Query("SELECT * FROM book")
	LiveData<List<Book>> loadAllBooks();

	@Query("SELECT isbn FROM book")
	LiveData<List<String>> loadAllISBN();

	@Query("SELECT DISTINCT author FROM book")
	List<String> loadAllAuthors();

	@Query("SELECT * FROM book WHERE author = :author")
	List<Book> loadAllBooksFromAuthor(String author);
}

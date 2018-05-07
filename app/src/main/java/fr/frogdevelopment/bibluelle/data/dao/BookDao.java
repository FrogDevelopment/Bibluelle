package fr.frogdevelopment.bibluelle.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.os.AsyncTask;

import java.util.List;

import fr.frogdevelopment.bibluelle.data.DatabaseCreator;
import fr.frogdevelopment.bibluelle.data.entities.Book;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;

@Dao
public abstract class BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertBook(Book book);
//
//	@Update
//	void updateBook(Book book);

    @Delete
    abstract void deleteBook(Book book);

    @Query("SELECT isbn FROM book")
    public abstract LiveData<List<String>> getAllIsbn();

    @Query("SELECT isbn, author, title, sub_title FROM book ORDER BY title")
    public abstract LiveData<List<BookPreview>> loadAllPreviews();

    @Query("SELECT * FROM book WHERE isbn = :isbn")
    public abstract LiveData<Book> getBook(String isbn);

    @Query("SELECT * FROM book ORDER BY title")
    public abstract LiveData<List<Book>> loadAllBooks();

    @Query("SELECT isbn FROM book")
    public abstract LiveData<List<String>> loadAllISBN();

//	@Query("SELECT DISTINCT author FROM book")
//	public abstract List<String> loadAllAuthors();
//
//	@Query("SELECT * FROM book WHERE author = :author")
//	public abstract List<Book> loadAllBooksFromAuthor(String author);

    public static void insert(Book book) {
        insert(book, null);
    }

    public static void insert(Book book, InsertBookTask.OnSavedListener listener) {
        // fixme
        if (book.description == null) {
            book.description = "";
        }
        new InsertBookTask(listener).execute(book);
    }

    public static void delete(Book book, DeleteBookTask.OnDeletedListener listener) {
        new DeleteBookTask(listener).execute(book);
    }

    private static class InsertBookTask extends AsyncTask<Book, Void, Void> {

        public interface OnSavedListener {
            void onSave();
        }

        private final InsertBookTask.OnSavedListener mListener;

        private InsertBookTask(InsertBookTask.OnSavedListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Void doInBackground(Book... books) {
            Book book = books[0];

            DatabaseCreator.getInstance().getBookDao().insertBook(book);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mListener != null) {
                mListener.onSave();
            }
        }
    }

    private static class DeleteBookTask extends AsyncTask<Book, Void, Void> {

        public interface OnDeletedListener {
            void onDelete();
        }

        private final DeleteBookTask.OnDeletedListener mListener;

        private DeleteBookTask(DeleteBookTask.OnDeletedListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Void doInBackground(Book... books) {
            Book book = books[0];

            DatabaseCreator.getInstance().getBookDao().deleteBook(book);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mListener.onDelete();
        }
    }
}

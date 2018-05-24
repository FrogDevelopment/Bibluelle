package fr.frogdevelopment.bibluelle.details;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import fr.frogdevelopment.bibluelle.data.DatabaseCreator;
import fr.frogdevelopment.bibluelle.data.entities.Book;

public class DetailViewModel extends ViewModel {

	private final MutableLiveData<String> isbn = new MutableLiveData<>();
	private LiveData<Book> book = null;

	public void setIsbn(String isbn) {
		this.isbn.setValue(isbn);
	}

	public LiveData<Book> getBook() {
		if (book == null) {
			book = DatabaseCreator.getInstance().getBookDao().getBook(isbn.getValue());
		}

		return book;
	}
}

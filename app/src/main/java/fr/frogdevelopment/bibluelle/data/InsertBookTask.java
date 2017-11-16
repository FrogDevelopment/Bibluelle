package fr.frogdevelopment.bibluelle.data;

import android.os.AsyncTask;

public class InsertBookTask extends AsyncTask<Book, Void, Void> {

	public interface OnSavedListener {
		void onSave();
	}

	private final OnSavedListener mListener;

	public InsertBookTask(OnSavedListener listener) {
		this.mListener = listener;
	}

	@Override
	protected Void doInBackground(Book... books) {
		Book book = books[0];

		DaoFactory database = DatabaseCreator.getInstance().getDatabase();

		database.bookDao().insertBook(book);

		return null;
	}

	@Override
	protected void onPostExecute(Void aVoid) {
		mListener.onSave();
	}
}

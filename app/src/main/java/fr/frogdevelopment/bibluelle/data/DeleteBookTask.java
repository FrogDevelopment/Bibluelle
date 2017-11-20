package fr.frogdevelopment.bibluelle.data;

import android.os.AsyncTask;

public class DeleteBookTask extends AsyncTask<Book, Void, Void> {

	public interface OnDeletedListener {
		void onDelete();
	}

	private final OnDeletedListener mListener;

	public DeleteBookTask(OnDeletedListener listener) {
		this.mListener = listener;
	}

	@Override
	protected Void doInBackground(Book... books) {
		Book book = books[0];

		DaoFactory database = DatabaseCreator.getInstance().getDatabase();

		database.bookDao().deleteBook(book);

		return null;
	}

	@Override
	protected void onPostExecute(Void aVoid) {
		mListener.onDelete();
	}
}

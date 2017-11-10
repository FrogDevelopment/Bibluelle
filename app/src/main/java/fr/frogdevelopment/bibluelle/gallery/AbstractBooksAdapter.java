package fr.frogdevelopment.bibluelle.gallery;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import fr.frogdevelopment.bibluelle.data.Book;

abstract class AbstractBooksAdapter<V extends AbstractBooksAdapter.BookViewHolder> extends RecyclerView.Adapter<V> {

	static abstract class BookViewHolder extends RecyclerView.ViewHolder {

		BookViewHolder(View itemView) {
			super(itemView);
		}

		abstract protected void bind(Book book);
	}

	private final List<Book> books;

	AbstractBooksAdapter(List<Book> books) {
		this.books = books;
	}

	@Override
	public void onBindViewHolder(BookViewHolder viewHolder, int position) {
		viewHolder.bind(books.get(position));
	}

	@Override
	public int getItemCount() {
		return books.size();
	}
}

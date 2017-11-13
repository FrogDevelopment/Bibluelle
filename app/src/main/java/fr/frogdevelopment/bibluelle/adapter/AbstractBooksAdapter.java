package fr.frogdevelopment.bibluelle.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import fr.frogdevelopment.bibluelle.data.Book;

public abstract class AbstractBooksAdapter<V extends AbstractBooksAdapter.BookViewHolder> extends RecyclerView.Adapter<V> {

	static abstract class BookViewHolder extends RecyclerView.ViewHolder {

		BookViewHolder(View itemView) {
			super(itemView);
		}

		abstract protected void bind(Book book);
	}

	public interface OnClickListener {
		void onClick(View v, Book book);
	}

	private final List<Book> mBooks;
	private final OnClickListener mListener;

	AbstractBooksAdapter(List<Book> books) {
		this(books, null);
	}

	AbstractBooksAdapter(List<Book> books, OnClickListener listener) {
		this.mBooks = books;
		this.mListener = listener;
	}

	public void addBooks(List<Book> books) {
		mBooks.addAll(books);
		notifyDataSetChanged();
	}

	@Override
	public void onBindViewHolder(BookViewHolder viewHolder, int position) {
		Book book = mBooks.get(position);

		viewHolder.bind(book);

		viewHolder.itemView.setTag(book);
		if (mListener != null) {
			viewHolder.itemView.setOnClickListener(v -> mListener.onClick(v, book));
		} else {
			viewHolder.itemView.setOnClickListener(null);
		}
	}

	@Override
	public int getItemCount() {
		return mBooks.size();
	}
}

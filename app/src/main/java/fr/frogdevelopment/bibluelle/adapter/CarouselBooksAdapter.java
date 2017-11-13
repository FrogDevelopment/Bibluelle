package fr.frogdevelopment.bibluelle.adapter;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.Book;
import fr.frogdevelopment.bibluelle.databinding.ItemCarouselBinding;

public class CarouselBooksAdapter extends AbstractBooksAdapter<CarouselBooksAdapter.ViewHolder> {

	static class ViewHolder extends AbstractBooksAdapter.BookViewHolder {

		private final ItemCarouselBinding mBinding;

		ViewHolder(View itemView) {
			super(itemView);

			mBinding = DataBindingUtil.bind(itemView);
		}

		@Override
		protected void bind(Book book) {
			mBinding.setBook(book);
		}
	}

	public CarouselBooksAdapter(List<Book> books) {
		super(books);
	}

	public CarouselBooksAdapter(List<Book> books, OnClickListener listener) {
		super(books, listener);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carousel, parent, false));
	}

}

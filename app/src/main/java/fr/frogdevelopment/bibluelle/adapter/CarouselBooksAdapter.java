package fr.frogdevelopment.bibluelle.adapter;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.databinding.ItemCarouselBinding;

public class CarouselBooksAdapter extends AbstractBooksAdapter<CarouselBooksAdapter.ViewHolder> {

	static class ViewHolder extends AbstractBooksAdapter.BookViewHolder {

		private final ItemCarouselBinding mBinding;

		ViewHolder(View itemView) {
			super(itemView);

			mBinding = DataBindingUtil.bind(itemView);
		}

		@Override
		protected void bind(BookPreview preview) {
			mBinding.setPreview(preview);
		}
	}

	public CarouselBooksAdapter(List<BookPreview> previews, OnClickListener listener) {
		super(previews, listener);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carousel, parent, false));
	}

}

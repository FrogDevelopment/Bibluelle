package fr.frogdevelopment.bibluelle.adapter;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.databinding.ItemListBinding;

public class ListBooksAdapter extends AbstractBooksAdapter<ListBooksAdapter.ViewHolder> {

	public static class ViewHolder extends AbstractBooksAdapter.BookViewHolder {

		private final ItemListBinding mBinding;

		ViewHolder(View itemView) {
			super(itemView);

			mBinding = DataBindingUtil.bind(itemView);
		}

		@Override
		protected void bind(BookPreview preview) {
			mBinding.setPreview(preview);
		}
	}

	public ListBooksAdapter(List<BookPreview> previews, OnClickListener listener) {
		super(previews, listener);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false));
	}

	public void setBookUpdated(String isbn, boolean saved) {
		for (int i = 0; i < mItems.size(); i++) {
			BookPreview item = mItems.get(i);
			if (item.isbn.equals(isbn)) {
				item.alreadySaved = saved;
				notifyItemChanged(i);
				break;
			}
		}
	}
}

package fr.frogdevelopment.bibluelle.adapter.sectioned;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.adapter.OnClickListener;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.databinding.ItemListBinding;

public class ListBooksSectionedAdapter extends AbstractBooksSectionedAdapter<ListBooksSectionedAdapter.ViewHolder> {

	public static class ViewHolder extends AbstractBooksSectionedAdapter.BookViewHolder {

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

	public ListBooksSectionedAdapter(Map<String, List<BookPreview>> previews, OnClickListener listener) {
		super(previews, listener);
	}

	@Override
	protected ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false));
	}
}

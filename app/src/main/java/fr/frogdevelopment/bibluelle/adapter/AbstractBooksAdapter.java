package fr.frogdevelopment.bibluelle.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import fr.frogdevelopment.bibluelle.data.entities.BookPreview;

public abstract class AbstractBooksAdapter<V extends AbstractBooksAdapter.BookViewHolder> extends RecyclerView.Adapter<V> {

	static abstract class BookViewHolder extends RecyclerView.ViewHolder {

		BookViewHolder(View itemView) {
			super(itemView);
		}

		abstract protected void bind(BookPreview preview);
	}

    private final List<BookPreview> mItems;
	private final OnClickListener mListener;

	AbstractBooksAdapter(List<BookPreview> previews, OnClickListener listener) {
		this.mItems = previews;
		this.mListener = listener;
	}

	@Override
    public void onBindViewHolder(@NonNull BookViewHolder viewHolder, int position) {
		BookPreview preview = mItems.get(position);

		viewHolder.bind(preview);

		viewHolder.itemView.setTag(preview);
		if (mListener != null) {
			viewHolder.itemView.setOnClickListener(v -> mListener.onClick(v, preview));
		} else {
			viewHolder.itemView.setOnClickListener(null);
		}
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}
}

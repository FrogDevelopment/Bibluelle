package fr.frogdevelopment.bibluelle.adapter.sectioned;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.truizlop.sectionedrecyclerview.SimpleSectionedAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.frogdevelopment.bibluelle.adapter.OnClickListener;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;

public abstract class AbstractBooksSectionedAdapter<V extends AbstractBooksSectionedAdapter.BookViewHolder> extends SimpleSectionedAdapter<V> {

	static abstract class BookViewHolder extends RecyclerView.ViewHolder {

		BookViewHolder(View itemView) {
			super(itemView);
		}

		abstract protected void bind(BookPreview preview);
	}

	private final Map<String, List<BookPreview>> mItems;
	protected final OnClickListener mListener;
	private final List<String> sections;

	AbstractBooksSectionedAdapter(Map<String, List<BookPreview>> previews, OnClickListener listener) {
		this.mItems = previews;
		this.sections = new ArrayList<>(mItems.keySet());
		this.mListener = listener;
	}

	@Override
	protected String getSectionHeaderTitle(int section) {
		return getSection(section);
	}

	@Override
	protected int getSectionCount() {
		return sections.size();
	}

	@Override
	protected int getItemCountForSection(int section) {
		return mItems.get(getSection(section)).size();
	}

	private String getSection(int section) {
		return sections.get(section);
	}

	@Override
	protected void onBindItemViewHolder(BookViewHolder viewHolder, int section, int position) {
		BookPreview preview = getItem(section, position);

		viewHolder.bind(preview);

		viewHolder.itemView.setTag(preview);
			viewHolder.itemView.setOnClickListener(v -> mListener.onClick(v, preview));
		}

	protected BookPreview getItem(int section, int position) {
		return mItems.get(getSection(section)).get(position);
	}
}

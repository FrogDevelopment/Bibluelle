package fr.frogdevelopment.bibluelle.gallery.adapter.sectioned;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.truizlop.sectionedrecyclerview.SimpleSectionedAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import fr.frogdevelopment.bibluelle.GlideRequest;
import fr.frogdevelopment.bibluelle.GlideRequests;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.gallery.adapter.FilterableAdapter;
import fr.frogdevelopment.bibluelle.gallery.adapter.OnBookClickListener;

public abstract class AbstractBooksSectionedAdapter<V extends RecyclerView.ViewHolder> extends SimpleSectionedAdapter<V> implements FilterableAdapter {

	final Context mContext;
	final OnBookClickListener mListener;
	final GlideRequest<Drawable> mRequestBuilder;
	private final List<BookPreview> mOriginalItems;
	private final SortedMap<String, List<BookPreview>> mItems = new TreeMap<>();
	private final List<String> mSections = new ArrayList<>();
	private final LayoutInflater mLayoutInflater;

	AbstractBooksSectionedAdapter(Context context, List<BookPreview> previews, OnBookClickListener listener, GlideRequests glideRequests) {
		this.mContext = context;
		this.mOriginalItems = previews;
		transformData(previews);

		this.mListener = listener;

		this.mLayoutInflater = LayoutInflater.from(context);

		this.mRequestBuilder = glideRequests
				.asDrawable()
				.fitCenter();
	}

	private void transformData(List<BookPreview> previews) {
		mItems.clear();
		mSections.clear();

		List<BookPreview> previewsByAuthor;
		for (BookPreview book : previews) {
			if (this.mItems.containsKey(book.author)) {
				previewsByAuthor = this.mItems.get(book.author);
			} else {
				previewsByAuthor = new ArrayList<>();
				this.mItems.put(book.author, previewsByAuthor);
			}

			previewsByAuthor.add(book);
		}

		this.mSections.addAll(mItems.keySet());

		notifyDataSetChanged();
	}

	protected View inflate(@LayoutRes int resource, ViewGroup parent) {
		return mLayoutInflater.inflate(resource, parent, false);
	}

	@Override
	protected String getSectionHeaderTitle(int section) {
		return mContext.getString(R.string.list_section_title, getSection(section), getItemCountForSection(section));
	}

	@Override
	protected int getSectionCount() {
		return mSections.size();
	}

	@Override
	protected int getItemCountForSection(int section) {
		List<BookPreview> itemsForSection = getItemsForSection(section);
		if (itemsForSection == null) {
			return 0; // when filtering
		}
		return itemsForSection.size();
	}

	private List<BookPreview> getItemsForSection(int section) {
		return mItems.get(getSection(section));
	}

	private String getSection(int section) {
		return mSections.get(section);
	}

	BookPreview getItem(int section, int position) {
		return mItems.get(getSection(section)).get(position);
	}

	@Override
	public void reset() {
		transformData(mOriginalItems);
	}

	@Override
	public void filter(String query) {
		BookPreview preview;
		List<BookPreview> booksBySection;
		for (int i = mSections.size() - 1; i >= 0; i--) {
			String section = mSections.get(i);
			booksBySection = mItems.get(section);
			for (int j = booksBySection.size() - 1; j >= 0; j--) {
				preview = booksBySection.get(j);
				if (!preview.title.toLowerCase().contains(query)) {
					booksBySection.remove(j);
					notifyItemRemoved(i + 1 + j);
					notifyItemChanged(i); // update header count
				}
			}

			if (booksBySection.isEmpty()) {
				mItems.remove(section);
				mSections.remove(i);
				notifyItemRemoved(i);
			}
		}
	}

	@Override
	public void filterBack(String query) {
		BookPreview preview;
		List<BookPreview> booksBySection;
		for (int i = mOriginalItems.size() - 1; i >= 0; i--) {
			preview = mOriginalItems.get(i);
			if (preview.title.toLowerCase().contains(query)) {

				if (mSections.contains(preview.author)) {
					if (!mItems.get(preview.author).contains(preview)) {
						booksBySection = new ArrayList<>(mItems.get(preview.author));
						booksBySection.add(preview);
						booksBySection.sort(Comparator.comparing(b -> b.publishedDate));
						mItems.put(preview.author, booksBySection);
						notifyItemInserted(i + booksBySection.indexOf(preview));
						notifyItemChanged(mSections.indexOf(preview.author)); // update header count
					}
				} else {
					mSections.add(preview.author);
					Collections.sort(mSections);
					int sectionPosition = mSections.indexOf(preview.author);
					notifyItemInserted(sectionPosition);
					mItems.put(preview.author, Collections.singletonList(preview));
					notifyItemInserted(sectionPosition + 1);
				}
			}
		}
	}
}

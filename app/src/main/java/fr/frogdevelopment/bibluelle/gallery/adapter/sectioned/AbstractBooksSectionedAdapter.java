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
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import fr.frogdevelopment.bibluelle.GlideRequest;
import fr.frogdevelopment.bibluelle.GlideRequests;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.gallery.adapter.FilterableAdapter;
import fr.frogdevelopment.bibluelle.gallery.adapter.OnBookClickListener;

public abstract class AbstractBooksSectionedAdapter<V extends RecyclerView.ViewHolder> extends SimpleSectionedAdapter<V> implements FilterableAdapter {

    protected final Context mContext;
    protected final OnBookClickListener mListener;
    protected final GlideRequest<Drawable> mRequestBuilder;
    private final List<BookPreview> mPreviews;
    private final SortedMap<String, List<BookPreview>> mItems = new TreeMap<>();
    private final List<String> sections = new ArrayList<>();
    private final LayoutInflater mLayoutInflater;

    AbstractBooksSectionedAdapter(Context context, List<BookPreview> previews, OnBookClickListener listener, GlideRequests glideRequests) {
        this.mContext = context;
        this.mPreviews = previews;
        transformData(previews);

        this.mListener = listener;

        this.mLayoutInflater = LayoutInflater.from(mContext);

        this.mRequestBuilder = glideRequests
                .asDrawable()
                .fitCenter();
    }

    private void transformData(List<BookPreview> previews) {
        mItems.clear();
        sections.clear();

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

        this.sections.addAll(mItems.keySet());

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
        return sections.size();
    }

    @Override
    protected int getItemCountForSection(int section) {
        return getItemsForSection(section).size();
    }

    protected List<BookPreview> getItemsForSection(int section) {
        return mItems.get(getSection(section));
    }

    protected String getSection(int section) {
        return sections.get(section);
    }

    protected BookPreview getItem(int section, int position) {
        return mItems.get(getSection(section)).get(position);
    }

    @Override
    public void reset() {
        transformData(mPreviews);
    }

    public void filter(String query) {
        SortedMap<String, List<BookPreview>> filteredItems = new TreeMap<>();
        for (Map.Entry<String, List<BookPreview>> entry : mItems.entrySet()) {
            List<BookPreview> previews = entry.getValue().stream().filter(p -> p.title.contains(query)).collect(Collectors.toList());
            if (!previews.isEmpty()) {
                filteredItems.put(entry.getKey(), previews);
            }
        }
    }

    //    public void animateTo(List<BookPreview> models) {
//        applyAndAnimateRemovals(models);
//        applyAndAnimateAdditions(models);
//        applyAndAnimateMovedItems(models);
//    }
//
//    private void applyAndAnimateRemovals(List<BookPreview> previews) {
//        for (int i = mPreviews.size() - 1; i >= 0; i--) {
//            final BookPreview model = mPreviews.get(i);
//            if (!previews.contains(model)) {
//                mRecyclerView.getAdapter().notifyItemRemoved(i);
//            }
//        }
//    }
//
//    private void applyAndAnimateAdditions(List<BookPreview> previews) {
//        for (int i = 0, count = previews.size(); i < count; i++) {
//            final BookPreview model = previews.get(i);
//            if (!mPreviews.contains(model)) {
//                mRecyclerView.getAdapter().notifyItemInserted(i);
//            }
//        }
//    }
//
//    private void applyAndAnimateMovedItems(List<BookPreview> previews) {
//        for (int toPosition = previews.size() - 1; toPosition >= 0; toPosition--) {
//            final BookPreview model = previews.get(toPosition);
//            final int fromPosition = mPreviews.indexOf(model);
//            if (fromPosition >= 0 && fromPosition != toPosition) {
//                mRecyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
//            }
//        }
//    }
}

package fr.frogdevelopment.bibluelle.search;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.databinding.ItemSearchBinding;

public class ListBooksAdapter extends RecyclerView.Adapter<ListBooksAdapter.ViewHolder> {

    private final List<BookPreview> mItems = new ArrayList<>();
    private final View.OnClickListener mListener;

    ListBooksAdapter(View.OnClickListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ListBooksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListBooksAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListBooksAdapter.ViewHolder viewHolder, int position) {
        BookPreview preview = mItems.get(position);

        //noinspection ConstantConditions
        viewHolder.mBinding.setPreview(preview);

        viewHolder.itemView.setTag(preview);
        viewHolder.itemView.setOnClickListener(mListener);

        GlideApp.with(viewHolder.itemView.getContext())
                .asDrawable()
                .load(preview.thumbnailUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(viewHolder.mBinding.itemCover);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    void addBooks(List<BookPreview> previews) {
        mItems.addAll(previews);
        notifyDataSetChanged();
    }

    void updateBook(String isbn, boolean saved) {
        for (int i = 0; i < mItems.size(); i++) {
            BookPreview item = mItems.get(i);
            if (item.isbn.equals(isbn)) {
                item.alreadySaved = saved;
                notifyItemChanged(i);
                break;
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemSearchBinding mBinding;

        ViewHolder(View itemView) {
            super(itemView);

            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}

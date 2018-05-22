package fr.frogdevelopment.bibluelle.gallery.adapter.sectioned;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import fr.frogdevelopment.bibluelle.GlideRequests;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.databinding.ItemListBinding;
import fr.frogdevelopment.bibluelle.gallery.adapter.OnBookClickListener;

public class ListBooksSectionedAdapter extends AbstractBooksSectionedAdapter<ListBooksSectionedAdapter.ViewHolder> {

    public ListBooksSectionedAdapter(Context context, List<BookPreview> previews, OnBookClickListener listener, GlideRequests glideRequests) {
        super(context, previews, listener, glideRequests);
    }

    @Override
    protected ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflate(R.layout.item_list, parent));
    }

    @Override
    protected void onBindItemViewHolder(ViewHolder viewHolder, int section, int position) {
        BookPreview preview = getItem(section, position);

        viewHolder.mBinding.setPreview(preview);

        viewHolder.itemView.setOnClickListener(v -> mListener.onBookClick(v, preview.isbn));

        mRequestBuilder
                .clone()
                .placeholder(R.drawable.no_image)
                .load(mContext.getFileStreamPath(preview.getThumbnailFile()))
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(viewHolder.mBinding.itemCover);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemListBinding mBinding;

        ViewHolder(View itemView) {
            super(itemView);

            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}

package fr.frogdevelopment.bibluelle.gallery.adapter.sectioned;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

import java.util.List;
import java.util.SortedMap;

import fr.frogdevelopment.bibluelle.GlideRequests;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.gallery.adapter.OnBookClickListener;

public class GridBooksSectionedAdapter extends AbstractBooksSectionedAdapter<GridBooksSectionedAdapter.ViewHolder> {

    public GridBooksSectionedAdapter(Context context, SortedMap<String, List<BookPreview>> previews, OnBookClickListener listener, GlideRequests glideRequests) {
        super(context, previews, listener, glideRequests);
    }

    @Override
    protected ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = inflate(R.layout.item_grid, parent);

        return new ViewHolder(view);
    }

    @Override
    protected void onBindItemViewHolder(ViewHolder viewHolder, int section, int position) {
        BookPreview preview = getItem(section, position);

        viewHolder.itemView.setOnClickListener(v -> mListener.onBookClick(v, preview.isbn));

        mRequestBuilder
                .clone()
                .load(mContext.getFileStreamPath(preview.getCoverFile()))
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(new DrawableImageViewTarget(viewHolder.mCoverView) {

                    @Override
                    protected void setResource(@Nullable Drawable resource) {
                        RippleDrawable rippledImage = new RippleDrawable(ColorStateList.valueOf(mContext.getColor(R.color.colorPrimaryDark)), resource, null);
                        super.setResource(rippledImage);
                    }
                });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mCoverView;

        ViewHolder(View itemView) {
            super(itemView);

            mCoverView = itemView.findViewById(R.id.item_cover);
        }
    }

}

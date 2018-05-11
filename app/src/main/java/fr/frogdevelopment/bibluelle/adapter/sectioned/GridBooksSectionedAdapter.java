package fr.frogdevelopment.bibluelle.adapter.sectioned;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;
import java.util.Map;

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.adapter.OnClickListener;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;

public class GridBooksSectionedAdapter extends AbstractBooksSectionedAdapter<GridBooksSectionedAdapter.ViewHolder> {

    public GridBooksSectionedAdapter(Map<String, List<BookPreview>> previews, OnClickListener listener) {
        super(previews, listener);
    }

    @Override
    protected ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false));
    }

    @Override
    protected void onBindItemViewHolder(ViewHolder viewHolder, int section, int position) {
        BookPreview preview = getItem(section, position);

        viewHolder.itemView.setTag(preview);
        viewHolder.itemView.setOnClickListener(v -> mListener.onClick(v, preview));

        final Context context = viewHolder.itemView.getContext();

        GlideApp.with(context)
                .asDrawable()
                .load(context.getFileStreamPath(preview.getCoverFile()))
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        RippleDrawable rippledImage = new RippleDrawable(ColorStateList.valueOf(context.getColor(R.color.colorPrimaryDark)), resource, null);

                        viewHolder.mCoverView.setImageDrawable(rippledImage);
                    }
                });
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        int position = holder.getAdapterPosition();

        if (!isSectionHeaderPosition(position) && !isSectionFooterPosition(position)) {
            GlideApp.with(holder.itemView.getContext()).clear(((ViewHolder) holder).mCoverView);
        }
    }

    public static class ViewHolder extends AbstractBooksSectionedAdapter.BookViewHolder {

        private final ImageView mCoverView;

        ViewHolder(View itemView) {
            super(itemView);

            mCoverView = itemView.findViewById(R.id.item_cover);
        }

        @Override
        protected void bind(BookPreview preview) {
        }
    }

}

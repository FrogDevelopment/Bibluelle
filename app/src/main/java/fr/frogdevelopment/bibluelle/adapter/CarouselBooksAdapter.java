package fr.frogdevelopment.bibluelle.adapter;

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

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;

public class CarouselBooksAdapter extends RecyclerView.Adapter<CarouselBooksAdapter.ViewHolder> {

    private final List<BookPreview> mItems;
    private final OnClickListener mListener;

    public CarouselBooksAdapter(List<BookPreview> previews, OnClickListener listener) {
        this.mItems = previews;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carousel, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        BookPreview preview = mItems.get(position);

        viewHolder.itemView.setTag(preview);
        viewHolder.itemView.setOnClickListener(v -> mListener.onClick(v, preview));

        Context context = viewHolder.itemView.getContext();

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
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        GlideApp.with(holder.itemView.getContext()).clear((holder.mCoverView));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mCoverView;

        ViewHolder(View itemView) {
            super(itemView);

            mCoverView = itemView.findViewById(R.id.item_cover);
        }
    }
}

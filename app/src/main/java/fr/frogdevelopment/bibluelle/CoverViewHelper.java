package fr.frogdevelopment.bibluelle;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.frogdevelopment.bibluelle.data.entities.Book;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;

public class CoverViewHelper {

	private CoverViewHelper() {
		//NO-OP
	}

	@BindingAdapter("thumbnail")
	public static void setThumbnail(ImageView imageView, BookPreview preview) {
		if (TextUtils.isEmpty(preview.thumbnailUrl)) {
			loadFromFile(imageView, preview.getThumbnailFile());
		} else {
			loadFromUrl(imageView, preview.thumbnailUrl, DiskCacheStrategy.ALL);
		}
	}

	@BindingAdapter("thumbnail")
	public static void setThumbnail(ImageView imageView, Book book) {
		if (TextUtils.isEmpty(book.thumbnailUrl)) {
			loadFromFile(imageView, book.getThumbnailFile());
		} else {
			loadFromUrl(imageView, book.thumbnailUrl, DiskCacheStrategy.ALL);
		}
	}

	@BindingAdapter("cover")
	public static void setCover(ImageView imageView, BookPreview preview) {
		loadFromFile(imageView, preview.getCoverFile());
	}

	@BindingAdapter("cover")
	public static void setCover(ImageView imageView, Book book) {
		if (TextUtils.isEmpty(book.coverUrl)) {
			loadFromFile(imageView, book.getCoverFile());
		} else {
			loadFromUrl(imageView, book.coverUrl, DiskCacheStrategy.RESOURCE);
		}
	}

	private static void loadFromUrl(ImageView imageView, String url, DiskCacheStrategy diskCacheStrategy) {
		GlideApp.with(imageView.getContext())
				.asDrawable()
				.load(url)
				.diskCacheStrategy(diskCacheStrategy)
				.into(imageView);
	}

	private static void loadFromFile(ImageView imageView, String fileName) {
		Context context = imageView.getContext();

		File file = context.getFileStreamPath(fileName);

		GlideApp.with(context)
				.asDrawable()
				.load(file)
				.into(imageView);
	}

	@BindingAdapter("coverCropTop")
	public static void setCoverCropTop(ImageView target, Book book) {
		Context context = target.getContext();

		Object loadArg = TextUtils.isEmpty(book.coverUrl) ? context.getFileStreamPath(book.getCoverFile()) : book.coverUrl;

		GlideApp.with(context)
				.asDrawable()
				.load(loadArg)
				.into(new SimpleTarget<Drawable>() {

					@Override
					public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
						final float imageWidth = resource.getIntrinsicWidth();
						final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
						final float scaleRatio = screenWidth / imageWidth;

						final Matrix matrix = target.getImageMatrix();
						matrix.postScale(scaleRatio, scaleRatio);
						target.setImageMatrix(matrix);

						target.setImageDrawable(resource);
					}
				});
	}

	public static void searchColors(@Nullable ImageView coverView, @NonNull Book book) {
		if (coverView == null || coverView.getDrawable() == null) {
			return;
		}

		Bitmap bitmap = ((BitmapDrawable) coverView.getDrawable()).getBitmap();

		Palette palette = Palette.from(bitmap).generate();
		Palette.Swatch dominantSwatch = palette.getDominantSwatch();

		if (dominantSwatch != null) {
			book.dominantRgb = dominantSwatch.getRgb();

			double[] dominantLab = new double[3];
			ColorUtils.colorToLAB(book.dominantRgb, dominantLab);

			Map<Double, double[]> distances = new HashMap<>();
			addColor(dominantLab, distances, palette.getLightVibrantSwatch());
			addColor(dominantLab, distances, palette.getVibrantSwatch());
			addColor(dominantLab, distances, palette.getLightMutedSwatch());
			addColor(dominantLab, distances, palette.getMutedSwatch());
			addColor(dominantLab, distances, palette.getDarkMutedSwatch());
			addColor(dominantLab, distances, palette.getDarkVibrantSwatch());

			double[] max = distances.get(Collections.max(distances.keySet()));
			book.collapsedTitleColor = ColorUtils.LABToColor(max[0], max[1], max[2]);
		}
	}

	private static void addColor(double[] dominantLab, Map<Double, double[]> distances, Palette.Swatch swatch) {
		if (swatch != null) {
			double[] vibrantLab = new double[3];
			ColorUtils.colorToLAB(swatch.getRgb(), vibrantLab);
			distances.put(ColorUtils.distanceEuclidean(dominantLab, vibrantLab), vibrantLab);
		}
	}
}

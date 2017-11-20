package fr.frogdevelopment.bibluelle;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import fr.frogdevelopment.bibluelle.data.Book;

public class CoverViewHelper {

	private CoverViewHelper() {
		//NO-OP
	}

	@BindingAdapter("thumbnail")
	public static void setThumbnail(ImageView imageView, Book book) {
		if (!TextUtils.isEmpty(book.thumbnailFile)) {
			loadFromFile(imageView, book.thumbnailFile);
		} else if (!TextUtils.isEmpty(book.thumbnailUrl)) {
			loadFromUrl(imageView, book.thumbnailUrl, DiskCacheStrategy.ALL, 128, 204);
		}
	}

	@BindingAdapter("cover")
	public static void setCover(ImageView imageView, Book book) {
		if (!TextUtils.isEmpty(book.coverFile)) {
			loadFromFile(imageView, book.coverFile);
		} else if (!TextUtils.isEmpty(book.coverUrl)) {
			loadFromUrl(imageView, book.coverUrl, DiskCacheStrategy.RESOURCE, 600, 919);
		}
	}

	private static void loadFromUrl(ImageView imageView, String url, DiskCacheStrategy diskCacheStrategy, int width, int height) {
		GlideApp.with(imageView.getContext())
				.asDrawable()
				.load(url)
//				.placeholder(R.drawable.no_image)
				.override(width, height)
//				.dontAnimate()
				.diskCacheStrategy(diskCacheStrategy)
				.into(imageView);
	}

	private static void loadFromFile(ImageView imageView, String fileName) {
		Context context = imageView.getContext();

		File url = context.getFileStreamPath(fileName);

		GlideApp.with(context)
				.asDrawable()
				.load(url)
				.into(imageView);
	}

	@BindingAdapter("coverCropTop")
	public static void setCoverCropTop(ImageView target, Book book) {
		Context context = target.getContext();

		Object loadArg = TextUtils.isEmpty(book.coverFile) ? book.coverUrl : context.getFileStreamPath(book.coverFile);

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

	public static class Todo {
		public int collapsedTitleColor = 0;
		public int dominantRgb = 0;
	}

	public static Todo todo(@Nullable ImageView coverView) {
		Todo todo = new Todo();
		if (coverView != null) {
			Bitmap bitmap = ((BitmapDrawable) coverView.getDrawable()).getBitmap();
			Palette palette = Palette.from(bitmap).generate();
			Palette.Swatch dominantSwatch = palette.getDominantSwatch();
			if (dominantSwatch != null) {
				todo.dominantRgb = dominantSwatch.getRgb();
				double[] dominantLab = new double[3];
				ColorUtils.colorToLAB(todo.dominantRgb, dominantLab);

				Map<Double, double[]> distances = new HashMap<>();
				Palette.Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();
				if (lightVibrantSwatch != null) {
					double[] lightVibrantLab = new double[3];
					ColorUtils.colorToLAB(lightVibrantSwatch.getRgb(), lightVibrantLab);
					distances.put(ColorUtils.distanceEuclidean(dominantLab, lightVibrantLab), lightVibrantLab);
				}

				Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
				if (vibrantSwatch != null) {
					double[] vibrantLab = new double[3];
					ColorUtils.colorToLAB(vibrantSwatch.getRgb(), vibrantLab);
					distances.put(ColorUtils.distanceEuclidean(dominantLab, vibrantLab), vibrantLab);
				}

				Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();
				if (lightMutedSwatch != null) {
					double[] lightMutedLab = new double[3];
					ColorUtils.colorToLAB(lightMutedSwatch.getRgb(), lightMutedLab);
					distances.put(ColorUtils.distanceEuclidean(dominantLab, lightMutedLab), lightMutedLab);
				}

				Palette.Swatch mutedSwatch = palette.getMutedSwatch();
				if (mutedSwatch != null) {
					double[] mutedLab = new double[3];
					ColorUtils.colorToLAB(mutedSwatch.getRgb(), mutedLab);
					distances.put(ColorUtils.distanceEuclidean(dominantLab, mutedLab), mutedLab);
				}

				Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();
				if (darkMutedSwatch != null) {
					double[] darkMutedLab = new double[3];
					ColorUtils.colorToLAB(darkMutedSwatch.getRgb(), darkMutedLab);
					distances.put(ColorUtils.distanceEuclidean(dominantLab, darkMutedLab), darkMutedLab);
				}

				Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
				if (darkVibrantSwatch != null) {
					double[] darkVibrantLab = new double[3];
					ColorUtils.colorToLAB(darkVibrantSwatch.getRgb(), darkVibrantLab);
					distances.put(ColorUtils.distanceEuclidean(dominantLab, darkVibrantLab), darkVibrantLab);
				}

				double[] max = distances.get(Collections.max(distances.keySet()));
				todo.collapsedTitleColor = ColorUtils.LABToColor(max[0], max[1], max[2]);
			}
		}

		return todo;
	}
}

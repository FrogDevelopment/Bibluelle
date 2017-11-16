package fr.frogdevelopment.bibluelle;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.FileInputStream;
import java.io.IOException;

public class DataBinder {

	private DataBinder() {
		//NO-OP
	}

	@BindingAdapter("thumbnailUrl")
	public static void setThumbnailUrl(ImageView imageView, String url) {
		load(imageView, url, DiskCacheStrategy.ALL);
	}

	@BindingAdapter("imageUrl")
	public static void setImageUrl(ImageView imageView, String url) {
		load(imageView, url, DiskCacheStrategy.RESOURCE);
	}

	private static void load(ImageView imageView, String url, DiskCacheStrategy diskCacheStrategy) {
		GlideApp.with(imageView.getContext())
				.load(url)
				.diskCacheStrategy(diskCacheStrategy)
				.into(imageView);
	}

	@BindingAdapter("imageFile")
	public static void setImageFile(ImageView imageView, String fileName) {
		try (FileInputStream fileInputStream = imageView.getContext().openFileInput(fileName)) {
			Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
			imageView.setImageBitmap(bitmap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

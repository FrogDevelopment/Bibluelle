package fr.frogdevelopment.bibluelle;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.FileInputStream;
import java.io.IOException;

import fr.frogdevelopment.bibluelle.data.Book;

public class DataBinder {

	private DataBinder() {
		//NO-OP
	}

	@BindingAdapter("thumbnail")
	public static void setThumbnail(ImageView imageView, Book book) {
		if (!TextUtils.isEmpty(book.thumbnailFile)) {
			loadFromFile(imageView, book.thumbnailFile);
		} else if (!TextUtils.isEmpty(book.thumbnailUrl)) {
			loadFromUrl(imageView, book.thumbnailUrl, DiskCacheStrategy.ALL,128, 204);
		}
	}

	@BindingAdapter("cover")
	public static void setCover(ImageView imageView, Book book) {
		if (!TextUtils.isEmpty(book.coverFile)) {
			loadFromFile(imageView, book.coverFile);
		} else if (!TextUtils.isEmpty(book.coverUrl)) {
			loadFromUrl(imageView, book.coverUrl, DiskCacheStrategy.RESOURCE,128, 204);
		}
	}

	private static void loadFromUrl(ImageView imageView, String url, DiskCacheStrategy diskCacheStrategy,int width, int height) {
		GlideApp.with(imageView.getContext())
				.load(url)
//				.placeholder(R.drawable.no_image)
				.override(width, height)
//				.dontAnimate()
				.diskCacheStrategy(diskCacheStrategy)
				.into(imageView);
	}

	private static void loadFromFile(ImageView imageView, String fileName) {
		try (FileInputStream fileInputStream = imageView.getContext().openFileInput(fileName)) {
			Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
			imageView.setImageBitmap(bitmap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

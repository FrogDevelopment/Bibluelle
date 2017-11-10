package fr.frogdevelopment.bibluelle;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.widget.ImageView;

public class DataBinder {

	private DataBinder() {
		//NO-OP
	}

	@BindingAdapter("imageUrl")
	public static void setImageUrl(ImageView imageView, String url) {
		Context context = imageView.getContext();
		GlideApp.with(context).load(url).into(imageView);
	}
}

package fr.frogdevelopment.bibluelle.search;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;

public class CoverActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_cover);

		int dominantRgb = getIntent().getIntExtra("dominantRgb", 0);
		if (dominantRgb != 0) {
			getWindow().getDecorView().setBackgroundColor(dominantRgb);
			getWindow().setStatusBarColor(dominantRgb);
		}

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			if (dominantRgb != 0) {
				actionBar.setBackgroundDrawable(new ColorDrawable(dominantRgb));
			}
		}

		ImageView mContentView = findViewById(R.id.fullscreen_content);

		String url = getIntent().getStringExtra("url");
//		DataBinder.setCover(mContentView, );
		GlideApp.with(this).load(url)
				.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
				.into(mContentView);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

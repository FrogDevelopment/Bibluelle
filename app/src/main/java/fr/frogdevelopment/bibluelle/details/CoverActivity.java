package fr.frogdevelopment.bibluelle.details;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;

import fr.frogdevelopment.bibluelle.CoverViewHelper;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.Book;

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

		Book book = (Book) getIntent().getSerializableExtra("book");
		CoverViewHelper.setCover(mContentView, book);
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

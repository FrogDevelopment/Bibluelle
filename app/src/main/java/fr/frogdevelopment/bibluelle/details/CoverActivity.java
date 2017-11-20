package fr.frogdevelopment.bibluelle.details;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.Book;
import fr.frogdevelopment.bibluelle.databinding.ActivityCoverBinding;

public class CoverActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActivityCoverBinding viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_cover);

		Book book = (Book) getIntent().getSerializableExtra("book");

		viewDataBinding.setBook(book);

		if (book.dominantRgb != 0) {
			getWindow().getDecorView().setBackgroundColor(book.dominantRgb);
			getWindow().setStatusBarColor(book.dominantRgb);
		}

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			if (book.dominantRgb != 0) {
				actionBar.setBackgroundDrawable(new ColorDrawable(book.dominantRgb));
			}
		}

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

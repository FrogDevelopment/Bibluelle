package fr.frogdevelopment.bibluelle.details;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.view.MenuItem;
import android.view.Window;

import fr.frogdevelopment.bibluelle.R;

public class BookDetailActivity extends AppCompatActivity {

	public static final String ARG_KEY = "book";
	public static final String ARG_IS_SEARCH = "IS_SEARCH";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_book_detail);

		Fade fade = new Fade();
		fade.excludeTarget(android.R.id.statusBarBackground, true);
		fade.excludeTarget(android.R.id.navigationBarBackground, true);

		Window window = getWindow();
		window.setEnterTransition(fade);
		window.setExitTransition(fade);

		if (savedInstanceState == null) {
			Bundle arguments = new Bundle();
			arguments.putAll(getIntent().getExtras());
			BookDetailFragment fragment = new BookDetailFragment();
			fragment.setArguments(arguments);

			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.book_detail_container, fragment)
					.commit();
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

	@Override
	public void onBackPressed() {
//		if (mBookSaved) {
//			setResult(1, getIntent()); fixme
//		}
		super.onBackPressed();
	}
}

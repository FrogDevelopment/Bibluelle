package fr.frogdevelopment.bibluelle.search;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.Book;
import fr.frogdevelopment.bibluelle.data.DaoFactory;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;

/**
 * An activity representing a single Book detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link BookListActivity}.
 */
public class BookDetailActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_detail);
		Toolbar toolbar = findViewById(R.id.detail_toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(view -> {
			DaoFactory database = DatabaseCreator.getInstance(this.getApplicationContext()).getDatabase();
			Book book = (Book) getIntent().getSerializableExtra(BookDetailFragment.ARG_KEY);
			new InsertBookTask(database, () -> Toast.makeText(getApplicationContext(), "Book saved", Toast.LENGTH_LONG).show()).execute(book);
		});

		// Show the Up button in the action bar.
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putAll(getIntent().getExtras());
			BookDetailFragment fragment = new BookDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.book_detail_container, fragment)
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

	private static class InsertBookTask extends AsyncTask<Book, Void, Void> {

		interface OnSavedListener {
			void onSave();
		}

		private final DaoFactory mDatabase;
		private final OnSavedListener mListener;

		private InsertBookTask(DaoFactory database, OnSavedListener listener) {
			this.mDatabase = database;
			this.mListener = listener;
		}

		@Override
		protected Void doInBackground(Book... books) {
			Book book = books[0];

			mDatabase.bookDao().insertBook(book);

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			mListener.onSave();
		}
	}
}

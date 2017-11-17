package fr.frogdevelopment.bibluelle.search;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.Book;
import fr.frogdevelopment.bibluelle.data.InsertBookTask;

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
		fab.setOnClickListener(view -> saveBook());

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

	private void saveBook() {
		Book book = (Book) getIntent().getSerializableExtra(BookDetailFragment.ARG_KEY);

		// save thumbnail
		GlideApp.with(this)
				.downloadOnly()
				.load(book.thumbnailUrl)
				.into(new SimpleTarget<File>() {
					@Override
					public void onResourceReady(File resource, Transition<? super File> transition) {
						book.thumbnailFile = saveFile(resource);
						saveBook(book);
					}
				});

		// save image
		GlideApp.with(this)
				.downloadOnly()
				.load(book.coverUrl)
				.into(new SimpleTarget<File>() {
					@Override
					public void onResourceReady(File resource, Transition<? super File> transition) {
						book.coverFile = saveFile(resource);
						saveBook(book);
					}
				});
	}

	private void saveBook(Book book) {
		if (book.coverFile != null && book.thumbnailFile != null) {
			// save book
			new InsertBookTask(() -> Toast.makeText(getApplicationContext(), "Book saved", Toast.LENGTH_LONG).show()).execute(book);
		}
	}

	private String saveFile(File file) {
		String fileName = file.getName();

		try (OutputStream fOut = openFileOutput(fileName, MODE_PRIVATE)) {
			Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
		} catch (Exception e) {
			e.printStackTrace(); // fixme
			Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
		}

		return fileName;
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

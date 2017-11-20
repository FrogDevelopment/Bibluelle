package fr.frogdevelopment.bibluelle.details;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.Book;
import fr.frogdevelopment.bibluelle.data.DeleteBookTask;
import fr.frogdevelopment.bibluelle.data.InsertBookTask;
import fr.frogdevelopment.bibluelle.databinding.ActivityBookDetailBinding;
import fr.frogdevelopment.bibluelle.search.BookListActivity;

/**
 * An activity representing a single Book detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link BookListActivity}.
 */
public class BookDetailActivity extends AppCompatActivity {

	private static final Logger LOGGER = LoggerFactory.getLogger(BookDetailActivity.class);

	private Book mBook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActivityBookDetailBinding viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_book_detail);

		Toolbar toolbar = findViewById(R.id.detail_toolbar);
		setSupportActionBar(toolbar);

		// Show the Up button in the action bar.
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		mBook = (Book) getIntent().getSerializableExtra(BookDetailFragment.ARG_KEY);

		viewDataBinding.setBook(mBook);

		FloatingActionButton fab = findViewById(R.id.fab);
//		fab.setImageResource(mBook.id == null ? R.drawable.ic_save : R.drawable.ic_delete);
		fab.setOnClickListener(view -> onClickFab());

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

	private void onClickFab() {
		if (mBook.id == null) {
			saveBook();
		} else {
			deleteBook();
		}
	}

	private void saveBook() {
		// save thumbnail
		GlideApp.with(this)
				.downloadOnly()
				.load(mBook.thumbnailUrl)
				.into(new SimpleTarget<File>() {
					@Override
					public void onResourceReady(File resource, Transition<? super File> transition) {
						mBook.thumbnailFile = saveFile(resource);
						onSaveBook();
					}
				});

		// save image
		GlideApp.with(this)
				.downloadOnly()
				.load(mBook.coverUrl)
				.into(new SimpleTarget<File>() {
					@Override
					public void onResourceReady(File resource, Transition<? super File> transition) {
						mBook.coverFile = saveFile(resource);
						onSaveBook();
					}
				});
	}

	private void onSaveBook() {
		if (mBook.coverFile != null && mBook.thumbnailFile != null) {
			// save book
			new InsertBookTask(() -> Toast.makeText(getApplicationContext(), "Book saved", Toast.LENGTH_LONG).show()).execute(mBook);
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

	private void deleteSavedFile(String fileName) {
		boolean deleted = deleteFile(fileName);
		if (!deleted) {
			LOGGER.warn("File {} not deleted", fileName);
		}
	}

	private void deleteBook() {
		new AlertDialog.Builder(this)
				.setTitle("Suppression")
				.setMessage("Vous êtes sur le point de supprimer ce livre de votre librairie !")
				.setPositiveButton("Continuer", (dialog, which) -> onDeleteBook())
				.setNegativeButton("Annuler", (d, w) -> {
				})
				.show();
	}

	private void onDeleteBook() {
		// delete thumbnail
		deleteSavedFile(mBook.thumbnailFile);
		// delete cover
		deleteSavedFile(mBook.coverFile);

		// delete book
		new DeleteBookTask(() -> {
			Toast.makeText(getApplicationContext(), "Book deleted", Toast.LENGTH_LONG).show();
			finish();
		}).execute(mBook);
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

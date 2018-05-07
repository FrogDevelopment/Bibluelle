package fr.frogdevelopment.bibluelle.details;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.clans.fab.FloatingActionButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.dao.BookDao;
import fr.frogdevelopment.bibluelle.data.entities.Book;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.databinding.ActivityBookDetailBinding;
import fr.frogdevelopment.bibluelle.search.BookListActivity;
import fr.frogdevelopment.bibluelle.search.rest.google.GoogleRestHelper;

/**
 * An activity representing a single Book detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link BookListActivity}.
 */
public class BookDetailActivity extends AppCompatActivity {

	private static final Logger LOGGER = LoggerFactory.getLogger(BookDetailActivity.class);

	private Book mBook;
	private ActivityBookDetailBinding viewDataBinding;
	private boolean mBookSaved = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_book_detail);

		Toolbar toolbar = findViewById(R.id.detail_toolbar);
		setSupportActionBar(toolbar);

		// Show the Up button in the action bar.
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		mBook = (Book) getIntent().getSerializableExtra(BookDetailFragment.ARG_KEY);

		boolean isSearch = getIntent().getBooleanExtra("IS_SEARCH", false);

		viewDataBinding.setBook(mBook);

		if (isSearch) {
			handleFabActions(mBook.alreadySaved);
		} else {
			handleFabActions(true);
		}

		FloatingActionButton fabDelete = findViewById(R.id.fab_delete);
		fabDelete.setOnClickListener(view -> deleteBook());

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

	private void handleFabActions(boolean saved) {
		if (saved) {
			FloatingActionButton fabSync = findViewById(R.id.fab_sync);
			fabSync.setVisibility(View.VISIBLE);
			fabSync.setOnClickListener(view -> syncBook());
		} else {
			FloatingActionButton fabAdd = findViewById(R.id.fab_save);
			fabAdd.setOnClickListener(view -> saveBook());
			fabAdd.setVisibility(View.VISIBLE);
		}
	}

	private void saveBook() {
		if (mBook.alreadySaved) {
			new AlertDialog.Builder(this)
					.setTitle("Attention")
					.setMessage("Book already saved ! You'll override existing data")
					.setPositiveButton(android.R.string.ok, (dialog, which) -> doSaveBook())
					.setNegativeButton(android.R.string.no, (dialog, which) -> {
					})
					.show();
		} else {
			doSaveBook();
		}
	}

	private void doSaveBook() {
		// save thumbnail
		GlideApp.with(this)
				.downloadOnly()
				.load(mBook.thumbnailUrl)
				.into(new SimpleTarget<File>() {
					@Override
					public void onResourceReady(@NonNull File resource, Transition<? super File> transition) {
						thumbnailSaved = saveFile(resource, mBook.getThumbnailFile());
						onSaveBook();
					}
				});

		// save image
		GlideApp.with(this)
				.downloadOnly()
				.load(mBook.coverUrl)
				.into(new SimpleTarget<File>() {
					@Override
					public void onResourceReady(@NonNull File resource, Transition<? super File> transition) {
						coverSaved = saveFile(resource, mBook.getCoverFile());
						onSaveBook();
					}
				});
	}

	boolean thumbnailSaved = false;
	boolean coverSaved = false;

	private void onSaveBook() {
		if (thumbnailSaved && coverSaved) {
			// save book
			BookDao.insert(mBook, this::onBookSaved);

			handleFabActions(true);
		}
	}

	private void onBookSaved() {
		mBookSaved = true;
		Toast.makeText(getApplicationContext(), "Book saved", Toast.LENGTH_LONG).show();
	}

	private boolean saveFile(File file, String fileName) {
		try (OutputStream fOut = openFileOutput(fileName, MODE_PRIVATE)) {
			Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

			return true;
		} catch (Exception e) {
			e.printStackTrace(); // fixme
			Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();

			return false;
		}
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
		deleteSavedFile(mBook.getThumbnailFile());
		// delete cover
		deleteSavedFile(mBook.getCoverFile());

		// delete book
		BookDao.delete(mBook, this::onBookDeleted);
	}

	private void onBookDeleted() {
		Toast.makeText(getApplicationContext(), "Book deleted", Toast.LENGTH_LONG).show();

		Intent data = new Intent();
		data.putExtra("isbn", mBook.isbn);
		setResult(2, data);

		finish();
	}

	private void syncBook() {
		BookPreview preview = new BookPreview();
		preview.isbn = mBook.isbn;
		preview.title = mBook.title;
		preview.author = mBook.author;
		preview.thumbnailUrl = mBook.thumbnailUrl;
		preview.alreadySaved = true;

		GoogleRestHelper.searchDetails(this, preview, book -> {
//			mSpinner.setVisibility(View.GONE); fixme

			if (book != null) {
				mBook = book;
				viewDataBinding.setBook(mBook);

				FloatingActionButton fabAdd = findViewById(R.id.fab_save);
				fabAdd.setOnClickListener(view -> saveBook());
				fabAdd.setVisibility(View.VISIBLE);
			} else {
				// fixme
			}
		});
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
		if (mBookSaved) {
			Intent data = new Intent();
			data.putExtra("isbn", mBook.isbn);
			setResult(1, data);
		}
		super.onBackPressed();
	}
}

package fr.frogdevelopment.bibluelle.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import fr.frogdevelopment.bibluelle.CoverViewHelper;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.adapter.SimpleBooksAdapter;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.details.BookDetailActivity;
import fr.frogdevelopment.bibluelle.details.BookDetailFragment;
import fr.frogdevelopment.bibluelle.search.rest.google.GoogleRestHelper;

public class BookListActivity extends AppCompatActivity {

	private boolean mTwoPane;

	private String mUrlParameters;
	private SimpleBooksAdapter mAdapter;
	private View mSpinner;

	private List<String> isbn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_book_list);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setTitle(getTitle());

		// Show the Up button in the action bar.
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		if (findViewById(R.id.book_detail_container) != null) {
			mTwoPane = true;
		}

		DatabaseCreator.getInstance().getBookDao().loadAllISBN().observe(this, isbn -> {
			this.isbn = isbn;
			search();
		});

		mSpinner = findViewById(R.id.spinner);

		RecyclerView recyclerView = findViewById(R.id.book_list);
		mAdapter = new SimpleBooksAdapter(new ArrayList<>(), (v, preview) -> showDetails(v.findViewById(R.id.item_cover), preview));
		recyclerView.setAdapter(mAdapter);


		recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(recyclerView.getLayoutManager()) {
			@Override
			public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
				searchBooks(page);
			}
		});
	}

	private void search() {
		String title = getIntent().getStringExtra("title");
		List<String> parameters = new ArrayList<>();
		if (!TextUtils.isEmpty(title)) {
			parameters.add("intitle:" + title);
		}

		String author = getIntent().getStringExtra("author");
		if (!TextUtils.isEmpty(author)) {
			parameters.add("inauthor:" + author);
		}

		String publisher = getIntent().getStringExtra("publisher");
		if (!TextUtils.isEmpty(publisher)) {
			parameters.add("inpublisher:" + publisher);
		}

		if (!parameters.isEmpty()) {
			mUrlParameters = TextUtils.join("+", parameters);
			searchBooks(0);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onNavigateUpFromChild(this);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void searchBooks(int page) {
		mSpinner.setVisibility(View.VISIBLE);

		GoogleRestHelper.searchBooks(this, mUrlParameters, page, "en,fr", isbn, previews -> {
			mSpinner.setVisibility(View.GONE);

			if (previews != null) {
				if (page == 0 && previews.size() == 1) {
					showDetails(null, previews.get(0));
				} else {
					mAdapter.addBooks(previews);
				}
			}
		});
	}

	private void showDetails(ImageView coverView, BookPreview preview) {
		mSpinner.setVisibility(View.VISIBLE);

		GoogleRestHelper.searchDetails(this, preview, book -> {
			mSpinner.setVisibility(View.GONE);

			if (book != null) {
				CoverViewHelper.searchColors(coverView, book);

				Bundle arguments = new Bundle();
				arguments.putSerializable(BookDetailFragment.ARG_KEY, book);
				if (mTwoPane) {
					BookDetailFragment fragment = new BookDetailFragment();
					fragment.setArguments(arguments);
					getSupportFragmentManager().beginTransaction()
							.replace(R.id.book_detail_container, fragment)
							.commit();
				} else {
					Intent intent = new Intent(BookListActivity.this, BookDetailActivity.class);
					intent.putExtras(arguments);
					if (coverView != null) {
						// cf https://guides.codepath.com/android/Shared-Element-Activity-Transition#3-start-activity
						ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(BookListActivity.this, coverView, "cover");

						startActivity(intent, options.toBundle());
					} else {
						startActivity(intent);
					}
				}
			}
		});
	}
}

package fr.frogdevelopment.bibluelle.search;

import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.frogdevelopment.bibluelle.CoverViewHelper;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;
import fr.frogdevelopment.bibluelle.data.entities.Book;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.details.BookDetailActivity;
import fr.frogdevelopment.bibluelle.details.BookDetailFragment;
import fr.frogdevelopment.bibluelle.search.rest.google.GoogleRestHelper;

public class BookListActivity extends AppCompatActivity {

    private boolean mTwoPane;

    private String mUrlParameters;
    private ListBooksAdapter mAdapter;
    private View mSpinner;

    private List<String> presentIsbns;
    private String[] languages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_book_list);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (findViewById(R.id.book_detail_container) != null) {
            mTwoPane = true;
        }

        LiveData<List<String>> liveData = DatabaseCreator.getInstance().getBookDao().loadAllISBN();
        liveData.observe(this, isbn -> {
            this.presentIsbns = isbn;

            liveData.removeObservers(BookListActivity.this);

            search();
        });

        mSpinner = findViewById(R.id.spinner);

        RecyclerView recyclerView = findViewById(R.id.book_list);
        mAdapter = new ListBooksAdapter(v -> showDetails(v.findViewById(R.id.item_cover), (BookPreview) v.getTag()));
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

        languages = getIntent().getStringArrayExtra("languages");

        if (!parameters.isEmpty()) {
            mUrlParameters = TextUtils.join("+", parameters);
            searchBooks(0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void searchBooks(int page) {
        mSpinner.setVisibility(View.VISIBLE);

        GoogleRestHelper.searchBooks(this, mUrlParameters, page, TextUtils.join(",", languages), presentIsbns, previews -> {
            mSpinner.setVisibility(View.INVISIBLE);

            if (previews != null) {
                if (page == 0 && previews.size() == 1) {
                    showDetails(null, previews.get(0));
                } else {
                    mAdapter.addBooks(previews);
                }
            } else {
                Toast.makeText(BookListActivity.this, "No more data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDetails(ImageView coverView, BookPreview preview) {
        mSpinner.setVisibility(View.VISIBLE);

        if (preview.alreadySaved) {
            LiveData<Book> bookLiveData = DatabaseCreator.getInstance().getBookDao().getBook(preview.isbn);
            bookLiveData.observe(this, book -> {
                bookLiveData.removeObservers(this);
                mSpinner.setVisibility(View.INVISIBLE);

                if (book != null) {
                    doShowDetails(coverView, book);
                }
            });
        } else {
            GoogleRestHelper.searchDetails(this, preview, book -> {
                mSpinner.setVisibility(View.INVISIBLE);

                if (book != null) {
                    doShowDetails(coverView, book);
                }
            });
        }
    }

    private void doShowDetails(ImageView imageView, Book book) {
        CoverViewHelper.searchColors(imageView, book);

        Bundle arguments = new Bundle();
        arguments.putSerializable(BookDetailActivity.ARG_KEY, book);
        arguments.putBoolean(BookDetailActivity.ARG_IS_SEARCH, true);

        if (mTwoPane) {
            BookDetailFragment fragment = new BookDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.book_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtras(arguments);

            startActivityForResult(intent, 123);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 123 && data != null) {
            String isbn = data.getStringExtra("isbn");
            switch (resultCode) {
                case 1:
                    mAdapter.updateBook(isbn, true);
                    break;
                case 2:
                    mAdapter.updateBook(isbn, false);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

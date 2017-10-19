package fr.frogdevelopment.bibluelle.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.Book;
import fr.frogdevelopment.bibluelle.data.Origin;

/**
 * An activity representing a list of Books. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BookDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BookListActivity extends AppCompatActivity {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
	 */
	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
			// The detail container view will be present only in the
			// large-screen layouts (res/values-w900dp).
			// If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;
		}

		RecyclerView recyclerView = findViewById(R.id.book_list);
		assert recyclerView != null;

		ArrayList<Book> books = (ArrayList<Book>) getIntent().getSerializableExtra("books");
		assert books != null;

		recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, books, mTwoPane));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onNavigateUpFromChild(this);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public static class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

		private final BookListActivity mParentActivity;
		private final List<Book> mBooks;
		private final boolean mTwoPane;

		private final View.OnClickListener mOnClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				Book book = (Book) view.getTag();
				if (mTwoPane) {
					Bundle arguments = new Bundle();
					arguments.putSerializable(BookDetailFragment.ARG_KEY, book);
					BookDetailFragment fragment = new BookDetailFragment();
					fragment.setArguments(arguments);
					mParentActivity.getSupportFragmentManager().beginTransaction()
							.replace(R.id.book_detail_container, fragment)
							.commit();
				} else {
					Context context = view.getContext();
					Intent intent = new Intent(context, BookDetailActivity.class);
					intent.putExtra(BookDetailFragment.ARG_KEY, book);

					context.startActivity(intent);
				}
			}
		};

		SimpleItemRecyclerViewAdapter(BookListActivity parent, List<Book> books, boolean twoPane) {
			mBooks = books;
			mParentActivity = parent;
			mTwoPane = twoPane;
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(final ViewHolder viewHolder, int position) {
			// Get the data model based on position
			Book book = mBooks.get(position);

			// Set item views based on your views and data model
			Glide.with(viewHolder.itemView.getContext())
					.load(book.getThumbnail())
					.into(viewHolder.mThumbnail);
			viewHolder.mTitle.setText(book.getTitle());
			viewHolder.mAuthor.setText(book.getAuthor());
			viewHolder.mOrigin.setImageResource(Origin.getResource(book.getOrigin()));
			viewHolder.itemView.setTag(mBooks.get(position));
			viewHolder.itemView.setOnClickListener(mOnClickListener);
		}

		@Override
		public int getItemCount() {
			return mBooks.size();
		}

		class ViewHolder extends RecyclerView.ViewHolder {

			final ImageView mThumbnail;
			final TextView mTitle;
			final TextView mAuthor;
			final ImageView mOrigin;

			ViewHolder(View itemView) {
				super(itemView);

				mThumbnail = itemView.findViewById(R.id.item_thumbnail);
				mTitle = itemView.findViewById(R.id.item_title);
				mAuthor = itemView.findViewById(R.id.item_author);
				mOrigin = itemView.findViewById(R.id.item_origin);
			}
		}
	}
}

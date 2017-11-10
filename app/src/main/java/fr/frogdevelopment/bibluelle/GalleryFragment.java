package fr.frogdevelopment.bibluelle;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import fr.frogdevelopment.bibluelle.data.Book;
import fr.frogdevelopment.bibluelle.data.DaoFactory;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;

//import com.azoft.carousellayoutmanager.CenterScrollListener;

public class GalleryFragment extends Fragment {

	private View mSpinner;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_gallery, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mSpinner = view.findViewById(R.id.spinner);

		// https://github.com/Azoft/CarouselLayoutManager
//		final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL);
		final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

		final RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setHasFixedSize(true);
//		recyclerView.addOnScrollListener(new CenterScrollListener());

		DaoFactory database = DatabaseCreator.getInstance(getActivity().getApplication()).getDatabase();
		database.bookDao().loadAllBooks().observe(this, books -> {
			mSpinner.setVisibility(View.GONE);
			recyclerView.setAdapter(new BooksAdapter(books));
		});

		// tester aussi https://github.com/GoodieBag/CarouselPicker
	}

	public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.ViewHolder> {

		class ViewHolder extends RecyclerView.ViewHolder {
			final ImageView mThumbnail;
			final TextView mTitle;
			final TextView mAuthor;

			ViewHolder(View itemView) {
				super(itemView);

				mThumbnail = itemView.findViewById(R.id.item_thumbnail);
				mTitle = itemView.findViewById(R.id.item_title);
				mAuthor = itemView.findViewById(R.id.item_author);
			}
		}

		// Store a member variable for the contacts
		private final List<Book> books;

		// Pass in the contact array into the constructor
		BooksAdapter(List<Book> books) {
			this.books = books;
		}

		@Override
		public BooksAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());

			// Inflate the custom layout
			View contactView = inflater.inflate(R.layout.item_book, parent, false);

			// Return a new holder instance
			return new ViewHolder(contactView);
		}

		@Override
		public void onBindViewHolder(BooksAdapter.ViewHolder viewHolder, int position) {
			// Get the data model based on position
			Book contact = books.get(position);

			// Set item views based on your views and data model
			viewHolder.mTitle.setText(contact.title);
			viewHolder.mAuthor.setText(contact.author);


			Glide.with(viewHolder.itemView.getContext())
					.load(contact.thumbnail)
					.into(viewHolder.mThumbnail);
		}

		// Returns the total count of items in the list
		@Override
		public int getItemCount() {
			return books.size();
		}
	}

}

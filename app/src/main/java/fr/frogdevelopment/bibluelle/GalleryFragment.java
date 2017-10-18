package fr.frogdevelopment.bibluelle;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import fr.frogdevelopment.bibluelle.data.Book;

public class GalleryFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_gallery, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// https://github.com/Azoft/CarouselLayoutManager
//		final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL);
		final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

		final RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setHasFixedSize(true);
		recyclerView.addOnScrollListener(new CenterScrollListener());

		Bundle arguments = getArguments();
		ArrayList<Book> books = (ArrayList<Book>) arguments.getSerializable("books");

		recyclerView.setAdapter(new ContactsAdapter(books));

		// tester aussi https://github.com/GoodieBag/CarouselPicker
	}

	public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

		public class ViewHolder extends RecyclerView.ViewHolder {
			public final ImageView mThumbnail;
			public final TextView mTitle;
			public final TextView mAuthor;

			public ViewHolder(View itemView) {
				super(itemView);

				mThumbnail = itemView.findViewById(R.id.item_thumbnail);
				mTitle = itemView.findViewById(R.id.item_title);
				mAuthor = itemView.findViewById(R.id.item_author);
			}
		}

		// Store a member variable for the contacts
		private final ArrayList<Book> book;

		// Pass in the contact array into the constructor
		public ContactsAdapter(ArrayList<Book> books) {
			book = books;
		}

		@Override
		public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());

			// Inflate the custom layout
			View contactView = inflater.inflate(R.layout.item_book, parent, false);

			// Return a new holder instance
			return new ViewHolder(contactView);
		}

		@Override
		public void onBindViewHolder(ContactsAdapter.ViewHolder viewHolder, int position) {
			// Get the data model based on position
			Book contact = book.get(position);

			// Set item views based on your views and data model
			viewHolder.mTitle.setText(contact.getTitle());
			viewHolder.mAuthor.setText(contact.getAuthor());


			Glide.with(viewHolder.itemView.getContext())
					.load(contact.getThumbnail())
					.into(viewHolder.mThumbnail);
		}

		// Returns the total count of items in the list
		@Override
		public int getItemCount() {
			return book.size();
		}
	}

}

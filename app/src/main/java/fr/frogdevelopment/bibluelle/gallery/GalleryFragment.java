package fr.frogdevelopment.bibluelle.gallery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;

import java.util.List;

import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.adapter.CarouselBooksAdapter;
import fr.frogdevelopment.bibluelle.adapter.GridBooksAdapter;
import fr.frogdevelopment.bibluelle.adapter.SimpleBooksAdapter;
import fr.frogdevelopment.bibluelle.data.Book;
import fr.frogdevelopment.bibluelle.data.DaoFactory;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;

public class GalleryFragment extends Fragment {

	private RecyclerView mRecyclerView;
	private List<Book> mBooks;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_gallery, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mRecyclerView = view.findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		DaoFactory database = DatabaseCreator.getInstance().getDatabase();
		database.bookDao().loadAllBooks().observe(this, books -> {
			view.findViewById(R.id.spinner).setVisibility(View.GONE);
			mBooks = books;

			// default
			setCarouselList(books);
		});
	}

	private void setSimpleList(List<Book> books) {
		final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

		mRecyclerView.setLayoutManager(layoutManager);

//		mRecyclerView.removeOnScrollListener();
		mRecyclerView.setAdapter(new SimpleBooksAdapter(books));
	}

	private void setCarouselList(List<Book> books) {
		// https://github.com/Azoft/CarouselLayoutManager
		final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL);
		layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
		layoutManager.setMaxVisibleItems(3);

//		mRecyclerView.addOnScrollListener(new CenterScrollListener());
		mRecyclerView.setLayoutManager(layoutManager);
		mRecyclerView.setAdapter(new CarouselBooksAdapter(books));
	}

	private void setGridList(List<Book> books) {
		final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);

		mRecyclerView.setLayoutManager(layoutManager);

//		mRecyclerView.removeOnScrollListener();
		mRecyclerView.setAdapter(new GridBooksAdapter(books));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.action_simple:
				setSimpleList(mBooks);
				return true;

			case R.id.action_grid:
				setGridList(mBooks);
				return true;

			case R.id.action_carousel:
				setCarouselList(mBooks);
				return true;

			default:
				return false;
		}
	}

}

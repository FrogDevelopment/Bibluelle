package fr.frogdevelopment.bibluelle.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
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
import android.widget.ImageView;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;

import java.util.List;

import fr.frogdevelopment.bibluelle.CoverViewHelper;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.adapter.AbstractBooksAdapter;
import fr.frogdevelopment.bibluelle.adapter.CarouselBooksAdapter;
import fr.frogdevelopment.bibluelle.adapter.GridBooksAdapter;
import fr.frogdevelopment.bibluelle.adapter.SimpleBooksAdapter;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.details.BookDetailActivity;
import fr.frogdevelopment.bibluelle.details.BookDetailFragment;

public class GalleryFragment extends Fragment {

	private RecyclerView mRecyclerView;
	private List<BookPreview> mPreviews;

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

		DatabaseCreator.getInstance().getBookDao().loadAllPreviews().observe(this, books -> {
			view.findViewById(R.id.spinner).setVisibility(View.GONE);
			mPreviews = books;

			// default
			setSimpleList(books);
		});
	}

	private void setSimpleList(List<BookPreview> books) {
		final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

		mRecyclerView.setLayoutManager(layoutManager);

//		mRecyclerView.removeOnScrollListener();
		mRecyclerView.setAdapter(new SimpleBooksAdapter(books, mListener));
	}

	private void setCarouselList(List<BookPreview> books) {
		// https://github.com/Azoft/CarouselLayoutManager
		final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL);
		layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
		layoutManager.setMaxVisibleItems(3);

//		mRecyclerView.addOnScrollListener(new CenterScrollListener());
		mRecyclerView.setLayoutManager(layoutManager);
		mRecyclerView.setAdapter(new CarouselBooksAdapter(books, mListener));
	}

	private void setGridList(List<BookPreview> books) {
		final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);

		mRecyclerView.setLayoutManager(layoutManager);

//		mRecyclerView.removeOnScrollListener();
		mRecyclerView.setAdapter(new GridBooksAdapter(books, mListener));
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
				setSimpleList(mPreviews);
				return true;

			case R.id.action_grid:
				setGridList(mPreviews);
				return true;

			case R.id.action_carousel:
				setCarouselList(mPreviews);
				return true;

			default:
				return false;
		}
	}

	private AbstractBooksAdapter.OnClickListener mListener = (v, preview) -> {
		ImageView coverView = v.findViewById(R.id.item_cover);

		DatabaseCreator.getInstance().getBookDao().getBook(preview.isbn).observe(getActivity(), book -> {

			CoverViewHelper.searchColors(coverView, book);

			Bundle arguments = new Bundle();
			arguments.putSerializable(BookDetailFragment.ARG_KEY, book);

			Intent intent = new Intent(getActivity(), BookDetailActivity.class);
			intent.putExtras(arguments);
			if (coverView != null) {
				// cf https://guides.codepath.com/android/Shared-Element-Activity-Transition#3-start-activity
				ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), coverView, "cover");

				startActivity(intent, options.toBundle());
			} else {
				startActivity(intent);
			}
		});

	};

}

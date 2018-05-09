package fr.frogdevelopment.bibluelle.gallery;

import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ImageView;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.truizlop.sectionedrecyclerview.SectionedSpanSizeLookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.frogdevelopment.bibluelle.CoverViewHelper;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.adapter.CarouselBooksAdapter;
import fr.frogdevelopment.bibluelle.adapter.OnClickListener;
import fr.frogdevelopment.bibluelle.adapter.sectioned.GridBooksSectionedAdapter;
import fr.frogdevelopment.bibluelle.adapter.sectioned.ListBooksSectionedAdapter;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;
import fr.frogdevelopment.bibluelle.data.entities.Book;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.details.BookDetailActivity;
import fr.frogdevelopment.bibluelle.details.BookDetailFragment;

public class GalleryFragment extends Fragment {

	private RecyclerView mRecyclerView;
	private Map<String, List<BookPreview>> mPreviews;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_gallery, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mRecyclerView = view.findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		DatabaseCreator.getInstance().getBookDao().loadAllPreviews().observe(this, books -> {
			view.findViewById(R.id.spinner).setVisibility(View.GONE);

			mPreviews = new HashMap<>();
			if (books != null) {
				List<BookPreview> previews;
				for (BookPreview book : books) {
					// fixme dynamic section
					if (this.mPreviews.containsKey(book.author)) {
						previews = this.mPreviews.get(book.author);
					} else {
						previews = new ArrayList<>();
						this.mPreviews.put(book.author, previews);
					}

					previews.add(book);
				}
			}

			// default
			setGridList(mPreviews);
		});
	}

	private void setSimpleList(Map<String, List<BookPreview>> previews) {
		mRecyclerView.setAdapter(new ListBooksSectionedAdapter(previews, mListener));

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
	}

	private void setCarouselList(Map<String, List<BookPreview>> previews) {
		List<BookPreview> items = new ArrayList<>();
		for (Map.Entry<String, List<BookPreview>> entry : previews.entrySet()) {
			items.addAll(entry.getValue());
		}
		mRecyclerView.setAdapter(new CarouselBooksAdapter(items, mListener));

		// https://github.com/Azoft/CarouselLayoutManager
		final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL);
		layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
		layoutManager.setMaxVisibleItems(3);

		mRecyclerView.setLayoutManager(layoutManager);
	}

	private void setGridList(Map<String, List<BookPreview>> previews) {
		GridBooksSectionedAdapter adapter = new GridBooksSectionedAdapter(previews, mListener);
		mRecyclerView.setAdapter(adapter);

		final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
		SectionedSpanSizeLookup lookup = new SectionedSpanSizeLookup(adapter, layoutManager);
		layoutManager.setSpanSizeLookup(lookup);
		mRecyclerView.setLayoutManager(layoutManager);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.action_list:
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

	private OnClickListener mListener = (v, preview) -> {
		ImageView coverView = v.findViewById(R.id.item_cover);

		LiveData<Book> bookLiveData = DatabaseCreator.getInstance().getBookDao().getBook(preview.isbn);
		bookLiveData.observe(requireActivity(), book -> {

			bookLiveData.removeObservers(requireActivity());

			CoverViewHelper.searchColors(coverView, book);

			Bundle arguments = new Bundle();
			arguments.putSerializable(BookDetailFragment.ARG_KEY, book);

			Intent intent = new Intent(requireContext(), BookDetailActivity.class);
			intent.putExtras(arguments);
//			if (coverView != null) {
//				// cf https://guides.codepath.com/android/Shared-Element-Activity-Transition#3-start-activity
//				ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), coverView, "cover");
//
//				startActivity(intent, options.toBundle());
//			} else {
				startActivity(intent);
//			}
		});

	};

}

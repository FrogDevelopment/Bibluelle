package fr.frogdevelopment.bibluelle.gallery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;

import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.DaoFactory;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;

public class GalleryFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_gallery, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		View spinner = view.findViewById(R.id.spinner);
		boolean isCarousel = true;


		final RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
		recyclerView.setHasFixedSize(true);

		if (isCarousel) {
			// https://github.com/Azoft/CarouselLayoutManager
			final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL);
			layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
			layoutManager.setMaxVisibleItems(3);

			recyclerView.addOnScrollListener(new CenterScrollListener());
			recyclerView.setLayoutManager(layoutManager);
		} else {
			final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

			recyclerView.setLayoutManager(layoutManager);
		}

		DaoFactory database = DatabaseCreator.getInstance(getActivity().getApplication()).getDatabase();
		database.bookDao().loadAllBooks().observe(this, books -> {
			spinner.setVisibility(View.GONE);
			if (isCarousel) {
				recyclerView.setAdapter(new CarouselBooksAdapter(books));
			} else {
				recyclerView.setAdapter(new SimpleBooksAdapter(books));
			}
		});
	}

}

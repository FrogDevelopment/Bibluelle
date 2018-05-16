package fr.frogdevelopment.bibluelle.gallery;

import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
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
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.GlideRequests;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;
import fr.frogdevelopment.bibluelle.data.entities.Book;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.details.BookDetailActivity;
import fr.frogdevelopment.bibluelle.gallery.adapter.CarouselBooksAdapter;
import fr.frogdevelopment.bibluelle.gallery.adapter.OnBookClickListener;
import fr.frogdevelopment.bibluelle.gallery.adapter.sectioned.GridBooksSectionedAdapter;
import fr.frogdevelopment.bibluelle.gallery.adapter.sectioned.ListBooksSectionedAdapter;

public class GalleryFragment extends Fragment implements OnBookClickListener {

    private RecyclerView mRecyclerView;
    private SortedMap<String, List<BookPreview>> mPreviews;
    private GlideRequests mGlideRequests;

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

        mGlideRequests = GlideApp.with(this);

        DatabaseCreator.getInstance().getBookDao().loadAllPreviews().observe(this, books -> {
            view.findViewById(R.id.spinner).setVisibility(View.GONE);

            mPreviews = new TreeMap<>();
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

    private void setSimpleList(SortedMap<String, List<BookPreview>> previews) {
        // ADAPTER
        ListBooksSectionedAdapter adapter = new ListBooksSectionedAdapter(requireContext(), previews, this, mGlideRequests);
        mRecyclerView.setAdapter(adapter);

        // LAYOUT MANAGER
        LinearLayoutManager layout = new LinearLayoutManager(requireContext());
        mRecyclerView.setLayoutManager(layout);
    }

    private void setCarouselList(SortedMap<String, List<BookPreview>> previews) {
        // ADAPTER
        List<BookPreview> items = new ArrayList<>();
        for (Map.Entry<String, List<BookPreview>> entry : previews.entrySet()) {
            items.addAll(entry.getValue());
        }
        CarouselBooksAdapter adapter = new CarouselBooksAdapter(requireContext(), items, this, mGlideRequests);
        mRecyclerView.setAdapter(adapter);

        // LAYOUT MANAGER https://github.com/Azoft/CarouselLayoutManager
        final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL);
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
        layoutManager.setMaxVisibleItems(3);

        mRecyclerView.setLayoutManager(layoutManager);
    }

    private void setGridList(SortedMap<String, List<BookPreview>> previews) {
        // ADAPTER
        GridBooksSectionedAdapter adapter = new GridBooksSectionedAdapter(requireContext(), previews, this, mGlideRequests);
        mRecyclerView.setAdapter(adapter);

        // LAYOUT MANAGER
        final GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 3);
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

    @Override
    public void onBookClick(View v, String isbn) {
        ImageView coverView = v.findViewById(R.id.item_cover);

        LiveData<Book> bookLiveData = DatabaseCreator.getInstance().getBookDao().getBook(isbn);
        bookLiveData.observe(requireActivity(), book -> {

            bookLiveData.removeObservers(requireActivity());

            Bundle arguments = new Bundle();
            arguments.putSerializable(BookDetailActivity.ARG_KEY, book);

            Intent intent = new Intent(requireContext(), BookDetailActivity.class);
            intent.putExtras(arguments);

            // https://android-developers.googleblog.com/2018/02/continuous-shared-element-transitions.html
            // https://guides.codepath.com/android/Shared-Element-Activity-Transition#3-start-activity

            // https://stackoverflow.com/questions/26600263/how-do-i-prevent-the-status-bar-and-navigation-bar-from-animating-during-an-acti
            Fade fade = new Fade();
            fade.excludeTarget(coverView, true);
            // https://stackoverflow.com/questions/26567822/hiccups-in-activity-transitions-with-shared-elements
//            fade.excludeTarget(requireActivity().findViewById(R.id.navigation), true);

            setEnterTransition(fade);
            setExitTransition(fade);

            // https://stackoverflow.com/questions/36137400/ripple-effect-not-working-with-shared-element-transition-and-recyclerview

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), coverView, "cover");
            startActivity(intent, options.toBundle());
        });

    }

}

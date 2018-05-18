package fr.frogdevelopment.bibluelle.gallery;

import android.app.SearchManager;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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

import java.util.List;

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
    private List<BookPreview> mPreviews;
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

            // Get the intent, verify the action and get the query
//            Intent intent = requireActivity().getIntent();
//            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//                String query = intent.getStringExtra(SearchManager.QUERY);
//                if (TextUtils.isEmpty(query)) {
//                    mPreviews = books;
//                } else {
//                    mPreviews = books.stream().filter(p -> p.title.contains(query)).collect(Collectors.toList());
//                }
//            } else {
//                mPreviews = books;
//            } // fixme do not start a new activity
//                mPreviews = books;

            // default
            setGridList();
        });
    }

    private void setSimpleList() {
        // ADAPTER
        ListBooksSectionedAdapter adapter = new ListBooksSectionedAdapter(requireContext(), mPreviews, this, mGlideRequests);
        mRecyclerView.setAdapter(adapter);

        // LAYOUT MANAGER
        LinearLayoutManager layout = new LinearLayoutManager(requireContext());
        mRecyclerView.setLayoutManager(layout);
    }

    private void setCarouselList() {
        // ADAPTER
        CarouselBooksAdapter adapter = new CarouselBooksAdapter(requireContext(), mPreviews, this, mGlideRequests);
        mRecyclerView.setAdapter(adapter);

        // LAYOUT MANAGER https://github.com/Azoft/CarouselLayoutManager
        final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL);
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
        layoutManager.setMaxVisibleItems(3);

        mRecyclerView.setLayoutManager(layoutManager);
    }

    private void setGridList() {
        // ADAPTER
        GridBooksSectionedAdapter adapter = new GridBooksSectionedAdapter(requireContext(), mPreviews, this, mGlideRequests);
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

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) requireContext().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default
        searchView.setSubmitButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.gallery_action_list:
                setSimpleList();
                return true;

            case R.id.gallery_action_grid:
                setGridList();
                return true;

            case R.id.gallery_action_carousel:
                setCarouselList();
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

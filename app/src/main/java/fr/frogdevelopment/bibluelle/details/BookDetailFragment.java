package fr.frogdevelopment.bibluelle.details;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.transition.Transition;
import android.support.transition.TransitionInflater;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.clans.fab.FloatingActionButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.format.FormatStyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import fr.frogdevelopment.bibluelle.AppBarStateChangeListener;
import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.dao.BookDao;
import fr.frogdevelopment.bibluelle.data.entities.Book;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;
import fr.frogdevelopment.bibluelle.databinding.FragmentBookDetailBinding;
import fr.frogdevelopment.bibluelle.search.rest.google.GoogleRestHelper;

public class BookDetailFragment extends Fragment {

	private static final Logger LOGGER = LoggerFactory.getLogger(BookDetailActivity.class);

	private static final DateTimeFormatter LONG_DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
	boolean thumbnailSaved = false;
	boolean coverSaved = false;
	private int topBackgroundColor;
	private DetailViewModel model;
	private Book mBook;
	private boolean mBookSaved = false;
	private View mRootView;
	private FragmentBookDetailBinding dataBinding;

	private static void addColor(double[] dominantLab, Map<Double, double[]> distances, Palette.Swatch swatch) {
		if (swatch != null) {
			double[] vibrantLab = new double[3];
			ColorUtils.colorToLAB(swatch.getRgb(), vibrantLab);
			distances.put(ColorUtils.distanceEuclidean(dominantLab, vibrantLab), vibrantLab);
		}
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		model = ViewModelProviders.of(requireActivity()).get(DetailViewModel.class);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// Avoid a postponeEnterTransition on orientation change, and postpone only of first creation.
		if (savedInstanceState == null) {
			postponeEnterTransition();
		}

		dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_book_detail, container, false);
		mRootView = dataBinding.getRoot();

		Toolbar toolbar = mRootView.findViewById(R.id.detail_toolbar);
		AppCompatActivity appCompatActivity = (AppCompatActivity) requireActivity();
		appCompatActivity.setSupportActionBar(toolbar);

		// Show the Up button in the action bar.
		ActionBar actionBar = appCompatActivity.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		String isbn = getArguments().getString("isbn");

		model.setIsbn(isbn);

		model.getBook().observe(this, new Observer<Book>() {
			@Override
			public void onChanged(@Nullable Book book) {

				mBook = book;

				prepareSharedElementTransition();

				dataBinding.setBook(book);

				ImageView detailCover = dataBinding.detailCover;

				Object loadArg = TextUtils.isEmpty(book.thumbnailUrl) ? requireContext().getFileStreamPath(book.getThumbnailFile()) : book.thumbnailUrl;

				GlideApp.with(BookDetailFragment.this)
						.asDrawable()
						.load(loadArg)
						.into(new DrawableImageViewTarget(detailCover) {

							@Override
							protected void setResource(@Nullable Drawable resource) {
								RippleDrawable rippledImage = new RippleDrawable(ColorStateList.valueOf(requireContext().getColor(R.color.colorPrimaryDark)), resource, null);
								super.setResource(rippledImage);
							}
						});

				detailCover.setOnClickListener(v -> {
					CoverFragment fragment = new CoverFragment();

					requireFragmentManager()
							.beginTransaction()
							.setReorderingAllowed(true)
							.addSharedElement(mRootView.findViewById(R.id.toolbar_cover), "cover")
							.addToBackStack(CoverFragment.class.getSimpleName())
							.replace(R.id.book_detail_container, fragment)
							.commit();
				});

				// fixme via data binding
				TextView publishedDate = mRootView.findViewById(R.id.detail_publication_date);
				try {
					LocalDate localDate = LocalDate.parse(book.publishedDate, DateTimeFormatter.ISO_DATE);
					publishedDate.setText("Publié le " + localDate.format(LONG_DATE_FORMATTER));
				} catch (DateTimeParseException e) {
					e.printStackTrace(); // fixme
					publishedDate.setText("Publié le " + book.publishedDate);
				}

				boolean isSearch = getArguments().getBoolean(BookDetailActivity.ARG_IS_SEARCH, false);

				if (isSearch) {
					handleFabActions(book.alreadySaved);
				} else {
					handleFabActions(true);
				}
			}
		});

		mRootView.findViewById(R.id.fab_delete).setOnClickListener(view -> deleteBook());

		return mRootView;
	}

	public void prepareSharedElementTransition() {

		Transition transition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.image_shared_element_transition);
		transition.setDuration(375);
		setSharedElementEnterTransition(transition);

		Object loadArg = TextUtils.isEmpty(mBook.coverUrl) ? requireActivity().getFileStreamPath(mBook.getCoverFile()) : mBook.coverUrl;

		ImageView toolbarCover = mRootView.findViewById(R.id.toolbar_cover);
		toolbarCover.setScaleType(ImageView.ScaleType.MATRIX);

		GlideApp.with(this)
				.asDrawable()
				.load(loadArg)
				.diskCacheStrategy(DiskCacheStrategy.DATA)
				.into(new DrawableImageViewTarget(toolbarCover) {

					@Override
					public void onLoadFailed(@Nullable Drawable errorDrawable) {
						startPostponedEnterTransition();

						super.onLoadFailed(errorDrawable);
					}

					@Override
					public void onResourceReady(@NonNull Drawable resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Drawable> transition) {
						setTopColors(resource);

						final float imageWidth = resource.getIntrinsicWidth();
						final int screenWidth = getResources().getDisplayMetrics().widthPixels;
						final float scaleRatio = screenWidth / imageWidth;

						final Matrix matrix = toolbarCover.getImageMatrix();
						matrix.postScale(scaleRatio, scaleRatio);

						toolbarCover.setImageMatrix(matrix);

						startPostponedEnterTransition();

						super.onResourceReady(resource, transition);
					}
				});
	}

	private void setTopColors(@NonNull Drawable drawable) {
		CollapsingToolbarLayout collapseToolbar = mRootView.findViewById(R.id.toolbar_layout);
		if (collapseToolbar != null) {

			AppBarLayout appBarLayout = mRootView.findViewById(R.id.app_bar);
			appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
				@Override
				public void onStateChanged(AppBarLayout appBarLayout, @State int state) {
					collapseToolbar.setTitleEnabled(state == AppBarStateChangeListener.COLLAPSED);
				}
			});

			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

			Palette.from(bitmap).generate(palette -> {

				Palette.Swatch dominantSwatch = palette.getDominantSwatch();

				if (dominantSwatch != null) {
					topBackgroundColor = dominantSwatch.getRgb();

					if (topBackgroundColor != 0) {
						collapseToolbar.setBackgroundColor(topBackgroundColor);
						collapseToolbar.setContentScrimColor(topBackgroundColor);
						collapseToolbar.setStatusBarScrimColor(topBackgroundColor);
						requireActivity().getWindow().setStatusBarColor(topBackgroundColor);
						requireActivity().getWindow().setNavigationBarColor(topBackgroundColor);
					}

					double[] dominantLab = new double[3];
					ColorUtils.colorToLAB(topBackgroundColor, dominantLab);

					Map<Double, double[]> distances = new HashMap<>();
					addColor(dominantLab, distances, palette.getLightVibrantSwatch());
					addColor(dominantLab, distances, palette.getVibrantSwatch());
					addColor(dominantLab, distances, palette.getLightMutedSwatch());
					addColor(dominantLab, distances, palette.getMutedSwatch());
					addColor(dominantLab, distances, palette.getDarkMutedSwatch());
					addColor(dominantLab, distances, palette.getDarkVibrantSwatch());

					double[] max = distances.get(Collections.max(distances.keySet()));

					int topTextColor = ColorUtils.LABToColor(max[0], max[1], max[2]);
					if (topTextColor != 0) {
						collapseToolbar.setCollapsedTitleTextColor(topTextColor);
					}
				}
			});
		}
	}

	private void handleFabActions(boolean saved) {
		if (saved) {
			FloatingActionButton fabSync = mRootView.findViewById(R.id.fab_sync);
			fabSync.setVisibility(View.VISIBLE);
			fabSync.setOnClickListener(view -> syncBook());
		} else {
			FloatingActionButton fabAdd = mRootView.findViewById(R.id.fab_save);
			fabAdd.setOnClickListener(view -> saveBook());
			fabAdd.setVisibility(View.VISIBLE);
		}
	}

	private void saveBook() {
		if (mBook.alreadySaved) {
			new AlertDialog.Builder(requireActivity())
					.setTitle("Attention")
					.setMessage("Book already saved ! You'll override existing data")
					.setPositiveButton(android.R.string.ok, (dialog, which) -> doSaveBook())
					.setNegativeButton(android.R.string.no, (dialog, which) -> {
					})
					.show();
		} else {
			doSaveBook();
		}
	}

	private void doSaveBook() {
		// save thumbnail
		GlideApp.with(this)
				.downloadOnly()
				.load(mBook.thumbnailUrl)
				.into(new SimpleTarget<File>() {
					@Override
					public void onResourceReady(@NonNull File resource, com.bumptech.glide.request.transition.Transition<? super File> transition) {
						thumbnailSaved = saveFile(resource, mBook.getThumbnailFile());
						onSaveBook();
					}
				});

		// save image
		GlideApp.with(this)
				.downloadOnly()
				.load(mBook.coverUrl)
				.into(new SimpleTarget<File>() {
					@Override
					public void onResourceReady(@NonNull File resource, com.bumptech.glide.request.transition.Transition<? super File> transition) {
						coverSaved = saveFile(resource, mBook.getCoverFile());
						onSaveBook();
					}
				});
	}

	private void onSaveBook() {
		if (thumbnailSaved && coverSaved) {
			// save book
			BookDao.insert(mBook, this::onBookSaved);

			handleFabActions(true);
		}
	}

	private void onBookSaved() {
		mBookSaved = true;
		Toasty.success(requireContext(), "Book saved", Toast.LENGTH_LONG).show();
	}

	private boolean saveFile(File file, String fileName) {
		try (OutputStream fOut = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE)) {
			Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

			return true;
		} catch (Exception e) {
			e.printStackTrace(); // fixme
			Toasty.error(requireContext(), "Error", Toast.LENGTH_LONG).show();

			return false;
		}
	}

	private void deleteSavedFile(String fileName) {
		boolean deleted = requireContext().deleteFile(fileName);
		if (!deleted) {
			LOGGER.warn("File {} not deleted", fileName);
		}
	}

	private void deleteBook() {
		new AlertDialog.Builder(requireActivity())
				.setTitle("Suppression")
				.setMessage("Vous êtes sur le point de supprimer ce livre de votre librairie !")
				.setPositiveButton("Continuer", (dialog, which) -> onDeleteBook())
				.setNegativeButton("Annuler", (d, w) -> {
				})
				.show();
	}

	private void onDeleteBook() {
		// delete thumbnail
		deleteSavedFile(mBook.getThumbnailFile());
		// delete cover
		deleteSavedFile(mBook.getCoverFile());

		// delete book
		BookDao.delete(mBook, this::onBookDeleted);
	}

	private void onBookDeleted() {
		Toasty.success(requireContext(), "Book deleted", Toast.LENGTH_LONG).show();

		Intent data = new Intent();
		data.putExtra("isbn", mBook.isbn);
		requireActivity().setResult(2, data);

		requireActivity().finish();
	}

	private void syncBook() {
		BookPreview preview = new BookPreview();
		preview.isbn = mBook.isbn;
		preview.title = mBook.title;
		preview.author = mBook.author;
		preview.thumbnailUrl = mBook.thumbnailUrl;
		preview.alreadySaved = true;

		GoogleRestHelper.searchDetails(requireContext(), preview, book -> {
//			mSpinner.setVisibility(View.GONE); fixme

			if (book != null) {
				mBook = book;
				dataBinding.setBook(mBook);

				FloatingActionButton fabAdd = mRootView.findViewById(R.id.fab_save);
				fabAdd.setOnClickListener(view -> saveBook());
				fabAdd.setVisibility(View.VISIBLE);
			} else {
				// fixme
			}
		});
	}
}

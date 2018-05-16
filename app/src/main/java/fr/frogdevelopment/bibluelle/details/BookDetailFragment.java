package fr.frogdevelopment.bibluelle.details;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
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
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.format.FormatStyle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.frogdevelopment.bibluelle.AppBarStateChangeListener;
import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.cover.CoverActivity;
import fr.frogdevelopment.bibluelle.data.entities.Book;
import fr.frogdevelopment.bibluelle.databinding.BookDetailBinding;

public class BookDetailFragment extends Fragment {

    private static final DateTimeFormatter LONG_DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);

    private Book mBook;
    private int topBackgroundColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBook = (Book) getArguments().getSerializable(BookDetailActivity.ARG_KEY);
    }

    private static void addColor(double[] dominantLab, Map<Double, double[]> distances, Palette.Swatch swatch) {
        if (swatch != null) {
            double[] vibrantLab = new double[3];
            ColorUtils.colorToLAB(swatch.getRgb(), vibrantLab);
            distances.put(ColorUtils.distanceEuclidean(dominantLab, vibrantLab), vibrantLab);
        }
    }

    public void prepareSharedElementTransition() {

        Transition transition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.image_shared_element_transition);
        transition.setDuration(375);
        setSharedElementEnterTransition(transition);

        Object loadArg = TextUtils.isEmpty(mBook.coverUrl) ? requireActivity().getFileStreamPath(mBook.getCoverFile()) : mBook.coverUrl;

        ImageView toolbarCover = requireActivity().findViewById(R.id.toolbar_cover);
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

                        startPostponedEnterTransition();

                        final float imageWidth = resource.getIntrinsicWidth();
                        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
                        final float scaleRatio = screenWidth / imageWidth;

                        final Matrix matrix = toolbarCover.getImageMatrix();
                        matrix.postScale(scaleRatio, scaleRatio);

                        toolbarCover.setImageMatrix(matrix);

                        super.onResourceReady(resource, transition);
                    }
                });
    }

    private void setTopColors(@NonNull Drawable drawable) {
        CollapsingToolbarLayout collapseToolbar = requireActivity().findViewById(R.id.toolbar_layout);
        if (collapseToolbar != null) {

            AppBarLayout appBarLayout = requireActivity().findViewById(R.id.app_bar);
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Avoid a postponeEnterTransition on orientation change, and postpone only of first creation.
        if (savedInstanceState == null) {
            postponeEnterTransition();
        }

        prepareSharedElementTransition();

        BookDetailBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.book_detail, container, false);

        dataBinding.setBook(mBook);

        View rootView = dataBinding.getRoot();

        ImageView detailCover = dataBinding.detailCover;

        Object loadArg = TextUtils.isEmpty(mBook.thumbnailUrl) ? requireContext().getFileStreamPath(mBook.getThumbnailFile()) : mBook.thumbnailUrl;

        GlideApp.with(this)
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
            View coverView = requireActivity().findViewById(R.id.toolbar_cover);

            Intent intent = new Intent(requireContext(), CoverActivity.class);
            intent.putExtra(CoverActivity.ARG_BOOK, mBook);
            intent.putExtra("topBackgroundColor", topBackgroundColor);

            Fade fade = new Fade();
            fade.excludeTarget(coverView, true);

            setEnterTransition(fade);
            setExitTransition(fade);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), coverView, "cover");
            startActivity(intent, options.toBundle());
        });

        // fixme via data binding
        TextView publishedDate = rootView.findViewById(R.id.detail_publication_date);
        try {
            LocalDate localDate = LocalDate.parse(mBook.publishedDate, DateTimeFormatter.ISO_DATE);
            publishedDate.setText("Publié le " + localDate.format(LONG_DATE_FORMATTER));
        } catch (DateTimeParseException e) {
            e.printStackTrace(); // fixme
            publishedDate.setText("Publié le " + mBook.publishedDate);
        }

        return rootView;
    }
}

package fr.frogdevelopment.bibluelle.search;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.zxing.Result;

import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;
import fr.frogdevelopment.bibluelle.details.BookDetailActivity;
import fr.frogdevelopment.bibluelle.details.BookDetailFragment;
import fr.frogdevelopment.bibluelle.search.rest.google.GoogleRestHelper;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private static final int ZXING_CAMERA_PERMISSION = 1;
    private ZXingScannerView mScannerView;
    private SpinKitView mSpinKitView;

    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ZXING_CAMERA_PERMISSION && (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(requireContext(), "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mScannerView = new ZXingScannerView(requireContext());

        RelativeLayout relativeLayout = new RelativeLayout(requireContext());
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mScannerView.addView(relativeLayout);

        mSpinKitView = new SpinKitView(requireContext(), null, com.github.ybq.android.spinkit.R.style.SpinKitView_CubeGrid);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mSpinKitView.setLayoutParams(layoutParams);
        mSpinKitView.setColor(getResources().getColor(R.color.colorAccent, requireContext().getTheme()));
        relativeLayout.addView(mSpinKitView);
        mSpinKitView.setVisibility(View.INVISIBLE);

        return mScannerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();

        mSpinKitView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void handleResult(Result rawResult) {
        String isbn = rawResult.getText();

        mSpinKitView.setVisibility(View.VISIBLE);

        GoogleRestHelper.searchBook(requireActivity(), isbn, book -> {

            if (book != null) {

                DatabaseCreator.getInstance().getBookDao().isPresent(isbn).observe(this, isPresent -> {
                    book.alreadySaved = isPresent != null ? isPresent : false; // fixme
                    Intent intent = new Intent(requireActivity(), BookDetailActivity.class);
                    intent.putExtra(BookDetailFragment.ARG_KEY, book);
                    intent.putExtra("IS_SEARCH", true);
                    startActivity(intent);
                });
            } else {
                // fixme
                mSpinKitView.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

}

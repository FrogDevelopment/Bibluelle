package fr.frogdevelopment.bibluelle.search.scan;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.zxing.Result;

import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;
import fr.frogdevelopment.bibluelle.details.BookDetailActivity;
import fr.frogdevelopment.bibluelle.search.rest.google.GoogleRestHelper;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private static final int ZXING_CAMERA_PERMISSION = 1;
    private ZXingScannerView mScannerView;
    private SpinKitView mSpinKitView;
    private TextView mMsgView;

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
        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);

        mScannerView = rootView.findViewById(R.id.scan_zxing);
        mSpinKitView = rootView.findViewById(R.id.scan_spinner);

        SharedPreferences preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);

        boolean autoFocus = preferences.getBoolean("AUTO_FOCUS", true);

        mScannerView.setAutoFocus(autoFocus);

        Switch focusSwitch = rootView.findViewById(R.id.scan_switch_focus);
        focusSwitch.setChecked(autoFocus);
        focusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mScannerView.setAutoFocus(isChecked);
            preferences.edit()
                    .putBoolean("AUTO_FOCUS", isChecked)
                    .apply();
        });

        mMsgView = rootView.findViewById(R.id.scan_msg_info);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();

        mSpinKitView.setVisibility(View.INVISIBLE);
        mMsgView.setText(R.string.scan_msg_help);
        mMsgView.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
    }

    @Override
    public void handleResult(Result rawResult) {
        String isbn = rawResult.getText();

        mSpinKitView.setVisibility(View.VISIBLE);

        GoogleRestHelper.searchBook(requireContext(), isbn, book -> {

            if (book != null) {
                mMsgView.setText(null);

                LiveData<Boolean> liveData = DatabaseCreator.getInstance().getBookDao().isPresent(isbn);
                liveData.observe(this, isPresent -> {

                    liveData.removeObservers(ScanFragment.this);

                    book.alreadySaved = isPresent != null ? isPresent : false; // fixme

                    Intent intent = new Intent(requireContext(), BookDetailActivity.class);
                    intent.putExtra(BookDetailActivity.ARG_KEY, book);
                    intent.putExtra(BookDetailActivity.ARG_IS_SEARCH, true);
                    startActivity(intent);
                });
            } else {
                // fixme
                mSpinKitView.setVisibility(View.INVISIBLE);
                mScannerView.resumeCameraPreview(this);
                mMsgView.setText(getString(R.string.scan_msg_no_data, isbn));
                mMsgView.setTextColor(getResources().getColor(android.R.color.holo_red_dark, requireContext().getTheme()));
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

}

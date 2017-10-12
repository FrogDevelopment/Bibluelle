package fr.frogdevelopment.bibluelle;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanFragment extends Fragment implements ZXingScannerView.ResultHandler {

	private static final int ZXING_CAMERA_PERMISSION = 1;
	private ZXingScannerView mScannerView;

	public ScanFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == ZXING_CAMERA_PERMISSION && (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
			Toast.makeText(getActivity(), "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_scan, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mScannerView = new ZXingScannerView(getActivity());

		FrameLayout scanFrame = view.findViewById(R.id.scan_frame);
		scanFrame.addView(mScannerView);
	}

	@Override
	public void onResume() {
		super.onResume();
		mScannerView.setResultHandler(this);
		mScannerView.startCamera();
	}

	@Override
	public void handleResult(Result rawResult) {
		Toast.makeText(getActivity(), "Contents = " + rawResult.getText() + ", Format = " + rawResult.getBarcodeFormat().toString(), Toast.LENGTH_SHORT).show();
		// Note:
		// * Wait 2 seconds to resume the preview.
		// * On older devices continuously stopping and resuming camera preview can result in freezing the app.
		// * I don't know why this is the case but I don't have the time to figure out.
		Handler handler = new Handler();
		handler.postDelayed(() -> mScannerView.resumeCameraPreview(ScanFragment.this), 2000);

//		https://www.googleapis.com/books/v1/volumes?q=isbn:9780134092669
	}

	@Override
	public void onPause() {
		super.onPause();
		mScannerView.stopCamera();
	}

}

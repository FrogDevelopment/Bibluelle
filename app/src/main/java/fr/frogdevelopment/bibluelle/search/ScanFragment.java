package fr.frogdevelopment.bibluelle.search;

import android.Manifest;
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

		return mScannerView;
	}

	@Override
	public void onResume() {
		super.onResume();
		mScannerView.setResultHandler(this);
		mScannerView.startCamera();
	}

	@Override
	public void handleResult(Result rawResult) {
		SearchFragment fragment = (SearchFragment) requireFragmentManager().findFragmentByTag("SEARCH_FRAGMENT");
		fragment.setIsbn(rawResult.getText());

		requireFragmentManager().popBackStack();
	}

	@Override
	public void onPause() {
		super.onPause();
		mScannerView.stopCamera();
	}

}

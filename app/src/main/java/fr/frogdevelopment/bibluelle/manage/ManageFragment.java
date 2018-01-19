package fr.frogdevelopment.bibluelle.manage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Task;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;

import es.dmoral.toasty.Toasty;
import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;

public class ManageFragment extends Fragment implements View.OnClickListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManageFragment.class);

	private static final int REQUEST_CODE_SIGN_IN = 0;
	public static final String FILE_NAME = "isbn-saved.txt";

	private GoogleSignInClient mGoogleSignInClient;
	private DriveResourceClient mDriveResourceClient;
	private SignInButton mSignInButton;
	private ImageView mPhoto;
	private TextView mName;
	private TextView mEmail;
	private View mConnectedView;
	private DriveContents driveContents;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Build a Google SignIn client.
		GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestScopes(Drive.SCOPE_APPFOLDER)
				.build();
		mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), signInOptions);

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_manage, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mConnectedView = view.findViewById(R.id.manager_connected);

		mSignInButton = view.findViewById(R.id.sign_in_button);
		mSignInButton.setSize(SignInButton.SIZE_WIDE);
		mSignInButton.setOnClickListener(this);

		mPhoto = view.findViewById(R.id.profile_photo);
		mName = view.findViewById(R.id.profile_name);
		mEmail = view.findViewById(R.id.profile_email);

		view.findViewById(R.id.sign_out_button).setOnClickListener(this);
		view.findViewById(R.id.disconnect_button).setOnClickListener(this);
		view.findViewById(R.id.save_button).setOnClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		LOGGER.info("Start sign in");

		// Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.
		GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
		updateUI(lastSignedInAccount);
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_SIGN_IN) {
			Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
			try {
				// Signed in successfully, show authenticated UI.
				GoogleSignInAccount account = task.getResult(ApiException.class);

				LOGGER.info("Signed in successfully.");
				updateUI(account);
			} catch (ApiException e) {
				// The ApiException status code indicates the detailed failure reason.
				// Please refer to the GoogleSignInStatusCodes class reference for more information.
				LOGGER.error("signInResult:failed code=" + e.getStatusCode(), e);
				updateUI(null);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.sign_in_button:
				startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
				break;

			case R.id.sign_out_button:
				mGoogleSignInClient.signOut().addOnCompleteListener(getActivity(), task -> updateUI(null));
				break;

			case R.id.disconnect_button:
				mGoogleSignInClient.revokeAccess().addOnCompleteListener(getActivity(), task -> updateUI(null));
				break;

			case R.id.save_button:
				saveBooks();
				break;
		}
	}

	private void updateUI(@Nullable GoogleSignInAccount account) {
		if (account != null) {
			mSignInButton.setVisibility(View.GONE);
			mConnectedView.setVisibility(View.VISIBLE);

			mDriveResourceClient = Drive.getDriveResourceClient(getActivity(), account);

			GlideApp.with(this)
					.asBitmap()
					.load(account.getPhotoUrl())
					.into(mPhoto);

			mName.setText(account.getDisplayName());
			mEmail.setText(account.getEmail());
		} else {
			mSignInButton.setVisibility(View.VISIBLE);
			mConnectedView.setVisibility(View.GONE);
		}
	}

	private static final CustomPropertyKey versionPropertyKey = new CustomPropertyKey("version", CustomPropertyKey.PUBLIC);

	private void saveBooks() {
		DatabaseCreator.getInstance().getBookDao().getAllIsbn().observe(this, list -> {
//			view.findViewById(R.id.spinner).setVisibility(View.GONE);

			if (list != null) {

				final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
				appFolderTask
						.continueWithTask(task -> {
							DriveFolder appFolderResult = task.getResult();
							Query query = new Query.Builder()
									.addFilter(Filters.eq(SearchableField.TITLE, FILE_NAME))
									.build();

							return mDriveResourceClient.queryChildren(appFolderResult, query);
						})
						.continueWithTask(task -> {
							MetadataBuffer queryResult = task.getResult();
							if (queryResult.getCount() > 0) {
								for (Metadata metadata : queryResult) {
									if (FILE_NAME.equals(metadata.getTitle())) { // already exist => use it
//										// todo check last modification time
//										Map<CustomPropertyKey, String> customProperties = metadata.getCustomProperties();
//										String version = customProperties.get(versionPropertyKey);

										DriveFile driveFile = metadata.getDriveId().asDriveFile();

										return mDriveResourceClient.openFile(driveFile, DriveFile.MODE_WRITE_ONLY);
									}
								}
							}

							// create a new file
							return mDriveResourceClient.createContents();
						})
						.continueWithTask(task -> {
							DriveFolder parent = appFolderTask.getResult();

							driveContents = task.getResult();
							OutputStream outputStream = driveContents.getOutputStream();

							try (Writer writer = new OutputStreamWriter(outputStream)) {
								for (String isbn : list) {
									writer.write(isbn + "\n");
								}
							}

							MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
									.setTitle(FILE_NAME)
									.setDescription("Isbn list of books")
									.setMimeType("text/plain")
									.setStarred(true)
									.setLastViewedByMeDate(new Date())
									.setCustomProperty(versionPropertyKey, "1") // fixme dynamic version for update sync
									.build();

							return mDriveResourceClient.createFile(parent, changeSet, driveContents);
						})
						.continueWithTask(task -> mDriveResourceClient.commitContents(driveContents, null))
						.addOnSuccessListener(Void -> Toasty.success(getActivity(), "Data saved").show())
						.addOnFailureListener(e -> {
							LOGGER.error("Unable to create file", e);
							Toasty.error(getActivity(), "error : " + ExceptionUtils.getMessage(e)).show();
						})
				;
			}
		});
	}
}

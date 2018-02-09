package fr.frogdevelopment.bibluelle.manage;

import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;

public class ManageFragment extends Fragment implements View.OnClickListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManageFragment.class);

	private static final int REQUEST_CODE_SIGN_IN = 0;
	private static final String FILE_NAME = "isbn-saved.txt";
	private static final String SYNC_VERSION = "sync_version";

	private GoogleSignInClient mGoogleSignInClient;
	private DriveResourceClient mDriveResourceClient;
	private SignInButton mSignInButton;
	private ImageView mPhoto;
	private TextView mName;
	private TextView mEmail;
	private View mConnectedView;
	private SharedPreferences preferences;

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
		view.findViewById(R.id.sync_button).setOnClickListener(this);
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
				GoogleSignInAccount account = task.getResult(ApiException.class);
				updateUI(account);
			} catch (ApiException e) {
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
				saveData();
				break;

			case R.id.sync_button:
				syncData();
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

			preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
		} else {
			mSignInButton.setVisibility(View.VISIBLE);
			mConnectedView.setVisibility(View.GONE);
		}
	}

	public static final CustomPropertyKey VERSION_PROPERTY_KEY = new CustomPropertyKey("version", CustomPropertyKey.PUBLIC);

	private DriveContents contents;

	private void saveData() {
		LiveData<List<String>> allIsbn = DatabaseCreator.getInstance().getBookDao().getAllIsbn();

		allIsbn.observe(this, data -> {
//			view.findViewById(R.id.spinner).setVisibility(View.GONE); todo
			allIsbn.removeObservers(ManageFragment.this);

			if (data != null) {
				int new_version = preferences.getInt(SYNC_VERSION, 0) + 1;

				final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
				final Task<DriveContents> driveContentsTask = mDriveResourceClient.createContents();
				Tasks.whenAll(appFolderTask, driveContentsTask)
						.continueWithTask(task -> {
							DriveFolder parent = appFolderTask.getResult();
							contents = driveContentsTask.getResult();

							try (OutputStream outputStream = contents.getOutputStream();
							     Writer writer = new OutputStreamWriter(outputStream)) {
								for (String isbn : data) {
									writer.write(isbn + "\n");
								}
							}

							SharedPreferences.Editor edit = preferences.edit();
							edit.putInt(SYNC_VERSION, new_version);
							edit.apply();

							MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
									.setTitle(FILE_NAME)
									.setDescription("Isbn list of books")
									.setMimeType("text/plain")
									.setStarred(true)
									.setLastViewedByMeDate(new Date())
									.setCustomProperty(VERSION_PROPERTY_KEY, String.valueOf(new_version))
									.build();

							return mDriveResourceClient.createFile(parent, changeSet, contents);
						})
						.addOnSuccessListener(driveFile -> Toasty.success(getActivity(), "Data saved with version " + new_version).show())
						.addOnFailureListener(e -> {
							LOGGER.error("Unable to create file", e);
							Toasty.error(getActivity(), "error : " + ExceptionUtils.getMessage(e)).show();
							// fixme decrement new_version if error on saving ?
						})
				;
			}
		});
	}

	private void syncData() {
		int sync_version = preferences.getInt(SYNC_VERSION, 0);

		mDriveResourceClient.getAppFolder()
				.continueWithTask(task -> {
					DriveFolder appFolderResult = task.getResult();
					Query query = new Query.Builder()
							.addFilter(Filters.eq(SearchableField.TITLE, FILE_NAME))
							.build();

					return mDriveResourceClient.queryChildren(appFolderResult, query);
				})
				.addOnSuccessListener(metadataBuffer -> {
					if (metadataBuffer.getCount() > 0) {
						for (Metadata metadata : metadataBuffer) {
							if (FILE_NAME.equals(metadata.getTitle())) {
								Map<CustomPropertyKey, String> customProperties = metadata.getCustomProperties();
								String version = customProperties.getOrDefault(VERSION_PROPERTY_KEY, "0");

								Integer driveVersion = Integer.valueOf(version);

								if (driveVersion > sync_version) {
									new AlertDialog.Builder(getActivity())
											.setTitle("INFORMATION")
											.setMessage("Une version plus récente de votre librairie existe, la mettre à jour?")
											.setPositiveButton(android.R.string.yes, (dialog, which) -> {
												Toasty.info(getActivity(), "Incoming").show();
												// todo
											})
											.setNegativeButton(android.R.string.no, null)
											.setCancelable(false)
											.show();
								} else {
									new AlertDialog.Builder(getActivity())
											.setTitle("INFORMATION")
											.setMessage("Vous avez la version la plus récente de votre librairie.")
											.setPositiveButton(android.R.string.ok, null)
											.setCancelable(false)
											.show();
								}

								return;
							}
						}
					}

					new AlertDialog.Builder(getActivity())
							.setTitle("INFORMATION")
							.setMessage("Vous n'avez aucune données sauvegardées !")
							.setPositiveButton(android.R.string.ok, null)
							.setCancelable(false)
							.show();
				})
				.addOnFailureListener(e -> {
					LOGGER.error("Error while checking sync data", e);
					Toasty.error(getActivity(), "Error while checking sync data").show();
				})
		;
	}

}
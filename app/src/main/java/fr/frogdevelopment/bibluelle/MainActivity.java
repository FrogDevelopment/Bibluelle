package fr.frogdevelopment.bibluelle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.Map;

import es.dmoral.toasty.Toasty;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;
import fr.frogdevelopment.bibluelle.gallery.GalleryFragment;
import fr.frogdevelopment.bibluelle.manage.ManageFragment;
import fr.frogdevelopment.bibluelle.search.SearchFragment;

public class MainActivity extends AppCompatActivity {

	private final SparseArray<Pair<Fragment, String>> fragments = new SparseArray<>();

	private boolean doubleBackToExitPressedOnce = false;

	private BottomNavigationView mNavigationView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationView = findViewById(R.id.navigation);
		mNavigationView.setOnNavigationItemSelectedListener(item -> switchFragment(item.getItemId(), false));

		SpinKitView spinner = findViewById(R.id.spinner);

		buildFragmentsList();

		AndroidThreeTen.init(this);

		DatabaseCreator databaseCreator = DatabaseCreator.getInstance();

		// listen fro database created
		databaseCreator.isDatabaseCreated().observe(this, aBoolean -> {
			spinner.setVisibility(View.GONE);

			if (Boolean.TRUE.equals(aBoolean)) {
				databaseCreator.isDatabaseCreated().removeObservers(MainActivity.this);
				// when created => display main view
				switchFragment(R.id.navigation_dashboard, true);
//			} else {
				// fixme
			}
		});

		databaseCreator.createDb(this.getApplication());

		checkAccount();
	}

	private void buildFragmentsList() {
		fragments.put(R.id.navigation_dashboard, Pair.create(new GalleryFragment(), "DASHBOARD_FRAGMENT"));
		fragments.put(R.id.nav_search, Pair.create(new SearchFragment(), "SEARCH_FRAGMENT"));
		fragments.put(R.id.nav_manage, Pair.create(new ManageFragment(), "MANAGE_FRAGMENT"));
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.doubleBackToExitPressedOnce = false;
	}

	@Override
	public void onBackPressed() {
		if (mNavigationView.getSelectedItemId() == R.id.navigation_dashboard) {
			if (doubleBackToExitPressedOnce) {
				super.onBackPressed();
			} else {
				this.doubleBackToExitPressedOnce = true;
				Toast.makeText(this, "R.string.exit_press_back_twice_message", Toast.LENGTH_SHORT).show();
			}
		} else {
			this.doubleBackToExitPressedOnce = false;
			mNavigationView.setSelectedItemId(R.id.navigation_dashboard);
		}
	}

	private boolean switchFragment(int itemId, boolean force) {
		if (!force && mNavigationView.getSelectedItemId() == itemId) {
			return false;
		}

		Pair<Fragment, String> pair = fragments.get(itemId);
		if (pair.first != null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.content_frame, pair.first, pair.second)
					.commit();

			return true;
		} else {
			return false;
		}
	}


	private void checkAccount() {
		// Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.
		GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
		if (account != null) {
			Toast.makeText(this, "Welcome back " + account.getGivenName(), Toast.LENGTH_SHORT).show();
			SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
			int sync_version = preferences.getInt("sync_version", 0);

			final DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(this, account);

			driveResourceClient.getAppFolder()
					.continueWithTask(task -> {
						DriveFolder appFolderResult = task.getResult();
						Query query = new Query.Builder()
								.addFilter(Filters.eq(SearchableField.TITLE, ManageFragment.FILE_NAME))
								.build();

						return driveResourceClient.queryChildren(appFolderResult, query);
					})
					.addOnSuccessListener(metadataBuffer -> {
						if (metadataBuffer.getCount() > 0) {
							for (Metadata metadata : metadataBuffer) {
								if (ManageFragment.FILE_NAME.equals(metadata.getTitle())) {
									Map<CustomPropertyKey, String> customProperties = metadata.getCustomProperties();
									String version = customProperties.getOrDefault(ManageFragment.VERSION_PROPERTY_KEY, "0");

									Integer driveVersion = Integer.valueOf(version);

									if (driveVersion > sync_version) {
										askUpdateData(metadata.getDriveId().asDriveFile());
									}

									return;
								}
							}
						}
					})
					.addOnFailureListener(e -> {
//							LOG fixme
						Toasty.error(this, "Error while checking sync data").show();
					})
			;
		}
	}

	private void askUpdateData(DriveFile driveFile) {
		new AlertDialog.Builder(this)
				.setTitle("INFORMATION")
				.setMessage("Une version plus récente de votre librairie existe, la mettre à jour?")
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toasty.info(getBaseContext(), "Yeah").show();
					}
				})
				.setNegativeButton(android.R.string.no, null)
				.show();
	}
}

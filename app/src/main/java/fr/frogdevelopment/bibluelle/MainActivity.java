package fr.frogdevelopment.bibluelle;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.util.SparseArray;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;
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

		buildFragmentsList();

		switchFragment(R.id.navigation_dashboard, true);
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
				Toasty.info(this, getString(R.string.exit_press_back_twice_message), Toast.LENGTH_SHORT).show();
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
			getSupportFragmentManager()
					.beginTransaction()
					.setReorderingAllowed(true)
					.replace(R.id.content_frame, pair.first, pair.second)
					.commit();

			return true;
		} else {
			return false;
		}
	}
}

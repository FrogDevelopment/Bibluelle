package fr.frogdevelopment.bibluelle;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;

public class Main2Activity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);

		BottomNavigationView navigation = findViewById(R.id.navigation);
		navigation.setOnNavigationItemSelectedListener(item -> {
			switch (item.getItemId()) {
				case R.id.nav_search:
					Fragment fragment = new SearchFragment();
					String tag = "SEARCH_FRAGMENT";
					getFragmentManager().beginTransaction()
							.replace(R.id.content_frame, fragment, tag)
							.addToBackStack(null)
							.commit();

					return true;
				case R.id.navigation_dashboard:
					return true;
				case R.id.nav_manage:
					return true;
			}
			return false;
		});
	}

}

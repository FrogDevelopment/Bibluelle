package fr.frogdevelopment.bibluelle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.jakewharton.threetenabp.AndroidThreeTen;

import fr.frogdevelopment.bibluelle.data.DatabaseCreator;

public class SplashScreenActivity extends AppCompatActivity {

	// Splash screen timer
	private static final long SPLASH_TIME_OUT = 500;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash_screen);

		AndroidThreeTen.init(this);

		DatabaseCreator databaseCreator = DatabaseCreator.getInstance();

		// listen fro database created
		databaseCreator.isDatabaseCreated().observe(this, aBoolean -> {

			if (Boolean.TRUE.equals(aBoolean)) {
				databaseCreator.isDatabaseCreated().removeObservers(SplashScreenActivity.this);

				// This method will be executed once the timer is over
				new Handler().postDelayed(() -> {
					// Start your app main activity
					Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
					SplashScreenActivity.this.startActivity(i);

					// close this activity
					SplashScreenActivity.this.finish();
				}, SPLASH_TIME_OUT);

//			} else {
				// fixme
			}
		});

		databaseCreator.createDb(this.getApplication());
	}

}

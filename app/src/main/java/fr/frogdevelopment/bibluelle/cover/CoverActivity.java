package fr.frogdevelopment.bibluelle.cover;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class CoverActivity extends AppCompatActivity {

    public static final String ARG_BOOK = "BOOK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setId(View.generateViewId());

        setContentView(frameLayout, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // https://stackoverflow.com/questions/26600263/how-do-i-prevent-the-status-bar-and-navigation-bar-from-animating-during-an-acti
        Fade fade = new Fade();
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putAll(getIntent().getExtras());
            CoverFragment fragment = new CoverFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(frameLayout.getId(), fragment)
                    .commit();
        }
    }

}

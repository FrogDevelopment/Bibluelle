package fr.frogdevelopment.bibluelle;

import android.support.annotation.IntDef;
import android.support.design.widget.AppBarLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

	public static final int EXPANDED = 0;
	public static final int COLLAPSED = 1;
	public static final int IDLE = 2;

	@IntDef({EXPANDED, COLLAPSED, IDLE})
	@Retention(RetentionPolicy.SOURCE)
	public @interface State {
	}

	private int mCurrentState = IDLE;

	@Override
	public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
		if (i == 0) {
			if (mCurrentState != EXPANDED) {
				onStateChanged(appBarLayout, EXPANDED);
			}
			mCurrentState = EXPANDED;
		} else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
			if (mCurrentState != COLLAPSED) {
				onStateChanged(appBarLayout, COLLAPSED);
			}
			mCurrentState = COLLAPSED;
		} else {
			if (mCurrentState != IDLE) {
				onStateChanged(appBarLayout, IDLE);
			}
			mCurrentState = IDLE;
		}
	}


	public abstract void onStateChanged(AppBarLayout appBarLayout, @State int state);
}

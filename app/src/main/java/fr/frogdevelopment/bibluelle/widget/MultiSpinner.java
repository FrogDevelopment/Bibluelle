package fr.frogdevelopment.bibluelle.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;

import fr.frogdevelopment.bibluelle.R;

public class MultiSpinner extends AppCompatSpinner {

	private CharSequence hint = "";
	private CharSequence[] entries;
	private boolean[] selected;
	private MultiSpinnerListener listener;

	public MultiSpinner(Context context) {
		this(context, null);
	}

	public MultiSpinner(Context context, int mode) {
		this(context, null, android.support.v7.appcompat.R.attr.spinnerStyle, mode);
	}

	public MultiSpinner(Context context, AttributeSet attrs) {
		this(context, attrs, android.support.v7.appcompat.R.attr.spinnerStyle);
	}

	public MultiSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, -1);
	}

	public MultiSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
		this(context, attrs, defStyleAttr, mode, null);
	}

	public MultiSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode, Resources.Theme popupTheme) {
		super(context, attrs, defStyleAttr, mode, popupTheme);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiSpinner);
		entries = a.getTextArray(R.styleable.MultiSpinner_android_entries);
		if (entries != null) {
			selected = new boolean[entries.length]; // false-filled by default
		}

		hint = a.getText(R.styleable.MultiSpinner_android_hint);

		a.recycle();
	}

	@Override
	@SuppressLint("ClickableViewAccessibility")
	public boolean performClick() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(hint);
		builder.setMultiChoiceItems(entries, selected, (dialog, which, isChecked) -> selected[which] = isChecked);
		builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
			// build new spinner text & delimiter management
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < entries.length; i++) {
				if (selected[i]) {
					stringBuilder.append(entries[i]);
					stringBuilder.append(", ");
				}
			}

			// Remove trailing comma
			if (stringBuilder.length() > 2) {
				stringBuilder.setLength(stringBuilder.length() - 2);
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{stringBuilder.toString()});
			setAdapter(adapter);
			if (listener != null) {
				listener.onItemsSelected(selected);
			}
		});

		builder.setCancelable(false);
		builder.show();
		return true;
	}

	public void setMultiSpinnerListener(MultiSpinnerListener listener) {
		this.listener = listener;
	}

	public interface MultiSpinnerListener {
		void onItemsSelected(boolean[] selected);
	}
}

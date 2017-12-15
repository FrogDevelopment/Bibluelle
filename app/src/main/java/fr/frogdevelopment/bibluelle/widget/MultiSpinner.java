package fr.frogdevelopment.bibluelle.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;

import org.apache.commons.lang3.ArrayUtils;

import fr.frogdevelopment.bibluelle.R;

public class MultiSpinner extends AppCompatSpinner {

	private CharSequence prompt = "";
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

		prompt = a.getText(R.styleable.MultiSpinner_android_prompt);

		a.recycle();

		setOnLongClickListener(view -> {
			performClick();
			return true;
		});

		setSelected(prompt);
	}

	@Override
	@SuppressLint("ClickableViewAccessibility")
	public boolean performClick() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(prompt);
		builder.setMultiChoiceItems(entries, selected, (dialog, which, isChecked) -> selected[which] = isChecked);
		builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
			// build new spinner text & delimiter management
			CharSequence[] selectedValues = null;
			for (int i = 0; i < entries.length; i++) {
				if (selected[i]) {
					selectedValues = ArrayUtils.add(selectedValues, entries[i]);
				}
			}

			setSelected(selectedValues);

			if (listener != null) {
				listener.onItemsSelected(selected);
			}
		});

		builder.setCancelable(false);
		builder.show();
		return true;
	}

	public void setSelected(CharSequence[] selected) {
		if (selected == null) {
			setSelected(prompt);
		} else {
			setSelected(TextUtils.join(",", selected));
		}
	}

	public void setSelected(CharSequence selected) {
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{selected.toString()});
		setAdapter(adapter);
	}

	public void setMultiSpinnerListener(MultiSpinnerListener listener) {
		this.listener = listener;
	}

	public interface MultiSpinnerListener {
		void onItemsSelected(boolean[] selected);
	}
}

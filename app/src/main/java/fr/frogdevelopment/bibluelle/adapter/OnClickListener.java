package fr.frogdevelopment.bibluelle.adapter;

import android.view.View;

import fr.frogdevelopment.bibluelle.data.entities.BookPreview;

public interface OnClickListener {
	void onClick(View v, BookPreview preview);
}

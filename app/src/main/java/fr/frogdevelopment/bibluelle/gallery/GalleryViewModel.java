package fr.frogdevelopment.bibluelle.gallery;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import fr.frogdevelopment.bibluelle.data.DatabaseCreator;
import fr.frogdevelopment.bibluelle.data.entities.BookPreview;

public class GalleryViewModel extends ViewModel {

	private LiveData<List<BookPreview>> previews;

	public LiveData<List<BookPreview>> getPreviews() {
		if (previews == null) {
			previews = DatabaseCreator.getInstance().getBookDao().loadAllPreviews();
		}

		return previews;
	}
}

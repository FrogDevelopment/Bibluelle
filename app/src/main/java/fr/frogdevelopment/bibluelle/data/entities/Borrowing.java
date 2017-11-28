package fr.frogdevelopment.bibluelle.data.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.threeten.bp.LocalDate;

import java.io.Serializable;

@Entity(
		tableName = "borrowing",
		foreignKeys = @ForeignKey(
				entity = Book.class,
				parentColumns = "id",
				childColumns = "book_id",
				onDelete = ForeignKey.CASCADE)
)
public class Borrowing implements Serializable {

	private static final long serialVersionUID = 266790165653294668L;

	@PrimaryKey(autoGenerate = true)
	public Integer id;

	@NonNull
	@ColumnInfo(name = "book_id")
	public Integer bookId;

	@NonNull
	@ColumnInfo(name = "borrower_name")
	public String borrowerName;

	@NonNull
	@ColumnInfo(name = "give_date")
	public LocalDate giveDate;

	@Nullable
	@ColumnInfo(name = "return_date")
	public LocalDate returnDate;
}

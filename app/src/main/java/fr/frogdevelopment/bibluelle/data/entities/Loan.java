package fr.frogdevelopment.bibluelle.data.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "loan")
@SuppressWarnings("NullableProblems")
public class Loan implements Serializable {

	@PrimaryKey(autoGenerate = true)
	public Integer id;

	@NonNull
	@ColumnInfo(name = "isbn")
	public String isbn;

	@NonNull
	@ColumnInfo(name = "who")
	public String who;

	@NonNull
	@ColumnInfo(name = "when_out")
	public String when_out;

	@NonNull
	@ColumnInfo(name = "when_in")
	public String when_in;
}

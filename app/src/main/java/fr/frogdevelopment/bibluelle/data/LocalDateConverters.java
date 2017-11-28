package fr.frogdevelopment.bibluelle.data;

import android.arch.persistence.room.TypeConverter;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.LocalDate;

import java.sql.Date;

public class LocalDateConverters {

	@TypeConverter
	public static LocalDate toLocalDate(Long value) {
		return value == null ? null : DateTimeUtils.toLocalDate(new Date(value));
	}

	@TypeConverter
	public static Long toSqlDate(LocalDate localDate) {
		return localDate == null ? null : DateTimeUtils.toSqlDate(localDate).getTime();
	}
}

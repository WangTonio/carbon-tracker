package de.unigoettingen.ct.data;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {
	// 2011/09/23 13:42:01
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private DateUtils() {

	}

	public static String calendarToString(Calendar calendar) {
		return DATE_FORMAT.format(calendar.getTime());
	}

	public static String timestampToString(Timestamp timestamp) {
		return DATE_FORMAT.format(timestamp.getTime());
	}

}

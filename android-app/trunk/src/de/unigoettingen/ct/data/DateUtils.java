package de.unigoettingen.ct.data;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;



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
	
	public static GregorianCalendar parseTimestamp(String timestamp) throws ParseException{
		Date d = DATE_FORMAT.parse(timestamp);
		GregorianCalendar ret = new GregorianCalendar();
		ret.setTime(d);
		return ret;
	}

}

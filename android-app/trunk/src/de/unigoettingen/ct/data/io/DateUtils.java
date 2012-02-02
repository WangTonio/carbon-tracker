package de.unigoettingen.ct.data.io;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Provides static  utility for converting from Calendar objects to Strings and vice versa.
 * The date format used here is also involved in the client-server protocol and thus must not be modified.
 * @author Fabian Sudau
 *
 */
public class DateUtils {
	
	// example date: 2011/09/23 13:42:01
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private DateUtils() {

	}

	public synchronized static String calendarToString(Calendar calendar) {
		return DATE_FORMAT.format(calendar.getTime());
	}

	
	public synchronized static GregorianCalendar parseTimestamp(String timestamp) throws ParseException{
		Date d = DATE_FORMAT.parse(timestamp);
		GregorianCalendar ret = new GregorianCalendar();
		ret.setTime(d);
		return ret;
	}

}

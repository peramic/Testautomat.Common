package havis.test.suite.common.helpers;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

	private static final SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH-mm-ss.SSS");
	private static final Object syncObject = new Object();

	public static String getDateString(Date date) {
		synchronized (syncObject) {
			return formatter.format(date);
		}
	}

}

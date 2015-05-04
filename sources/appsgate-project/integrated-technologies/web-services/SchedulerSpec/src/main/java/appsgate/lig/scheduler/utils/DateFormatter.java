package appsgate.lig.scheduler.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class to ensure that Date Format used in the scheduler will be correct
 * @author thibaud
 *
 */
public class DateFormatter {
	
	public final static SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	/**
	 * Not instanciable (pure helper class)
	 */
	private DateFormatter() {
	}
	
	public static String format(Date date) {
		return dateFormat.format(date);
	}
	
	public static String format(long timeInMillis) {
		return dateFormat.format(new Date(timeInMillis));
	}
	
	public static Date parse(String date) throws ParseException {
		return dateFormat.parse(date);
	}
	
	

}

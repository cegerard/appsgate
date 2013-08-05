package appsgate.lig.calendar.core.command;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Temp {

	public static void main(String[] args) throws ParseException {
		String target = "25052013 14:00";
		DateFormat df = new SimpleDateFormat("ddMMyyyy HH:mm");
		df.setTimeZone(TimeZone.getDefault());
		Date d=df.parse(target);
		
		System.out.println("--"+d);
		
	}
	
}

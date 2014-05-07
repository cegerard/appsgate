package appsgate.lig.weather.utils;

import java.util.Date;

/**
 * Forecast for a given Day (java.util.Date)
 * @author jnascimento
 *
 */
public class DayForecast {

	private Date date;
	private int min;
	private int max;
	private int code;
	
	public DayForecast(Date date,int min,int max, int code){
		this.date=date;
		this.min=min;
		this.max=max;
		this.code=code;
	}

	public Date getDate() {
		return date;
	}


	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}
	
	public int getCode() {
		return code;
	}


	@Override
	public String toString() {
		return String.format("%s , code:%s min:%s max:%s", code, date,min,max);
	}
	
	
	
}

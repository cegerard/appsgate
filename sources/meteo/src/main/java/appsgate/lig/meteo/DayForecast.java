package appsgate.lig.meteo;

import java.util.Date;

/**
 * Forecast for a given moment (java.util.Date) of the day
 * @author jnascimento
 *
 */
public class DayForecast {

	private Date date;
	private Float min;
	private Float max;
	
	public DayForecast(Date date,Float min,Float max){
		this.date=date;
		this.min=min;
		this.max=max;
	}

	public Date getDate() {
		return date;
	}


	public Float getMin() {
		return min;
	}

	public Float getMax() {
		return max;
	}

	@Override
	public String toString() {
		return String.format("%s min:%s max:%s", date,min,max);
	}
	
	
	
}

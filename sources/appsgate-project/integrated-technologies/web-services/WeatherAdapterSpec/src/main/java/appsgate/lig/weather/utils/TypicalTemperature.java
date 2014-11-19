package appsgate.lig.weather.utils;

public enum TypicalTemperature {
	MIN (1),
	MAX (2),
	AVG (3);
	
	private final int value;
	TypicalTemperature(int value) {
		this.value = value;
	}
}

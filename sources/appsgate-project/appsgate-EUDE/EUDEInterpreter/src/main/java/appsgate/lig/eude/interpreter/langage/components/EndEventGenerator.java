package appsgate.lig.eude.interpreter.langage.components;

public interface EndEventGenerator {
	void addEndEventListener(EndEventListener listener);
	void removeEndEventListener(EndEventListener listener);
}

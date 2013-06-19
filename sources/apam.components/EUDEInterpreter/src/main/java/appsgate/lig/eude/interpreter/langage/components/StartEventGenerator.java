package appsgate.lig.eude.interpreter.langage.components;

public interface StartEventGenerator {
	void addStartEventListener(StartEventListener listener);
	void removeStartEventListener(StartEventListener listener);
}

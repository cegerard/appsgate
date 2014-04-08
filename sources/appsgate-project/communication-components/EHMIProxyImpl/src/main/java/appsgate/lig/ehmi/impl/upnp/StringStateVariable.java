package appsgate.lig.ehmi.impl.upnp;

import java.beans.PropertyChangeEvent;

import org.osgi.service.upnp.UPnPLocalStateVariable;
import org.apache.felix.upnp.extra.util.UPnPEventNotifier;

abstract class StringStateVariable implements UPnPLocalStateVariable {

	String NAME;
	String DEFAULT_VALUE;
	String stringValue;

	private UPnPEventNotifier notifier;

	public String getCurrentStringValue() {
		return stringValue;
	}

	public void setStringValue(String value) {
		if (value != null && !value.equals(stringValue)) {
			String oldValue = stringValue.toString();
			stringValue = value;
			if (notifier != null)
				notifier.propertyChange(new PropertyChangeEvent(this, NAME,
						oldValue, value));
		}
	}

	public void setNotifier(UPnPEventNotifier notifier) {
		this.notifier = notifier;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Class<?> getJavaDataType() {
		return String.class;
	}

	@Override
	public String getUPnPDataType() {
		return TYPE_STRING;
	}

	@Override
	public Object getDefaultValue() {
		return DEFAULT_VALUE;
	}

	@Override
	public String[] getAllowedValues() {
		return null;
	}

	@Override
	public Number getMinimum() {
		return null;
	}

	@Override
	public Number getMaximum() {
		return null;
	}

	@Override
	public Number getStep() {
		return null;
	}

	@Override
	public boolean sendsEvents() {
		return true;
	}

	@Override
	public Object getCurrentValue() {
		return stringValue;
	}

}

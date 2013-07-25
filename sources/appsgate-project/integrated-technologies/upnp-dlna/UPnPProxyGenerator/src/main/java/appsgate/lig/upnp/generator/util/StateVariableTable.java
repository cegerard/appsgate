package appsgate.lig.upnp.generator.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.upnp.devicegen.holder.*;
import org.osgi.service.upnp.UPnPStateVariable;

/**
 * This class keeps a register of the the state variables defined in a Service Control Protocol Description
 * for further use in the transformation script.
 * 
 */
public class StateVariableTable {

	private static Map<String,Class<?>> javaMapping;
	static {
		javaMapping = new HashMap<String,Class<?>>();
		javaMapping.put(UPnPStateVariable.TYPE_UI1, int.class);
		javaMapping.put(UPnPStateVariable.TYPE_UI2, int.class);
		javaMapping.put(UPnPStateVariable.TYPE_UI4, long.class);
		javaMapping.put(UPnPStateVariable.TYPE_I1, int.class);
		javaMapping.put(UPnPStateVariable.TYPE_I2, int.class);
		javaMapping.put(UPnPStateVariable.TYPE_I4, int.class);
		javaMapping.put(UPnPStateVariable.TYPE_INT, int.class);
		javaMapping.put(UPnPStateVariable.TYPE_R4, float.class);
		javaMapping.put(UPnPStateVariable.TYPE_R8, double.class);
		javaMapping.put(UPnPStateVariable.TYPE_NUMBER, double.class);
		javaMapping.put(UPnPStateVariable.TYPE_FIXED_14_4, double.class);
		javaMapping.put(UPnPStateVariable.TYPE_FLOAT, float.class);
		javaMapping.put(UPnPStateVariable.TYPE_CHAR, char.class);
		javaMapping.put(UPnPStateVariable.TYPE_STRING, String.class);
		javaMapping.put(UPnPStateVariable.TYPE_DATE, Date.class);
		javaMapping.put(UPnPStateVariable.TYPE_DATETIME, Date.class);
		javaMapping.put(UPnPStateVariable.TYPE_DATETIME_TZ, Date.class);
		javaMapping.put(UPnPStateVariable.TYPE_TIME, long.class);
		javaMapping.put(UPnPStateVariable.TYPE_TIME_TZ, long.class);
		javaMapping.put(UPnPStateVariable.TYPE_BOOLEAN, boolean.class);
		javaMapping.put(UPnPStateVariable.TYPE_BIN_BASE64, byte[].class);
		javaMapping.put(UPnPStateVariable.TYPE_BIN_HEX, byte[].class);
		javaMapping.put(UPnPStateVariable.TYPE_URI, String.class);
		javaMapping.put(UPnPStateVariable.TYPE_UUID, String.class);
	}

	private static Map<Class<?>,Class<?>> mutable;
	static {
		mutable = new HashMap<Class<?>,Class<?>>();
		mutable.put(boolean.class,BooleanHolder.class);
		mutable.put(byte[].class,ByteArrayHolder.class);
		mutable.put(int.class,IntegerHolder.class);
		mutable.put(long.class,LongHolder.class);
		mutable.put(float.class,FloatHolder.class);
		mutable.put(double.class,DoubleHolder.class);
		mutable.put(char.class,CharacterHolder.class);
		mutable.put(String.class,StringHolder.class);
		mutable.put(Date.class,DateHolder.class);
	}

	private static Map<Class<?>,Class<?>> boxed;
	static {
		boxed = new HashMap<Class<?>,Class<?>>();
		boxed.put(boolean.class,Boolean.class);
		boxed.put(byte[].class,byte[].class);
		boxed.put(int.class,Integer.class);
		boxed.put(long.class,Long.class);
		boxed.put(float.class,Float.class);
		boxed.put(double.class,Double.class);
		boxed.put(char.class,Character.class);
		boxed.put(String.class,String.class);
		boxed.put(Date.class,Date.class);
	}

	final private static boolean trace = false;

	private final String			 label;
	private final Map<String,String> declaredVariables;
	
	public StateVariableTable(String label) {
		this.label = label;
		this.declaredVariables = new HashMap<String, String>();
		
		trace("created");
	}
	
	
	public void register(String variable,String type) {
		trace("registering",variable,type);
		declaredVariables.put(variable.trim(),type);
	}

	public void clear() {
		trace("clearing");
		declaredVariables.clear();
	}
	
	
	public String getType(String variable) {
		String type = declaredVariables.get(variable.trim());
		if (type == null)
			trace("undefined type for variable ",variable);
		return type != null ? javaMapping.get(type).getSimpleName() : null;
	}

	public String getBoxedType(String variable) {
		String type = declaredVariables.get(variable.trim());
		if (type == null)
			trace("undefined type for variable ",variable);
		return type != null ? boxed.get(javaMapping.get(type)).getSimpleName() : null;
	}

	public String getInputParameterType(String variable) {
		String type = declaredVariables.get(variable.trim());
		if (type == null)
			trace("undefined type for variable ",variable);
		return type != null ? javaMapping.get(type).getSimpleName() : null;
	}

	public String getOutputParameterType(String variable) {
		String type = declaredVariables.get(variable.trim());
		if (type == null)
			trace("undefined type for variable ",variable);
		return type != null ? mutable.get(javaMapping.get(type)).getSimpleName() : null;
	}

	private void trace(String ... messages) {
		if(trace) {
			System.out.print("StateVariableTable ");
			System.out.print(label);
			System.out.print(" : ");
			for (String message : messages) {
				System.out.print(message);
				System.out.print(" ");
			}
			System.out.println();
		};
	}

}
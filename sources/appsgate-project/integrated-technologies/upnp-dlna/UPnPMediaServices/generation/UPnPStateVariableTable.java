import java.util.HashMap;
import java.util.Map;

/**
 * This class provides an XSLT extension function that
 * may be utilized by Xalan-Java extension mechanism.
 */
public class UPnPStateVariableTable {

	private static Map stateVariableTable=new HashMap();
	
	final private static boolean trace=true;
	/**
	 */
	public static String clean() {
		if(trace) System.out.println("UPnPStateVariableTable clean");
		stateVariableTable.clear();
		return "";
	}

	public static String add(String varname,String datatype) {
		if(trace) System.out.println("UPnPStateVariableTable add("+varname+","+datatype+")");
		stateVariableTable.put(varname,datatype);
		return "";
	}
	
	public static String getUPnPDataType(String varname) throws IllegalArgumentException {
		if(trace) System.out.println("UPnPStateVariableTable getUPnPDataType("+varname+")");
		String datatype=(String)stateVariableTable.get(varname);
		if(datatype==null) throw new IllegalArgumentException("No \""+varname+"\" statevariable");
		return datatype;
	}

	public static String getClassName(String varname) {
		if(trace) System.out.println("UPnPStateVariableTable getClassName("+varname+")");
		String datatype=(String)stateVariableTable.get(varname);
		if(datatype==null) throw new IllegalArgumentException("No \""+varname+"\" statevariable");
		return UPnPDataTypeUtil.getClassName(datatype);
	}

	public static String getInputJavaType(String varname) {
		if(trace) System.out.println("UPnPStateVariableTable getInputJavaType("+varname+")");
		String datatype=(String)stateVariableTable.get(varname);
		if(datatype==null) throw new IllegalArgumentException("No \""+varname+"\" statevariable");
		return UPnPDataTypeUtil.getInputJavaType(datatype);
	}

	public static String getOutputJavaType(String varname) {
		if(trace) System.out.println("UPnPStateVariableTable getOutputJavaType("+varname+")");
		String datatype=(String)stateVariableTable.get(varname);
		if(datatype==null) throw new IllegalArgumentException("No \""+varname+"\" statevariable");
		return UPnPDataTypeUtil.getOutputJavaType(datatype);
	}
	
}
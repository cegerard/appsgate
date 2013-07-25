package appsgate.lig.upnp.generator.util;

/**
 * This is a utility class to handle all naming conventions for Java fields, methods and parameters 
 * derived from* the service description elements
 * 
 * @author vega
 *
 */
public class Naming {

	private static String getSuffix(String serviceId) {
		String[] segments = serviceId.split(":");
		return segments[segments.length-1];
	}

	public static String getField(String serviceId) {
		serviceId = getSuffix(serviceId.trim());
		StringBuffer fieldName = new StringBuffer();
		fieldName.append(Character.toLowerCase(serviceId.charAt(0)));
		fieldName.append(serviceId, 1, serviceId.length());
		return fieldName.toString();
	}

	public static String getGetter(String serviceId) {
		serviceId = getSuffix(serviceId.trim());
		StringBuffer getterName = new StringBuffer();
		return getterName.append("get").append(serviceId).toString();
	}

	public static String getField(String serviceId,String variable) {
		variable 	= variable.trim();
		serviceId 	= serviceId == null ? null : serviceId.trim().isEmpty() ? null : serviceId.trim();
		
		StringBuffer fieldName = new StringBuffer();
		fieldName.append(Character.toLowerCase(variable.charAt(0)));
		fieldName.append(variable, 1, variable.length());
		if (serviceId != null) {
			fieldName.append("_").append(getSuffix(serviceId));
		}
		return fieldName.toString();
	}

	public static String getGetter(String serviceId,String variable) {
		variable 	= variable.trim();
		serviceId 	= serviceId == null ? null : serviceId.trim().isEmpty() ? null : serviceId.trim();

		StringBuffer getterName = new StringBuffer();
		getterName.append("get");
		getterName.append(Character.toUpperCase(variable.charAt(0)));
		getterName.append(variable, 1, variable.length());
		if (serviceId != null) {
			getterName.append("_").append(getSuffix(serviceId));
		}
		return getterName.toString();
	}

	public static String getMethod(String serviceId,String action) {
		action 		= action.trim();
		serviceId 	= serviceId == null ? null : serviceId.trim().isEmpty() ? null : serviceId.trim();

		StringBuffer methodName = new StringBuffer();
		methodName.append(Character.toLowerCase(action.charAt(0)));
		methodName.append(action, 1, action.length());
		if (serviceId != null) {
			methodName.append("_").append(getSuffix(serviceId));
		}
		return methodName.toString();
	}

	public static String getArgument(String parameter) {
		parameter = parameter.trim();
		StringBuffer argumentName = new StringBuffer();
		argumentName.append(Character.toLowerCase(parameter.charAt(0)));
		argumentName.append(parameter, 1, parameter.length());
		return argumentName.toString();
	}



}

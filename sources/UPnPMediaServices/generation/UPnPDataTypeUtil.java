/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//import org.osgi.service.upnp.UPnPStateVariable;

/**
 * utility class to obtains java class for UPnP data type
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class UPnPDataTypeUtil {
	
	
	private static Map dataTypeMap;
	static {
		dataTypeMap = new HashMap();
		dataTypeMap.put(UPnPStateVariable.TYPE_UI1, Integer.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_UI2, Integer.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_UI4, Long.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_I1, Integer.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_I2, Integer.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_I4, Integer.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_INT, Integer.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_R4, Float.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_R8, Double.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_NUMBER, Double.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_FIXED_14_4, Double.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_FLOAT, Float.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_CHAR, Character.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_STRING, String.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_DATE, Date.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_DATETIME, Date.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_DATETIME_TZ, Date.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_TIME, Long.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_TIME_TZ, Long.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_BOOLEAN, Boolean.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_BIN_BASE64, byte[].class);
		dataTypeMap.put(UPnPStateVariable.TYPE_BIN_HEX, byte[].class);
		dataTypeMap.put(UPnPStateVariable.TYPE_URI, String.class);
		dataTypeMap.put(UPnPStateVariable.TYPE_UUID, String.class);
	}

	private static Map inputJavaTypeMap;
	static {
		inputJavaTypeMap = new HashMap();
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_UI1, int.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_UI2, int.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_UI4, long.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_I1, int.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_I2, int.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_I4, int.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_INT, int.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_R4, float.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_R8, double.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_NUMBER, double.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_FIXED_14_4, double.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_FLOAT, float.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_CHAR, char.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_STRING, String.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_DATE, Date.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_DATETIME, Date.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_DATETIME_TZ, Date.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_TIME, long.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_TIME_TZ, long.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_BOOLEAN, boolean.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_BIN_BASE64, byte[].class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_BIN_HEX, byte[].class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_URI, String.class);
		inputJavaTypeMap.put(UPnPStateVariable.TYPE_UUID, String.class);
	}

	private static Map outputJavaTypeMap;
	static {
		outputJavaTypeMap = new HashMap();
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_UI1, "IntegerHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_UI2, "IntegerHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_UI4, "LongHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_I1, "IntegerHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_I2, "IntegerHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_I4, "IntegerHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_INT, "IntegerHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_R4, "FloatHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_R8, "DoubleHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_NUMBER, "DoubleHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_FIXED_14_4, "DoubleHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_FLOAT, "FloatHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_CHAR, "CharacterHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_STRING, "StringHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_DATE, "DateHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_DATETIME, "DateHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_DATETIME_TZ, "DateHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_TIME, "LongHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_TIME_TZ, "LongHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_BOOLEAN, "BooleanHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_BIN_BASE64, "ByteArrayHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_BIN_HEX, "ByteArrayHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_URI, "StringHolder");
		outputJavaTypeMap.put(UPnPStateVariable.TYPE_UUID, "StringHolder");
	}
	
	public static Class getClass(String upnpDataType){
		return (Class)dataTypeMap.get(upnpDataType);
	}

	public static String getClassName(String upnpDataType)throws IllegalArgumentException {
		Class clazz=(Class)dataTypeMap.get(upnpDataType);
		if(clazz==null) throw new IllegalArgumentException("No class for "+upnpDataType); 		
		if(clazz.equals(byte[].class)) return "byte[]";
		return clazz.getName();
	}

	public static String getInputJavaType(String upnpDataType)throws IllegalArgumentException {
		Class clazz=(Class)inputJavaTypeMap.get(upnpDataType);
		if(clazz==null) throw new IllegalArgumentException("No class for "+upnpDataType); 		
		if(clazz.equals(byte[].class)) return "byte[]";
		return clazz.getName();
	}

	public static String getOutputJavaType(String upnpDataType)throws IllegalArgumentException {
		String name=(String)outputJavaTypeMap.get(upnpDataType);
		if(name==null) throw new IllegalArgumentException("No class for "+upnpDataType); 		
		return name;
	}

	
	public static Object instanciateObject(String upnpDataType, String value){
		Class clazz=(Class)dataTypeMap.get(upnpDataType);
		if(clazz.equals(Integer.class)) return new Integer(value);
		if(clazz.equals(Long.class)) return new Long(value);
		if(clazz.equals(Float.class)) return new Float(value);
		if(clazz.equals(Double.class)) return new Double(value);
		if(clazz.equals(Boolean.class)) return new Boolean(value);
		if(clazz.equals(String.class)) return new String(value);
		if(clazz.equals(Character.class)) return new Character(value.charAt(0));

		// TODO TYPE_DATE,TYPE_BIN_BASE64, TYPE_BIN_HEX
		if(clazz==null) throw new IllegalArgumentException("Can not instanciate object for "+upnpDataType); 		
		return null;
	}

}
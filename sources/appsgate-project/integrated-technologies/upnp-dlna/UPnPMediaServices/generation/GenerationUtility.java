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

/**
 * This class provides an XSLT extension function that
 * may be utilized by Xalan-Java extension mechanism.
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class GenerationUtility {

	// to understand XSLT java method invocations
	private static int cpt=0;
	private final static boolean trace=false;

	
	/**
	 * This method variabilize the first character in the provided string.
	 * @return resulted string
	 */
	public static String variabilize(String classname) {
	    if(trace) System.out.println((++cpt)+"\tVariabilize "+classname);
		return Character.toLowerCase(classname.charAt(0)) + classname.substring(1);
	}

	/**
	 * This method capitalizes the first character in the provided string.
	 * @return resulted string
	 */
	public static String capitalize(String membername) {
		if(trace) System.out.println((++cpt)+"\tCapitalize "+membername);
		return Character.toUpperCase(membername.charAt(0)) + membername.substring(1);
	}

	/**
	 * This method capitalizes all characters in the provided string.
	 * @return resulted string
	 */
	public static String finalstaticOf(String membername) {
		if(trace) System.out.println((++cpt)+"\tFinalstaticof "+  membername);	
		int len=membername.length();
		StringBuffer sb=new StringBuffer(len+2);
		for(int i=0; i<len; i++){
			char c=membername.charAt(i);
			if(Character.isLowerCase(c) ) {
				sb.append(Character.toUpperCase(c));
			} else if(Character.isUpperCase(c) ) {
				sb.append('_').append(c);
			} else {
				
				sb.append(c);				
			}
		} 
		return sb.toString();
	}
	
	/**
	 * This method returns the package name in a full class name
	 * @return resulted string
	 */
	public static String packageOf(String fullclassname) {
		if(trace) System.out.println((++cpt)+"\tPackageof " + fullclassname);
		int index=fullclassname.lastIndexOf(".");
		if(index>0) {
			return fullclassname.substring(0,index);
		} else {
			return "";	
		}
	}

	/**
	 * This method returns the package name in a full class name
	 * @return resulted string
	 */
	public static String classOf(String fullclassname) {
		if(trace) System.out.println((++cpt)+"\tClassof " + fullclassname);
		int index=fullclassname.lastIndexOf(".");
		if(index>0) {
			return fullclassname.substring(index+1);
		} else {
			return fullclassname;	
		}
	}
}
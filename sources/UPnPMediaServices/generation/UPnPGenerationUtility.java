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

public class UPnPGenerationUtility {

	private final static String URN_UPNP_ORG_SERVICE="urn:schemas-upnp-org:service:";
	private final static String URN_UPNP_ORG_SERVICE_ID="urn:upnp-org:serviceId:";
	private final static String URN_UPNP_ORG_DEVICE="urn:schemas-upnp-org:device:";
	
	/**
	 */

	private static String getType(String urn, String prefix ) {
		return getType(urn, prefix, true);
	}
	
	private static String getType(String urn, String prefix , boolean versioned) {
		int last=urn.lastIndexOf(':');
		int first;
		if(urn.startsWith(prefix)){
			first=prefix.length();
			return versioned ? urn.substring(first,last) : urn.substring(first);
		} else if(last>0 && (first=urn.lastIndexOf(':',last-1))>0) {
			return versioned ? urn.substring(first+1,last) : urn.substring(last+1);
		} else {
			String st=urn.replace(':','_');
			st=st.replace('-','_');
			return st;						
		}
	}

	private static String getVersion(String urn) {
		int last=urn.lastIndexOf(":");
		String version;
		if(last<0) {
			version="0";
		} else {
			version=urn.substring(last+1);
		}
		return version;
	}

	private static String getTypeVersion(String urn, String prefix) {
		return getType(urn,prefix)+getVersion(urn);
	}

	/**
	 */
	public static String deviceType(String urn) {
		return getType(urn,URN_UPNP_ORG_DEVICE);
	}

	public static String deviceTypeVersion(String urn) {
		return getTypeVersion(urn,URN_UPNP_ORG_DEVICE);
	}
	
	/**
	 */
	public static String serviceType(String urn) {
		return getType(urn,URN_UPNP_ORG_SERVICE);
	}

	public static String serviceTypeVersion(String urn) {
		return getTypeVersion(urn,URN_UPNP_ORG_SERVICE);
	}
	
	/**
	 */
	public static String serviceId(String urn) {
		return getType(urn,URN_UPNP_ORG_SERVICE_ID,false);
	}


	/**
	 */
	public static String serviceVersion(String urn) {
		return getVersion(urn);
	}

}
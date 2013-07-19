<?xml version="1.0"?>
<!--
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
-->

<!--
 * @author Felix Project Team mailto:dev@felix.apache.org
-->
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="1.0"   
    xmlns:java="http://xml.apache.org/xslt/java"
    exclude-result-prefixes="java">
<xsl:output method = "text"/>

<!-- parameter declaration -->
<xsl:param name="author"/>
<xsl:param name="date"/>
<xsl:param name="package"/>
<xsl:param name="classname"/>
<xsl:param name="serviceid"/>
<xsl:param name="servicetype"/>

<xsl:template match="/">
/*
__BANNER__
*/
// this file was generated at <xsl:value-of select="$date"/> by <xsl:value-of select="$author"/>
package <xsl:value-of select="$package"/>.proxy;

import org.apache.felix.upnp.devicegen.holder.*;
import <xsl:value-of select="$package"/>.*;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import org.osgi.framework.Filter;

import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPEventListener;
import org.osgi.service.upnp.UPnPException;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPService;

import fr.imag.adele.apam.Instance;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectSpec;
<xsl:apply-templates select="*"/>
</xsl:template>

<xsl:template match="scpd">

public class <xsl:value-of select="$classname"/>ProxyImpl implements CoreObjectSpec, <xsl:value-of select="$classname"/>, UPnPEventListener {		

	private String 		userObjectName;
	private int			locationId;
	private String 		pictureId;
	
	private String 		serviceType ;
	private String 		serviceId ;

	private UPnPDevice  upnpDevice;
	private UPnPService upnpService;
	
	<xsl:value-of select="$classname"/>ProxyImpl(){
		super();
	}
	
	@SuppressWarnings("unused")
	private void initialize(Instance instance) {
		serviceType = instance.getProperty(UPnPService.TYPE);
		serviceId 	= instance.getProperty(UPnPService.ID);
		upnpService	= upnpDevice.getService(serviceId);
	}
	
	public String getAbstractObjectId() {
		return upnpDevice.getDescriptions(null).get(UPnPDevice.ID)+"/"+serviceId;
	}

	public String getUserType() {
		// TODO Generate unique type identifier !!!
		return Integer.toString(serviceType.hashCode());
	}

	public int getObjectStatus() {
		return 2;
	}
	
	<xsl:variable name="initStateVariables" select="java:UPnPStateVariableTable.clean()"/>
	<xsl:apply-templates select="serviceStateTable/stateVariable" mode="getterdefinition"/>	
	public String getUserObjectName() {
		return userObjectName;
	}

	public void setUserObjectName(String userName) {
		this.userObjectName = userName;
	}

	public int getLocationId() {
		return locationId;
	}

	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}
	
	public String getPictureId() {
		return pictureId;
	}

	public void setPictureId(String pictureId) {
		this.pictureId = pictureId;
	}

	public JSONObject getDescription() throws JSONException {
		JSONObject description = new JSONObject();
		
		description.put("id", getAbstractObjectId());
		description.put("physical-device", upnpDevice.getDescriptions(null).get(UPnPDevice.ID));
		description.put("name", getUserObjectName());
		description.put("type", getUserType()); 
		description.put("locationId", getLocationId());
		description.put("status", getObjectStatus());
		
		<xsl:apply-templates select="serviceStateTable/stateVariable" mode="description"/>	
		
		return description;
	}

	@SuppressWarnings("unused")
	private Filter upnpEventFilter;
	
	@Override
	@SuppressWarnings("unchecked")
	public void notifyUPnPEvent(String deviceId, String serviceId,	@SuppressWarnings("rawtypes") Dictionary events) {

		Enumeration&lt;String&gt; variables = events.keys();
		while( variables.hasMoreElements()) {

			String variable = variables.nextElement();
			Object value	= events.get(variable);
			
			stateChanged(variable,value);
		}
	}		

	private NotificationMsg stateChanged(String variable, Object value) {
	
	<xsl:apply-templates select="serviceStateTable/stateVariable" mode="update"/>	
		return new Notification(variable, value.toString());
	}

	private  class Notification implements NotificationMsg {

		private final String variable;
		private final String value;
		
		public Notification( String variable, String value) {
			this.variable 	= variable;
			this.value		= value;
		}
		
		@Override
		public CoreObjectSpec getSource() {
			return <xsl:value-of select="$classname"/>ProxyImpl.this;
		}

		@Override
		public String getNewValue() {
			return value;
		}

		@Override
		public JSONObject JSONize() throws JSONException {
			
			JSONObject notification = new JSONObject();
			
			notification.put("objectId", getAbstractObjectId());
			notification.put("varName", variable);
			notification.put("value", value);
		
			return notification;
		}	
	
	}

	
	<xsl:apply-templates select="actionList/action"  mode="itfdefinition"/>	

	
}
</xsl:template>

	
<xsl:template match="serviceStateTable/stateVariable" mode="getterdefinition">
	<xsl:variable name="addStateVariable" 	select="java:UPnPStateVariableTable.add(./name,./dataType)"/>
	<xsl:variable name="stateVariableType" 	select="java:UPnPStateVariableTable.getInputJavaType(./name)"/>
	<xsl:variable name="stateVariableCapitalizedName" 	select="java:GenerationUtility.capitalize(./name)"/>
	<xsl:variable name="stateVariableField" select="java:GenerationUtility.variabilize(./name)"/>
	<xsl:choose>
	<xsl:when test="self::node()[starts-with(name,'A_ARG')]"></xsl:when>
	<xsl:when test="self::node()[sendEventsAttribute='no']"></xsl:when>
	<xsl:otherwise>
	private <xsl:value-of select="$stateVariableType"/><xsl:text> </xsl:text><xsl:value-of select="$stateVariableField"/>;
	
	@Override
	public <xsl:value-of select="$stateVariableType"/> get<xsl:value-of select="$stateVariableCapitalizedName"/>() {
		return <xsl:value-of select="$stateVariableField"/>;
	}
	
	</xsl:otherwise>	
	</xsl:choose>	
</xsl:template>
	
<xsl:template match="serviceStateTable/stateVariable" mode="description">
	<xsl:variable name="stateVariableType" 	select="java:UPnPStateVariableTable.getClassName(./name)"/>
	<xsl:variable name="stateVariableCapitalizedName" 	select="java:GenerationUtility.capitalize(./name)"/>
	<xsl:variable name="stateVariableField" select="java:GenerationUtility.variabilize(./name)"/>
	<xsl:choose>
	<xsl:when test="self::node()[starts-with(name,'A_ARG')]"></xsl:when>
	<xsl:when test="self::node()[sendEventsAttribute='no']"></xsl:when>
	<xsl:otherwise>
		description.put("<xsl:value-of select="$stateVariableField"/>", get<xsl:value-of select="$stateVariableCapitalizedName"/>());
	</xsl:otherwise>	
	</xsl:choose>	
</xsl:template>

<xsl:template match="serviceStateTable/stateVariable" mode="update">
	<xsl:variable name="stateVariableType" 	select="java:UPnPStateVariableTable.getClassName(./name)"/>
	<xsl:variable name="stateVariableName" 	select="./name"/>
	<xsl:variable name="stateVariableField" select="java:GenerationUtility.variabilize(./name)"/>
	<xsl:choose>
	<xsl:when test="self::node()[starts-with(name,'A_ARG')]"></xsl:when>
	<xsl:when test="self::node()[sendEventsAttribute='no']"></xsl:when>
	<xsl:otherwise>
		if (variable.equals("<xsl:value-of select="$stateVariableName"/>"))
			<xsl:value-of select="$stateVariableField"/> = (<xsl:value-of select="$stateVariableType"/>) value;
	
	</xsl:otherwise>	
	</xsl:choose>	
</xsl:template>

<xsl:template match="action"  mode="itfdefinition">
	<xsl:variable name="methodName" select="java:GenerationUtility.variabilize(./name)"/>
	<xsl:variable name="actionName" select="./name"/>
	/**
	 * This method is "add description here"	
<xsl:apply-templates select="argumentList" mode="comment"/>
	 */
	@SuppressWarnings("rawtypes")
	public void <xsl:value-of select="$methodName"/>(
		<xsl:apply-templates select="argumentList" mode="param"/>
	) throws UPnPException {
		
		UPnPAction upnpAction = upnpService.getAction("<xsl:value-of select="$actionName"/>");

		if (upnpAction == null)
			throw new UnsupportedOperationException("Action "+"<xsl:value-of select="$actionName"/>"+" is not provided by service "+getAbstractObjectId());

		Dictionary _parameters = new Hashtable();
		
		<xsl:apply-templates select="argumentList" mode="invoke.input"/>

		Dictionary _result;
		try {
				_result = upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}

		<xsl:apply-templates select="argumentList" mode="invoke.output"/>
	}

</xsl:template>

<xsl:template match="argumentList" mode="param">
		<xsl:apply-templates select="argument" mode="param"/>
</xsl:template>

<xsl:template match="argument[last()]" mode="param">
	<xsl:variable name="paramName" select="java:GenerationUtility.variabilize(./name)"/>

	<xsl:choose>
		<xsl:when test="self::node()[direction='in']"><xsl:value-of select="java:UPnPStateVariableTable.getInputJavaType(./relatedStateVariable)"/></xsl:when>
		<xsl:when test="self::node()[direction='out']"><xsl:value-of select="java:UPnPStateVariableTable.getOutputJavaType(./relatedStateVariable)"/></xsl:when>
	</xsl:choose><xsl:text> </xsl:text><xsl:value-of select="$paramName"/>

</xsl:template>
	
<xsl:template match="argument" mode="param">
	<xsl:variable name="paramName" select="java:GenerationUtility.variabilize(./name)"/>

	<xsl:choose>
		<xsl:when test="self::node()[direction='in']"><xsl:value-of select="java:UPnPStateVariableTable.getInputJavaType(./relatedStateVariable)"/></xsl:when>
		<xsl:when test="self::node()[direction='out']"><xsl:value-of select="java:UPnPStateVariableTable.getOutputJavaType(./relatedStateVariable)"/></xsl:when>
	</xsl:choose><xsl:text> </xsl:text><xsl:value-of select="$paramName"/>,

</xsl:template>
	
<xsl:template match="argumentList" mode="invoke.input">
		<xsl:apply-templates select="argument" mode="invoke.input"/>
</xsl:template>

<xsl:template match="argument" mode="invoke.input">
	<xsl:variable name="paramName" select="java:GenerationUtility.variabilize(./name)"/>

	<xsl:choose>
		<xsl:when test="self::node()[direction='in']">
		_parameters.put("<xsl:value-of select="./name"/>", 
						(new <xsl:value-of select="java:UPnPStateVariableTable.getOutputJavaType(./relatedStateVariable)"/>(<xsl:value-of select="$paramName"/>)).getObject());
		</xsl:when>
	</xsl:choose>

</xsl:template>

<xsl:template match="argumentList" mode="invoke.output">
		<xsl:apply-templates select="argument" mode="invoke.output"/>
</xsl:template>

<xsl:template match="argument" mode="invoke.output">
	<xsl:variable name="paramName" select="java:GenerationUtility.variabilize(./name)"/>
	<xsl:variable name="className" select="java:UPnPStateVariableTable.getClassName(./relatedStateVariable)"/>

	<xsl:choose>
		<xsl:when test="self::node()[direction='out']">
		<xsl:value-of select="$paramName"/>.setValue(<xsl:value-of select="java:UPnPStateVariableTable.getOutputJavaType(./relatedStateVariable)"/>.toValue((<xsl:value-of select="$className"/>)_result.get("<xsl:value-of select="./name"/>")));
		</xsl:when>
	</xsl:choose>
	

</xsl:template>
	

<xsl:template match="argumentList" mode="comment">
		<xsl:apply-templates select="argument" mode="comment"/>
</xsl:template>

<xsl:template match="argument" mode="comment">
	<xsl:variable name="paramName" select="java:GenerationUtility.variabilize(./name)"/>

	<xsl:text> * </xsl:text><xsl:value-of select="$paramName"/>
	<xsl:choose>
		<xsl:when test="self::node()[direction='in']"> in </xsl:when>
		<xsl:when test="self::node()[direction='out']"> out </xsl:when>
	</xsl:choose> parameter

</xsl:template>

<xsl:template match="*"></xsl:template>

</xsl:stylesheet>

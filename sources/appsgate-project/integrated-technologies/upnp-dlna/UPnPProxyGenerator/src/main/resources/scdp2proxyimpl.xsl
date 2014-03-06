<?xml version="1.0"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="1.0"
    xmlns:table="xalan://appsgate.lig.upnp.generator.util.StateVariableTable"
    xmlns:naming="xalan://appsgate.lig.upnp.generator.util.Naming">

<xsl:import href="scdp2java.xsl"/>

<xsl:param name="servicetype"/>
<xsl:param name="serviceId"/>
<xsl:param name="package"/>
<xsl:param name="classname"/>

<xsl:param name="interfacePackage"/>
<xsl:param name="interfaceName"/>

<xsl:output method = "text"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/">
package <xsl:value-of select="$package"/>;

import org.apache.felix.upnp.devicegen.holder.*;
import <xsl:value-of select="$interfacePackage"/>.<xsl:value-of select="$interfaceName"/>;

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
import appsgate.lig.core.object.spec.CoreObjectBehavior;

<xsl:apply-templates select="*"/>
</xsl:template>

<xsl:template match="scpd">

public class <xsl:value-of select="$classname"/> extends CoreObjectBehavior implements CoreObjectSpec, <xsl:value-of select="$interfaceName"/>, UPnPEventListener {		

	private String 		userObjectName;
	private int			locationId;
	private String 		pictureId;
	
	private String 		serviceType ;
	private String 		serviceId ;

	private UPnPDevice  upnpDevice;
	private UPnPService upnpService;
	
	<xsl:value-of select="$classname"/>(){
		super();
	}
	<xsl:apply-templates select="serviceStateTable/stateVariable" mode="definition"/>
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
		
<xsl:apply-templates select="serviceStateTable/stateVariable[not(starts-with(name,'A_ARG')) and sendEventsAttribute !='no']" mode="description"/>	
		return description;
	}
	
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.SERVICE;
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
	
<xsl:apply-templates select="serviceStateTable/stateVariable[not(starts-with(name,'A_ARG')) and sendEventsAttribute !='no']" mode="update"/>	
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
			return <xsl:value-of select="$classname"/>.this;
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

	<xsl:apply-templates select="actionList/action"  mode="actionDefinition"/>	
}
</xsl:template>


<xsl:template match="stateVariable[starts-with(name,'A_ARG') or sendEventsAttribute='no']" mode="definition">
	<xsl:apply-templates select="." mode="register"/>
</xsl:template>
	
<xsl:template match="stateVariable" mode="definition">
	<xsl:apply-templates select="." mode="register"/>
	<xsl:text>
	</xsl:text>
	<xsl:apply-templates select="." mode="fieldDeclaration"/>
	<xsl:text>
	
	</xsl:text>
	<xsl:apply-templates select="." mode="getterSignature"/> {	
		return <xsl:value-of select="naming:getField($serviceId,./name)"/>;
	}
</xsl:template>

<xsl:template match="stateVariable" mode="description">		
<xsl:text>		</xsl:text>description.put("<xsl:value-of select="naming:getField($serviceId,./name)"/>",<xsl:value-of select="naming:getGetter($serviceId,./name)"/>());<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="stateVariable" mode="update">
<xsl:text>		</xsl:text>if (variable.equals("<xsl:value-of select="./name"/>"))
			<xsl:value-of select="naming:getField($serviceId,./name)"/> = (<xsl:value-of select="table:getBoxedType($stateVariables,./name)"/>) value;<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="action"  mode="actionDefinition">
	<xsl:text>
	</xsl:text>@Override
	<xsl:apply-templates select="." mode="methodSignature"/> {
	
		UPnPAction _upnpAction = upnpService.getAction("<xsl:value-of select="./name"/>");
		if (_upnpAction == null)
			throw new UnsupportedOperationException("Action "+"<xsl:value-of select="./name"/>"+" is not provided by service "+getAbstractObjectId());

		Dictionary&lt;String,Object&gt; _parameters = new Hashtable&lt;String,Object&gt;();
		
		<xsl:apply-templates select="argumentList/argument[direction='in']" mode="invoke.input"/>
		@SuppressWarnings(<xsl:choose><xsl:when test="count(argumentList/argument[direction='out']) > 0">"rawtypes"</xsl:when><xsl:otherwise>{"rawtypes","unused"}</xsl:otherwise></xsl:choose>)
		Dictionary _result;
		try {
			_result = _upnpAction.invoke(_parameters);
		} catch (UPnPException e) {
			throw e;
		} catch (Exception e) {
			throw new UPnPException(UPnPException.DEVICE_INTERNAL_ERROR,e.getMessage());
		}
		
		<xsl:apply-templates select="argumentList/argument[direction='out']" mode="invoke.output"/>
	}
</xsl:template>

<xsl:template match="argument" mode="invoke.input">
	<xsl:apply-templates select="." mode="input"/><xsl:text>
		</xsl:text>	
</xsl:template>

<xsl:template match="argument" mode="invoke.output">
	<xsl:apply-templates select="." mode="output"/><xsl:text>
		</xsl:text>	
</xsl:template>
	
<xsl:template match="argument" mode="input">
	<xsl:text>_parameters.put(</xsl:text>
	<xsl:text>"</xsl:text><xsl:value-of select="./name"/><xsl:text>"</xsl:text>
	<xsl:text>,</xsl:text>
	<xsl:value-of select="naming:getArgument(./name)"/>
	<xsl:text>);</xsl:text> 
</xsl:template>

<xsl:template match="argument" mode="output">
	<xsl:value-of select="naming:getArgument(./name)"/><xsl:text>.setValue(</xsl:text>
	<xsl:text>(</xsl:text><xsl:value-of select="table:getBoxedType($stateVariables,./relatedStateVariable)"/><xsl:text>)</xsl:text>
	<xsl:text>_result.get("</xsl:text><xsl:value-of select="./name"/><xsl:text>")</xsl:text>
	<xsl:text>);</xsl:text>
</xsl:template>
	
<xsl:template match="*"></xsl:template>

</xsl:stylesheet>

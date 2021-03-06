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
import java.util.Hashtable;

import org.osgi.framework.Filter;


import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPEventListener;
import org.osgi.service.upnp.UPnPException;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPService;

import fr.imag.adele.apam.Instance;

import appsgate.lig.upnp.adapter.event.UPnPEvent;

<xsl:apply-templates select="*"/>
</xsl:template>

<xsl:template match="scpd">

public class <xsl:value-of select="$classname"/> implements  <xsl:value-of select="$interfaceName"/>, UPnPEventListener {


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


	@SuppressWarnings("unused")
	private Filter upnpEventFilter;
	
	@Override
	public void notifyUPnPEvent(String deviceId, String serviceId,	@SuppressWarnings("rawtypes") Dictionary events) {

		try {
	        UPnPEvent evt = new UPnPEvent(deviceId, serviceId, events);
			stateChanged(evt);
		}
		catch(Exception exception) {
			System.out.println("unexpected exception while handling UPnP notification "+deviceId+" "+serviceId+" "+events);
			exception.printStackTrace();
		}
	}

	private UPnPEvent stateChanged(UPnPEvent event) {

		return event;
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

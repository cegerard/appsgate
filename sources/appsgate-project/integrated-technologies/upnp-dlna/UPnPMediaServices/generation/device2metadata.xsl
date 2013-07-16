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
    xmlns="fr.imag.adele.apam" 
    xmlns:ipojo="org.apache.felix.ipojo"
    xmlns:java="http://xml.apache.org/xslt/java"
 	exclude-result-prefixes="java">
 	
<xsl:output omit-xml-declaration="yes" indent="yes"/>
<xsl:strip-space elements="*"/>

<!-- parameter declaration -->
<xsl:param name="author"/>
<xsl:param name="date"/>
<xsl:param name="package"/>

<xsl:template match="root">

<apam xmlns="fr.imag.adele.apam" xmlns:ipojo="org.apache.felix.ipojo"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="fr.imag.adele.apam http://repository-apam.forge.cloudbees.com/release/schema/ApamCore.xsd">

	<xsl:apply-templates select="serviceList/service" mode="metadata"/>
</apam>

</xsl:template>

<xsl:template match="service" mode="metadata">
	<xsl:variable name="serviceType" select="./serviceType/text()"/>
	<xsl:variable name="serviceId" select="./serviceId/text()"/>
	<xsl:variable name="classname" select="java:UPnPGenerationUtility.serviceType(./serviceType)"/>
	
	<implementation specification="" push="">
		<xsl:attribute name="name"><xsl:value-of select="$classname"/>ProxyImpl</xsl:attribute>	
		<xsl:attribute name="classname"><xsl:value-of select='$package'/>.proxy.<xsl:value-of select='$classname'/>ProxyImpl</xsl:attribute>	
		<xsl:attribute name="specification">AbstractObjectSpec</xsl:attribute>	
		<xsl:attribute name="push">stateChanged</xsl:attribute>	

		<callback onInit="initialize"/>

		<property>
			<xsl:attribute name="name">UPnP.service.type</xsl:attribute>	
			<xsl:attribute name="type">string</xsl:attribute>	
			<xsl:attribute name="value"><xsl:value-of select='$serviceType'/></xsl:attribute>
		</property>
		
		<definition name="UPnP.device.UDN" type="string" />
		<definition name="UPnP.service.id" type="string" />
		
		<ipojo:provides specifications="org.osgi.service.upnp.UPnPEventListener">
			<ipojo:property name="upnp.filter" field="upnpEventFilter" type="org.osgi.framework.Filter" mandatory="true"/>
		</ipojo:provides>
		
		<ipojo:requires id="UPnP.device.UDN" field="upnpDevice" optional="false"/>
		
	</implementation>
	
</xsl:template>

<xsl:template match="*"></xsl:template>

</xsl:stylesheet>

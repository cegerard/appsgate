<?xml version="1.0"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="1.0" 
    xmlns:table="xalan://appsgate.lig.upnp.generator.util.StateVariableTable">

<xsl:import href="scdp2java.xsl"/>

<xsl:param name="servicetype"/>
<xsl:param name="package"/>
<xsl:param name="classname"/>

<xsl:param name="serviceId"/>

<xsl:output method = "text"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/">

package <xsl:value-of select="$package"/>;

import org.apache.felix.upnp.devicegen.holder.*;
import org.osgi.service.upnp.UPnPException;

<xsl:apply-templates select="*"/>
	
</xsl:template>

<xsl:template match="scpd">
public interface <xsl:value-of select="$classname"/> {	

<xsl:apply-templates select="serviceStateTable/stateVariable" mode="getterDeclaration"/>

<xsl:apply-templates select="actionList/action"  mode="actionDeclaration"/>

}
</xsl:template>

<xsl:template match="stateVariable[starts-with(name,'A_ARG') or sendEventsAttribute='no']" mode="getterDeclaration">
	<xsl:apply-templates select="." mode="register"/>
</xsl:template>
	
<xsl:template match="stateVariable" mode="getterDeclaration">
	<xsl:apply-templates select="." mode="register"/>
<xsl:text>		</xsl:text><xsl:apply-templates select="." mode="getterSignature"/><xsl:text>;

</xsl:text>
</xsl:template>


<xsl:template match="action"  mode="actionDeclaration">
<xsl:text>		</xsl:text><xsl:apply-templates select="." mode="methodSignature"/><xsl:text>;

</xsl:text>
</xsl:template>

<xsl:template match="*"></xsl:template>

</xsl:stylesheet>

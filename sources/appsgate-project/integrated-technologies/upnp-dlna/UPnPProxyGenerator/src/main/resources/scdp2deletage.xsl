<?xml version="1.0"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="1.0"
    xmlns:table="xalan://appsgate.lig.upnp.generator.util.StateVariableTable"
    xmlns:naming="xalan://appsgate.lig.upnp.generator.util.Naming">

<xsl:import href="scdp2java.xsl"/>

<xsl:param name="servicetype"/>
<xsl:param name="serviceId"/>

<xsl:param name="interfacePackage"/>
<xsl:param name="interfaceName"/>

<xsl:output method = "text"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/">
	<xsl:apply-templates select="*"/>
</xsl:template>

<xsl:template match="scpd">
	<xsl:apply-templates select="serviceStateTable/stateVariable" mode="register"/>
	
	private <xsl:value-of select="$interfaceName"/><xsl:text> </xsl:text><xsl:value-of select="naming:getField($serviceId)"/>;

	public <xsl:value-of select="$interfaceName"/><xsl:text> </xsl:text><xsl:value-of select="naming:getGetter($serviceId)"/>() { return  <xsl:value-of select="naming:getField($serviceId)"/>; };
	<xsl:apply-templates select="actionList/action"  mode="actionDefinition"/>	
</xsl:template>

<xsl:template match="action"  mode="actionDefinition">
	<xsl:text>
	</xsl:text>
	<xsl:apply-templates select="." mode="methodSignature"/> {
		<xsl:apply-templates select="." mode="delegate"/>
	}
</xsl:template>

<xsl:template match="action"  mode="delegate">
	<xsl:value-of select="naming:getField($serviceId)"/>
	<xsl:text>.</xsl:text>
	<xsl:value-of select="naming:getMethod('',./name)"/>
	<xsl:text>(</xsl:text>
	<xsl:apply-templates select="argumentList" mode="invoke"/>
	<xsl:text>);</xsl:text>
</xsl:template>

<xsl:template match="*"></xsl:template>

</xsl:stylesheet>

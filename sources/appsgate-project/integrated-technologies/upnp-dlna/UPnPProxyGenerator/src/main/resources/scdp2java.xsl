<?xml version="1.0"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="1.0" 
    xmlns:table="xalan://appsgate.lig.upnp.generator.util.StateVariableTable"
    xmlns:naming="xalan://appsgate.lig.upnp.generator.util.Naming">
     
<!-- parameter declaration -->
<xsl:param name="serviceType"/>
<xsl:param name="serviceId"/>

<xsl:variable name="stateVariables" select="table:new(string($serviceType))"/>

<xsl:template match="stateVariable" mode="register">
	<xsl:value-of select="table:register($stateVariables,./name,./dataType)"/>
</xsl:template>

<xsl:template match="stateVariable" mode="getterSignature">
	<xsl:text>public </xsl:text>
	<xsl:value-of select="table:getType($stateVariables,./name)"/>
	<xsl:text> </xsl:text>
	<xsl:value-of select="naming:getGetter($serviceId,./name)"/>
	<xsl:text>()</xsl:text>
</xsl:template>

<xsl:template match="stateVariable" mode="fieldDeclaration">
	<xsl:text>private </xsl:text>
	<xsl:value-of select="table:getType($stateVariables,./name)"/>
	<xsl:text> </xsl:text>
	<xsl:value-of select="naming:getField($serviceId,./name)"/>
	<xsl:text>;</xsl:text>
</xsl:template>

<xsl:template match="action"  mode="methodSignature">
	<xsl:text>public void </xsl:text>
	<xsl:value-of select="naming:getMethod($serviceId,./name)"/>
	<xsl:text>(</xsl:text>
	<xsl:apply-templates select="argumentList" mode="argument"/>
	<xsl:text>)</xsl:text>
	<xsl:text>throws UPnPException</xsl:text>
</xsl:template>

<xsl:template match="argumentList" mode="argument">
	<xsl:apply-templates select="argument" mode="withSeparator"/>
</xsl:template>

<xsl:template match="argument" mode="withSeparator">
	<xsl:apply-templates select="." mode="argument"/><xsl:text>,</xsl:text>
</xsl:template>

<xsl:template match="argument[position()=last()]" mode="withSeparator">
	<xsl:apply-templates select="." mode="argument"/>
</xsl:template>
	
<xsl:template match="argument[direction='in']" mode="argument">
	<xsl:value-of select="table:getInputParameterType($stateVariables,./relatedStateVariable)"/><xsl:text> </xsl:text><xsl:value-of select="naming:getArgument(./name)"/>
</xsl:template>

<xsl:template match="argument[direction='out']" mode="argument">
	<xsl:value-of select="table:getOutputParameterType($stateVariables,./relatedStateVariable)"/><xsl:text> </xsl:text><xsl:value-of select="naming:getArgument(./name)"/>
</xsl:template>

<xsl:template match="argumentList" mode="invoke">
	<xsl:apply-templates select="argument" mode="invokeWithSeparator"/>
</xsl:template>

<xsl:template match="argument" mode="invokeWithSeparator">
	<xsl:apply-templates select="." mode="invoke"/><xsl:text>,</xsl:text>
</xsl:template>

<xsl:template match="argument[position()=last()]" mode="invokeWithSeparator">
	<xsl:apply-templates select="." mode="invoke"/>
</xsl:template>
	
<xsl:template match="argument" mode="invoke">
	<xsl:value-of select="naming:getArgument(./name)"/>
</xsl:template>

</xsl:stylesheet>

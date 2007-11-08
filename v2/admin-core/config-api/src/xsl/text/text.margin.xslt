<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:str="http://www.ora.com/XSLTCookbook/namespaces/strings"
  xmlns:text="http://www.ora.com/XSLTCookbook/namespaces/text" extension-element-prefixes="text">

<xsl:include href="../strings/str.dup.xslt"/>

<xsl:template name="text:justify">
  <xsl:param name="value" />
  <xsl:param name="margin" select="0"/>
  <xsl:param name="width" select="10"/>
  <xsl:param name="align" select=" 'left' "/>

  <!-- Truncate if too long -->  
  <xsl:variable name="output" select="substring($value,1,$width - $margin)"/>
  
  <xsl:choose>
    <xsl:when test="$align = 'left'">
      <xsl:call-template name="str:dup">
        <xsl:with-param name="input" select=" ' ' "/>
        <xsl:with-param name="count" select="$margin"/>
      </xsl:call-template>
      <xsl:value-of disable-output-escaping="yes" select="$output"/>
      <xsl:call-template name="str:dup">
        <xsl:with-param name="input" select=" ' ' "/>
        <xsl:with-param name="count" select="$width - $margin - string-length($output)"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="$align = 'right'">
      <xsl:call-template name="str:dup">
        <xsl:with-param name="input" select=" ' ' "/>
        <xsl:with-param name="count" select="$width - $margin - string-length($output)"/>
      </xsl:call-template>
      <xsl:value-of select="$output"/>
      <xsl:call-template name="str:dup">
        <xsl:with-param name="input" select=" ' ' "/>
        <xsl:with-param name="count" select="$margin"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="$align = 'center'">
      <xsl:call-template name="str:dup">
        <xsl:with-param name="input" select=" ' ' "/>
        <xsl:with-param name="count" select="floor(($width - string-length($output)) div 2)"/>
      </xsl:call-template>
      <xsl:value-of select="$output"/>
      <xsl:call-template name="str:dup">
        <xsl:with-param name="input" select=" ' ' "/>
        <xsl:with-param name="count" select="ceiling(($width - string-length($output)) div 2)"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>INVALID ALIGNMENT VALUE</xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>

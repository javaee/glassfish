<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  xmlns:text="http://www.ora.com/XSLTCookbook/namespaces/text" exclude-result-prefixes="text">


  <xsl:import href="../text/text.wrap.xslt"/>
  <xsl:template name="make-entity">
    <xsl:param name="name"/>
    <xsl:param name="value"/>
    <xsl:variable name="quoted-value">
      <xsl:text>"</xsl:text><xsl:value-of select="$value"/><xsl:text disable-output-escaping="yes">"></xsl:text>
    </xsl:variable>
    <xsl:if test="not($name)">
      <xsl:message terminate="yes">
        make-entity - no name supplied
      </xsl:message>
    </xsl:if>
    <xsl:if test="not($value)">
      <xsl:message terminate="yes">
        make-entity for <xsl:value-of select="$name"/> - no value supplied
      </xsl:message>
    </xsl:if>
    <xsl:variable name="prefix">&lt;!ENTITY % <xsl:value-of select="$name"/></xsl:variable>
<!--     <xsl:text disable-output-escaping="yes">&lt;!ENTITY % </xsl:text> -->
<!--     <xsl:value-of select="$name"/> -->
<!--     <xsl:text> "</xsl:text> -->
<!--     <xsl:message><xsl:value-of select="$value"/></xsl:message> -->

    <xsl:variable name="all"><xsl:value-of select="$prefix"/><xsl:text xml:space="preserve"> </xsl:text><xsl:value-of select="$quoted-value"/></xsl:variable>
    <xsl:choose>
      <xsl:when test="string-length($all) > 79">
        <xsl:value-of disable-output-escaping="yes" select="$prefix"/>
        <xsl:text>&#10;</xsl:text>
        <xsl:call-template name="text:wrap">
          <xsl:with-param name="input" select="$quoted-value"/>
          <xsl:with-param name="margin" select="4"/>
          <xsl:with-param name="width" select="79"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of disable-output-escaping="yes" select="$all"/>
      </xsl:otherwise>
    </xsl:choose>
<!--     <xsl:value-of select="$prefix"/> -->
<!--     <xsl:value-of select="$value"/> -->
<!--     <xsl:text disable-output-escaping="yes">" &gt;</xsl:text> -->
  </xsl:template>


</xsl:stylesheet>

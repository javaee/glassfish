<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  xmlns:text="http://www.ora.com/XSLTCookbook/namespaces/text" exclude-result-prefixes="text"
  >

  <xsl:import href="../text/text.wrap.xslt"/>
  <xsl:template name="make-element">
    <xsl:param name="name"/>
    <xsl:param name="contents">EMPTY</xsl:param>
    <xsl:param name="adjusted-contents">
      <xsl:choose>
        <xsl:when test="string-length($contents) = 0">
          <xsl:text>EMPTY</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$contents"/>
        </xsl:otherwise>
      </xsl:choose><xsl:text disable-output-escaping="yes">></xsl:text>
    </xsl:param>

    <xsl:if test="not($name)">
      <xsl:message terminate="yes">
        make-element: No name provided
      </xsl:message>
    </xsl:if>
    <xsl:variable name="prefix">&lt;!ELEMENT <xsl:value-of select="$name"/></xsl:variable>
    <xsl:variable name="all"><xsl:value-of select="normalize-space($prefix)"/>
      <xsl:text xml:space="preserve"> </xsl:text>
      <xsl:value-of select="normalize-space($adjusted-contents)"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string-length($all) > 79">
        <xsl:value-of disable-output-escaping="yes" select="$prefix"/>
        <xsl:text>&#10;</xsl:text>
          <xsl:call-template name="text:wrap">
            <xsl:with-param name="input" select="$adjusted-contents"/>
            <xsl:with-param name="margin" select="4"/>
            <xsl:with-param name="width" select="79"/>
          </xsl:call-template>
<!--         <xsl:value-of disable-output-escaping="yes" select="$body"/> -->
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of disable-output-escaping="yes" select="$all"/>
      </xsl:otherwise>
    </xsl:choose>
<!--     <xsl:value-of select="$prefix"/> -->
<!--     <xsl:text xml:space="preserve"> </xsl:text> -->
<!--     <xsl:value-of select="$adjusted-contents"/> -->
<!--     <xsl:text disable-output-escaping="yes">&gt;</xsl:text> -->
  </xsl:template>
</xsl:stylesheet>

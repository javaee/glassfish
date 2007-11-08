<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:variable name="lower">abcdefghijklmnopqrstuvwxyz</xsl:variable>
  <xsl:variable name="upper">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
  <xsl:template name="camel-case">
    <xsl:param name="input"/>
    <xsl:if test="$input">
      <xsl:choose>
	<xsl:when test='contains($input, "-")'>
	  <xsl:call-template name="capitalize">
	    <xsl:with-param name="input" select='substring-before($input, "-")'/>
	  </xsl:call-template>
	  <xsl:call-template name="camel-case">
	    <xsl:with-param name="input" select='substring-after($input, "-")'/>
	  </xsl:call-template>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:call-template name="capitalize">
	    <xsl:with-param name="input" select="$input"/>
	  </xsl:call-template>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template name="capitalize">
    <xsl:param name="input"/>
    <xsl:if test="$input">
      <xsl:value-of select="concat(translate(substring($input,1,1), $lower, $upper), substring($input,2))"/>	  
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>
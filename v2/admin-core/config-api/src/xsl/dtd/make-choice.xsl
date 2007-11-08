<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:import href="make-cp.xsl"/>

  <xsl:template name="make-choice">
    <xsl:param name="values"/>
    <xsl:param name="noParens"/>
    <xsl:if test="not($noParens)">
      <xsl:text>(</xsl:text>
    </xsl:if>
    <xsl:call-template name="make-cp">
      <xsl:with-param name="values" select="$values"/>
      <xsl:with-param name="sep"> | </xsl:with-param>
      <xsl:with-param name="noParens" select="$noParens"/>
    </xsl:call-template>
    <xsl:if test="not($noParens)">
      <xsl:text>)</xsl:text>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>

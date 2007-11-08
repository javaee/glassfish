<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:import href="make-cp.xsl"/>
  <xsl:template name="make-seq">
    <xsl:param name="values"/>
<!--     <xsl:variable name="content"> -->
<!--       <xsl:call-template name="make-cp"> -->
<!--         <xsl:with-param name="values" select="$values"/> -->
<!--         <xsl:with-param name="sep">,</xsl:with-param> -->
<!--       </xsl:call-template> -->
<!--     </xsl:variable> -->
<!--     <xsl:choose> -->
<!--       <xsl:when test="not(starts-with(normalize-space($content), '('))"> -->
        <xsl:text>(</xsl:text>
<!--         <xsl:value-of select="$content"/> -->
        <xsl:call-template name="make-cp">
          <xsl:with-param name="values" select="$values"/>
          <xsl:with-param name="sep">, </xsl:with-param>
        </xsl:call-template>
        <xsl:text>)</xsl:text>
  </xsl:template>
</xsl:stylesheet>

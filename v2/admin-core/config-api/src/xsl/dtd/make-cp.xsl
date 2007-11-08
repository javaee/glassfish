<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template name="make-cp">
    <xsl:param name="values"/>
    <xsl:param name="sep"/>
    <xsl:for-each select="$values">
      <xsl:if test="position() != 1"><xsl:value-of select="$sep"/></xsl:if>
      <xsl:apply-templates select="."/>
    </xsl:for-each>
  </xsl:template>


</xsl:stylesheet>

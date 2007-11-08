<?xml version="1.0"?>
<!-- This stylesheet copies its input to its output, omitting any
elements which aren't part of the schematron namespace -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns:sch="http://www.ascc.net/xml/schematron">

  <xsl:output method="xml" indent="yes"/>
  <xsl:strip-space elements="*"/>
  <xsl:template match="sch:*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="code">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="*"/><!-- swallow -->

</xsl:stylesheet>

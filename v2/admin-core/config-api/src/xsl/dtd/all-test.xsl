<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:t="test">

  <xsl:import href="make-choice-test.xsl"/>
  <xsl:import href="make-element-test.xsl"/>
  <xsl:import href="make-entity-test.xsl"/>
  <xsl:import href="make-seq-test.xsl"/>
  <xsl:import href="test-driver.xsl"/>

  <xsl:template match="/">
    <xsl:call-template name="executeTests"/>
  </xsl:template>

</xsl:stylesheet>

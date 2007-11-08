<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:t="http:///xsl-tests" >
  <xsl:param name="test_dir">tests</xsl:param>
  <xsl:template name="getTestFileName">
    <xsl:param name="test"/>
    <xsl:if test="not($test)">
      <xsl:message terminate="yes">
        getTestFileName() - no test parameter
      </xsl:message>
    </xsl:if>
    <xsl:if test="count($test) &gt; 1">
      <xsl:message terminate="no">
        getTestFileName() - recieved <xsl:value-of select="count($test)"/> test nodes only processing $test[1]
      </xsl:message>
    </xsl:if>
    <xsl:value-of select="concat($test_dir, '/', @ruleId, '_', @assertionId, '_', @id, '.xml')"/>
  </xsl:template>
</xsl:stylesheet>

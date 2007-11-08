<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:t="http:///xsl-tests" >
  <xsl:template name="getTestFileName">
    <xsl:param name="test"/>
    <xsl:param name="test.dir"/>
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
    <xsl:variable name="file_name" select="concat(@ruleId, '_', @assertionId, '_', @id, '.xml')"/>
    <xsl:choose>
      <xsl:when test="$test.dir">
	<xsl:value-of select="concat($test.dir, '/', $file_name)"/>	
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="$file_name"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>

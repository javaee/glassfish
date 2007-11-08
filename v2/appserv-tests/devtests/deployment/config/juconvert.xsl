<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>
<xsl:template match="/">
<xsl:for-each select="testsuites/testsuite">  
<xsl:choose>
<xsl:when test="@errors + @failures = 0">
[PASSED] </xsl:when>
<xsl:otherwise>
[FAILED] </xsl:otherwise>
</xsl:choose>
 <xsl:value-of select="properties/property[@name='ant.project.name']/@value"/> : <xsl:for-each select="testcase">
<xsl:value-of select="@name"/>, </xsl:for-each>

</xsl:for-each>

</xsl:template>

</xsl:stylesheet>

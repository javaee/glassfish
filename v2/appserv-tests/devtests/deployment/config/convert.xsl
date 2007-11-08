<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:template match="/">
<xsl:for-each select="tests/test">  
[<xsl:value-of select="result/@status"/>] <xsl:value-of select="@name"/> : <xsl:value-of select="@description"/>
</xsl:for-each>

</xsl:template>

</xsl:stylesheet>

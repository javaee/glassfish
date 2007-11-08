<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
Configuration Validation - Result 
*********************************
<xsl:for-each select="elements/element">
Element Name	:	<xsl:value-of select="name"/>
Key-value	:	<xsl:value-of select="key"/>		
Failed          :	<xsl:value-of select="failed"/>	

-------------------------------------------------------

</xsl:for-each>
<xsl:for-each select="elements">
<xsl:value-of select="description"/> 
</xsl:for-each>
***************END**************
</xsl:template>
</xsl:stylesheet>

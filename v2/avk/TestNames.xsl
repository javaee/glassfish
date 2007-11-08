<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <xsl:element name="tests">
    <!-- add the description element and it's child to the output  -->
            <xsl:element name="description">
                <xsl:value-of select="tests/description"/>
            </xsl:element>
    <!-- loop for every test in the xml -->
            <xsl:for-each select="tests/test">
                <xsl:choose>
                    <xsl:when test = "avk">
        <!-- if avk tag is found then do nothing -->
                    </xsl:when>
                    <xsl:otherwise>
        <!-- if avk tag is not found then add this test to the output -->
                        <xsl:copy-of select="."/>
                    </xsl:otherwise>
                </xsl:choose>  
            </xsl:for-each> 
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>


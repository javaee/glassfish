<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="text.wrap"
  xmlns:str="http://www.ora.com/XSLTCookbook/namespaces/strings" 
  xmlns:text="http://www.ora.com/XSLTCookbook/namespaces/text" exclude-result-prefixes="text">

<xsl:include href="../strings/str.find-last.xslt"/>
<!-- <xsl:include href="text.justify.xslt"/> -->
<xsl:include href="text.margin.xslt"/>

<!--
 input - the text to be input - defaults to current node
 width - the width of the output text
 align-width - the right hand margin position, counting from the left.
 align - left, right, center
 margin - offset from left hand edge before any of this stuff happens
-->
<xsl:template match="node() | @*" mode="text:wrap" name="text:wrap">
  <xsl:param name="input" select="normalize-space()"/> 
  <xsl:param name="width" select="70"/>
  <xsl:param name="align-width" select="$width"/>
  <xsl:param name="align" select=" 'left' "/>
  <xsl:param name="margin" select="number(0)"/>

  <xsl:variable name="effective-width" select="$width - $margin"/>
  <xsl:if test="$effective-width &lt;=0">
    <xsl:message terminate="yes">width - margin is too small: <xsl:value-of select="$effective-width"/></xsl:message>
  </xsl:if>

  <xsl:if test="$align-width &lt; $width">
    <xsl:message terminate="yes">align-width (<xsl:value-of select="$align-width"/>) is less than width (<xsl:value-of select="$width"/>)</xsl:message>
  </xsl:if>
  
  <xsl:if test="$input">
    <xsl:variable name="line">
      <xsl:choose>
        <xsl:when test="string-length($input) > $effective-width">
          <xsl:variable name="candidate-line" select="substring($input,1,$effective-width)"/>
          <xsl:choose>
            <xsl:when test="contains($candidate-line,' ')">
              <xsl:call-template name="str:substring-before-last">
                  <xsl:with-param name="input" select="$candidate-line"/>
                  <xsl:with-param name="substr" select=" ' ' "/>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$candidate-line"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$input"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
  
    <xsl:if test="$line">
      <xsl:call-template name="text:justify">
        <xsl:with-param name="value" select="$line"/>
        <xsl:with-param name="width" select="$align-width"/>
        <xsl:with-param name="align" select="$align"/>
        <xsl:with-param name="margin" select="$margin"/>
      </xsl:call-template>
      <xsl:text>&#xa;</xsl:text>
    </xsl:if>  

    <xsl:call-template name="text:wrap">
      <xsl:with-param name="input" select="substring($input, string-length($line) + 2)"/>
      <xsl:with-param name="width" select="$width"/>
      <xsl:with-param name="align-width" select="$align-width"/>
      <xsl:with-param name="align" select="$align"/>
      <xsl:with-param name="margin" select="$margin"/>
    </xsl:call-template>
  </xsl:if>  
  
</xsl:template>


</xsl:stylesheet>

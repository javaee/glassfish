<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:sch="http://www.ascc.net/xml/schematron" xmlns:t="http:///xsl-tests">

  <xsl:output method="text"/>

  <xsl:variable name="pk">-IsPrimaryKey</xsl:variable>
  
<!--   <xsl:template match="sch:extends"> -->
<!--     <xsl:if test="substring(@rule, (string-length(@rule) - string-length($pk)) + 1) = $pk"> -->
<!--       <xsl:value-of select="ancestor::sch:rule/@context"/>=<xsl:value-of select="substring-before(@rule, $pk)"/> -->
<!--       <xsl:text>&#10;</xsl:text> -->
<!--     </xsl:if> -->
<!--   </xsl:template> -->

<!--   <xsl:template match="sch:assert"> -->
<!--     <xsl:if test="substring(@id, (string-length(@id) - string-length($pk)) + 1) = $pk"> -->
<!--       <xsl:value-of select="ancestor::sch:rule/@context"/>=<xsl:value-of select="substring-before(@id, $pk)"/> -->
<!--       <xsl:text>&#10;</xsl:text> -->
<!--     </xsl:if> -->
<!--   </xsl:template> -->

  <xsl:template match="sch:extends">
    <xsl:if test="substring(@rule, (string-length(@rule) - string-length($pk)) + 1) = $pk">
      <xsl:call-template name="make-property">
	<xsl:with-param name="element" select="ancestor::sch:rule/@context"/>
	<xsl:with-param name='pk' select="substring-before(@rule, $pk)"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template match="sch:assert">
    <xsl:if test="substring(@id, (string-length(@id) - string-length($pk)) + 1) = $pk">
      <xsl:call-template name="make-property">
	<xsl:with-param name="element" select="ancestor::sch:rule/@context"/>
	<xsl:with-param name='pk' select="substring-before(@id, $pk)"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <xsl:template name="make-property">
    <xsl:param name="element"/>
    <xsl:param name="pk"/>
    <xsl:variable name="output-element">
      <xsl:choose>
	<xsl:when test="$element='property'">element-property</xsl:when>
	<xsl:otherwise><xsl:value-of select="$element"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="$output-element"/>=<xsl:value-of select="$pk"/>
    <xsl:text>&#10;</xsl:text>
  </xsl:template>
  

  <!-- need to convert property to element-property -->

  <xsl:template match="sch:*">
    <xsl:apply-templates select="sch:*"/>
  </xsl:template>
  
  <xsl:template match="t:* | comment()"/>
</xsl:stylesheet>
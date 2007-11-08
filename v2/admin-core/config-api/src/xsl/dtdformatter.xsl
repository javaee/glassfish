<?xml version="1.0"?>
<!--
XSL replacement for the dtdformatter stuff done in java

It generates an rng file which:

1) Has no rng:define[rng:data] | rng:define[rng:choice] - this ensures
no entities are generated.

2) Has the element name property renamed to element-property

3) Has the element name system-element-property renamed to
system-property

4) Makes every attribute a text attribute, not a data attribute

Note that we make use of the fact that all names are unique!
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		version="1.0"
		xmlns:rng="http://relaxng.org/ns/structure/1.0"
		xmlns:text="http://www.ora.com/XSLTCookbook/namespaces/text" xmlns:x="http://www.w3.org/1999/xhtml"
		>

  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="* | rng:* | text:* | x:* ">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*">
	<xsl:copy/>
  </xsl:template>

  <!-- rename these elements and anything that refers to them -->
  <xsl:template match="@name[(parent::rng:element or parent::rng:define
		or parent::rng:ref) and (.='property' or .='property-attlist')]">
      <xsl:attribute name="name"><xsl:value-of select="concat('element-',.)"/></xsl:attribute>
  </xsl:template>


  <!-- don't include these in the output -->
  <!-- these make entities -->
  <xsl:template match="rng:define[rng:data] |
		rng:define[rng:choice]"/>

  <!-- these make attributes have non CDATA types -->
  <xsl:template match="rng:attribute//rng:ref |
		rng:attribute//rng:data | rng:attribute//rng:choice"/>




<!--
  <xsl:template match="@name">
      <xsl:choose>
        <xsl:when test="(parent::rng:element or parent::rng:define
		or parent::rng:ref) and .='property'">
          <xsl:attribute name="name">element-property</xsl:attribute>
        </xsl:when>
        <xsl:when test="(parent::rng:element or parent::rng:define
		or parent::rng:ref) and .='property-attlist'">
          <xsl:attribute name="name">element-property-attlist</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy/>
        </xsl:otherwise>
      </xsl:choose>
  </xsl:template>
-->

</xsl:stylesheet>

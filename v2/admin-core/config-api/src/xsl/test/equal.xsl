<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- this template determines whether two nodes are lexically
  identical - containing the same content and attributes

  it returns a null string for false, and a non-null string for true -->
  <xsl:template name="equal">
    <xsl:param name="node1"/>
    <xsl:param name="node2"/>
    <xsl:variable name="same-attrs">
      <xsl:call-template name="same-attr-names">
	<xsl:with-param name="a1" select="$node1/@*"/>
	<xsl:with-param name="a2" select="$node2/@*"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="count($node1) != count($node2)"/>
      <xsl:otherwise>
	<xsl:choose>
	  <xsl:when test="count($node1) = 1">
	    <xsl:call-template name='same-attrs'>
	      <xsl:with-param name="a1" select="$node1/@*"/>
	      <xsl:with-param name="a2" select="$node2/@*"/>
	    </xsl:call-template>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:variable name="first-pair">
	      <xsl:call-template name="equal">
		<xsl:with-param name="node1" select="$node1[1]"/>
		<xsl:with-param name="node2" select="$node2[1]"/>
	      </xsl:call-template>
	    </xsl:variable>
	    <xsl:variable name="rest">
	      <xsl:call-template name="equal">
		<xsl:with-param name="node1" select="$node1[position() != 1]"/>
		<xsl:with-param name="node2" select="$node2[position() != 1]"/>
	      </xsl:call-template>
	    </xsl:variable>
	    <xsl:value-of select="normalize-space($first-pair) and normalize-space($rest)"/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="same-attrs">
    <xsl:param name="a1"/>
    <xsl:param name="a2"/>
    <xsl:choose>
      <xsl:when test="count($a1) != count($a2)"/>
      <xsl:when test="count($a1) = 0">
	<xsl:value-of select="true()"/>
      </xsl:when>
      <xsl:when test="count($a1) = 1">
	<xsl:if test="$a1 = $a2 and name($a1) = name($a2)">
	  <xsl:value-of select="true()"/>
	</xsl:if>
      </xsl:when>
      <xsl:otherwise>
	<xsl:variable name="first-pair">
	  <xsl:call-template name="same-attrs">
	    <xsl:with-param name="a1" select="$a1[1]"/>
	    <xsl:with-param name="a2" select="$a2[1]"/>
	  </xsl:call-template>
	</xsl:variable>
	<xsl:variable name="rest">
	  <xsl:call-template name="same-attrs">
	    <xsl:with-param name="a1" select="$a1[position() != 1]"/>
	    <xsl:with-param name="a2" select="$a2[name(.) != name($a1[1])]"/>
	  </xsl:call-template>
	</xsl:variable>
	<xsl:value-of select="normalize-space($first-pair) and normalize-space($rest)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="same-attr-names">
    <xsl:param name="a1"/>
    <xsl:param name="a2"/>
    <xsl:choose>
      <xsl:when test="count($a1) != count($a2)"/>
      <xsl:when test="count($a1) = 0">
	<xsl:value-of select="true()"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:call-template  name="same-attr-names">
	  <xsl:with-param name="a1" select="$a1[position() != 1]"/>
	  <xsl:with-param name="a2" select="$a2[name(.) != name($a1[1])]"/>
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>


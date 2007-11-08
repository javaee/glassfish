<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:t="test"
		xmlns:m="messages"
		xmlns:sch="http://www.ascc.net/xml/schematron"
		exclude-result-prefixes="xsl t sch">
  <xsl:output omit-xml-declaration="yes" method="xml" indent="yes"></xsl:output>

  <xsl:include href="equal.xsl"/>
  <!--

  This stylesheet provides a limited unit test facility. It prints a
  '.' character for each test where the expected equals the actual
  results, and prints text describing the difference between expected
  and actual results differ.

  This works by having test templates to be run match a specific name
  (their name attribute) - this way the driving template can select
  them and drive through the collection.
  -->
  <xsl:template match="/">
    <xsl:call-template name="executeTests"/>
  </xsl:template>

  <xsl:template name="executeTests">
    <xsl:for-each select="document('')/*/t:test/@name">
      <xsl:call-template name="executeTest">
        <xsl:with-param name="testName" select="."></xsl:with-param>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="executeTest">
    <xsl:param name="testName"/>
    <xsl:if test="$testName">
      <xsl:text>.</xsl:text>
      <xsl:apply-templates select="document('')/*/t:test[@name=$testName]"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="t:assertEquals">
    <xsl:param name="expected"/>
    <xsl:param name="actual"/>
    <xsl:variable name="result">
      <xsl:call-template name="equal">
	<xsl:with-param name="node1" select="$expected"/>
	<xsl:with-param name="node2" select="$actual"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:message>result: <xsl:value-of select="$result"/></xsl:message>
    <xsl:choose>
      <xsl:when test="$result"/>
      <xsl:otherwise>
        <xsl:text>&#xa;test:</xsl:text>
        <xsl:value-of select="./@name"/>
	<xsl:text>Assertion failed: Expected:
	</xsl:text>
	<xsl:copy-of select="$expected"/>
	<xsl:text>
Actual:
	</xsl:text>
	<xsl:copy-of select="$actual"/>
      </xsl:otherwise>
<!--         <xsl:text> Assertion failed. Expected: -->
<!-- </xsl:text>"<xsl:value-of disable-output-escaping="yes" select="$expected"></xsl:value-of>"<xsl:text> -->
<!-- actual:&#10;"</xsl:text> -->
<!-- <xsl:value-of disable-output-escaping="yes" select="$actual"/>"<xsl:text>&#xa;</xsl:text> -->
<!--       </xsl:otherwise> -->
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>

<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns:sch="http://www.ascc.net/xml/schematron"
                xmlns:redirect="http://xml.apache.org/xalan/redirect"
                xmlns:t="http:///xsl-tests"
                extension-element-prefixes="redirect">
  <xsl:output method="xml" indent="yes"/>
  <xsl:include href="testName.xsl"/>
  <xsl:param name="test_dir">tests</xsl:param>

  <xsl:template match="/">
    <!--
    Note - this expression is same as in
    schematron-test2.xsl::process-root and needs to be sychronized
    with it -->
    <xsl:apply-templates select="//t:test[not(ancestor::sch:rule[@abstract='true'])]"/>
  </xsl:template>

  <xsl:template match="t:test">
    <xsl:variable name="testFileName">
      <xsl:call-template name="getTestFileName">
        <xsl:with-param name="test" select="."/>
      </xsl:call-template>
    </xsl:variable>
    <redirect:write select="$testFileName">
       <xsl:copy>
         <xsl:apply-templates select="@*" mode="copy"/>
         <xsl:if test="not(@expectedAssertions)">
           <xsl:attribute name="expectedAssertions">0</xsl:attribute>
         </xsl:if>
         <xsl:if test="not(@expectedNonAssertions)">
           <xsl:attribute name="expectedNonAssertions">0</xsl:attribute>
         </xsl:if>
         <xsl:text>&#10;</xsl:text>
         <xsl:copy-of select="*"/>
       </xsl:copy>
    </redirect:write>
  </xsl:template>

  <xsl:template match="@*" mode="copy">
    <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>
  
</xsl:stylesheet>

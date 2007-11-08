<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:t="test">

  <xsl:include href="../test/test-driver.xsl"/>
  <xsl:include href="../strings/camel-case.xsl"/>

  <t:test name="empty"></t:test>
  <xsl:template match="t:test[@name='empty']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected"></xsl:with-param>
      <xsl:with-param name="actual">
	<xsl:call-template name="camel-case">
	  <xsl:with-param name="input" select='document("")//t:test[@name="empty"]'/>
	</xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="simple"/>
  <xsl:template match="t:test[@name='simple']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">Simple</xsl:with-param>
      <xsl:with-param name="actual">
	<xsl:call-template name="camel-case">
	  <xsl:with-param name="input">simple</xsl:with-param>
	</xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="two-part"/>
  <xsl:template match="t:test[@name='simple']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">TwoPart</xsl:with-param>
      <xsl:with-param name="actual">
	<xsl:call-template name="camel-case">
	  <xsl:with-param name="input">two-part</xsl:with-param>
	</xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="multi-multi-multi-part"/>
  <xsl:template match="t:test[@name='simple']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">MultiMultiMultiPart</xsl:with-param>
      <xsl:with-param name="actual">
	<xsl:call-template name="camel-case">
	  <xsl:with-param name="input">multi-multi-multi-part</xsl:with-param>
	</xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>
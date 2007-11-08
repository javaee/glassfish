<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:t="test">

  <xsl:include href="../test/test-driver.xsl"/>
  <xsl:include href="../text/text.wrap.xslt"/>
  <xsl:include href="make-choice.xsl"/>

  <t:test name="empty"/>
  <xsl:template match="t:test[@name='empty']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">()</xsl:with-param>
      <xsl:with-param name="actual">
        <xsl:call-template name="make-choice">
          <xsl:with-param name="values" select="/.."/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="one">
    <t:data>one</t:data>
  </t:test>
  <xsl:template match="t:test[@name='one']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">(one)</xsl:with-param>
      <xsl:with-param name="actual">
        <xsl:call-template name="make-choice">
          <xsl:with-param name="values" select="document('')//t:test[@name='one']/t:data"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="two">
    <t:data>one</t:data>
    <t:data>two</t:data>
  </t:test>
  <xsl:template match="t:test[@name='two']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">(one | two)</xsl:with-param>
      <xsl:with-param name="actual">
        <xsl:call-template name="make-choice">
          <xsl:with-param name="values" select="document('')//t:test[@name='two']/t:data"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="many">
    <t:data>one</t:data>
    <t:data>two</t:data>
    <t:data>one</t:data>
    <t:data>two</t:data>
    <t:data>one</t:data>
    <t:data>two</t:data>
  </t:test>
  <xsl:template match="t:test[@name='many']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">(one | two | one | two | one | two)</xsl:with-param>
      <xsl:with-param name="actual">
        <xsl:call-template name="make-choice">
          <xsl:with-param name="values" select="document('')//t:test[@name='many']/t:data"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="noParens">
    <t:data>one</t:data>
    <t:data>two</t:data>
    <t:data>three</t:data>
  </t:test>
  <xsl:template match="t:test[@name='noParens']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">one | two | three</xsl:with-param>
      <xsl:with-param name="actual">
        <xsl:call-template name="make-choice">
          <xsl:with-param name="values" select="document('')//t:test[@name='noParens']/t:data"/>
          <xsl:with-param name="noParens">true</xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>

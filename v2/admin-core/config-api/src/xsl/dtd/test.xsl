<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:t="test">

  <xsl:include href="../test/test-driver.xsl"/>
  <xsl:include href="make-entity.xsl"/>

  <t:test-data>
    <foo>1</foo>
    <foo>2</foo>
    <foo>3</foo>
  </t:test-data>
  <t:test name="test1"/>
  <xsl:template match="t:test[@name='test1']">
    <xsl:variable name="actual">
      <xsl:call-template name="make-entity">
        <xsl:with-param name="name" select="'foo'"/>
        <xsl:with-param name="values" select="document('')//foo">
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="expected">&lt;!ENTITY % foo "1|2|3" ></xsl:variable>

    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected" select="$expected"/>
      <xsl:with-param name="actual" select="$actual"/>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>

<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:t="test">

  <xsl:include href="../test/test-driver.xsl"/>
  <xsl:include href="make-entity.xsl"/>
  <xsl:include href="make-choice.xsl"/>


  <t:test name="simple">
    <foo>1</foo>
    <foo>2</foo>
    <foo>3</foo>
  </t:test>
  <xsl:template match="t:test[@name='simple']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">&lt;!ENTITY % e "1 | 2 | 3"&gt;</xsl:with-param>
      <xsl:with-param name="actual">
        <xsl:call-template name="make-entity">
          <xsl:with-param name="name" select="'e'"/>
          <xsl:with-param name="value">
            <xsl:call-template name="make-choice">
              <xsl:with-param name="values" select="document('')//t:test[@name='simple']/foo"/>
              <xsl:with-param name="noParens">1</xsl:with-param>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="wide">
    <e>read-uncommitted</e>
    <e>read-committed</e>
    <e>repeatable-read</e>
    <e>serializable</e>
    <e>something-equally-long-and-difficult-to-split</e>
  </t:test>
  <xsl:template match="t:test[@name='wide']">
    <xsl:variable name="value">
      <xsl:call-template name="make-choice">
        <xsl:with-param name="values" select="document('')//t:test[@name='wide']/e"/>
        <xsl:with-param name="noParens">1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">&lt;!ENTITY % e
    "read-uncommitted | read-committed | repeatable-read | serializable |      
    something-equally-long-and-difficult-to-split"&gt;                            
</xsl:with-param>
      <xsl:with-param name="actual">
        <xsl:call-template name="make-entity">
          <xsl:with-param name="name" select="'e'"/>
          <xsl:with-param name="value">
            <xsl:call-template name="make-choice">
              <xsl:with-param name="values" select="document('')//t:test[@name='wide']/e"/>
              <xsl:with-param name="noParens">1</xsl:with-param>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  

      
</xsl:stylesheet>

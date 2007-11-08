<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:t="test">

  <xsl:include href="../test/test-driver.xsl"/>
  <xsl:include href="make-element.xsl"/>
  
  <t:test name="empty">
    <t:data><empty/></t:data>
  </t:test>
  <xsl:template match="t:test[@name='empty']">
    <xsl:variable name="expected">&lt;!ELEMENT foo EMPTY&gt;</xsl:variable>
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">&lt;!ELEMENT foo EMPTY&gt;</xsl:with-param>
      <xsl:with-param name="actual">
        <xsl:call-template name="make-element">
          <xsl:with-param name="name">foo</xsl:with-param>
<!--           <xsl:with-param name="contents" select="/.."/> -->
          <xsl:with-param name="contents" select="document('')//t:test[@name='empty']/t:data/*[local-name(.) != 'empty']"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="noDoubleParens"/>
  <xsl:template match="t:test[@name='noDoubleParens']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">&lt;!ELEMENT foo (a|b)*&gt;</xsl:with-param>
      <xsl:with-param name="actual">
        <xsl:call-template name="make-element">
          <xsl:with-param name="name">foo</xsl:with-param>
          <xsl:with-param name="contents">(a|b)*</xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="long">
    <t:data>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa bbbbbbbbbbbbbbbbbbbb cccccccccc dddddddddd eeeeeeeeee</t:data>
  </t:test>
  <xsl:template match="t:test[@name='long']">
    <xsl:call-template name="t:assertEquals">
      <!-- Watch out - the white spaces are significant! -->
      <xsl:with-param name="expected">&lt;!ELEMENT foo
    aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa bbbbbbbbbbbbbbbbbbbb cccccccccc dddddddddd  
    eeeeeeeeee&gt;                                                                
</xsl:with-param>
      <xsl:with-param name="actual">
        <xsl:call-template name="make-element">
          <xsl:with-param name="name">foo</xsl:with-param>
          <xsl:with-param name="contents" select="document('')//t:test[@name='long']/t:data"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>

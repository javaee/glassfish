<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:t="test">
  <xsl:include href="../test/test-driver.xsl"/>
  <xsl:include href="make-seq.xsl"/>
  <xsl:include href="make-choice.xsl"/>
  <xsl:strip-space elements="*"/>

  <t:test name="many">
    <foo>x</foo>
    <foo>y</foo>
    <foo>z</foo>
  </t:test>
  <xsl:template match="t:test[@name='many']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">(x, y, z)</xsl:with-param>
      <xsl:with-param name="actual">
        <xsl:call-template name="make-seq">
          <xsl:with-param name="values" select="document('')//foo"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

<!--   <t:test name="noParens"> -->
<!--     <t:data>x</t:data> -->
<!--     <t:data>y</t:data> -->
<!--   </t:test> -->
<!--   <xsl:template match="t:test[@name='noParens']"> -->
<!--     <xsl:call-template name="t:assertEquals"> -->
<!--       <xsl:with-param name="expected">(x|y)</xsl:with-param> -->
<!--       <xsl:with-param name="actual"> -->
<!--         <xsl:call-template name="make-seq"> -->
<!--           <xsl:with-param name="values"> -->
<!--             <xsl:call-template name="make-choice"> -->
<!--               <xsl:with-param name="values" select="document('')//t:test[@name='noParens']/t:data"/> -->
<!--             </xsl:call-template> -->
<!--           </xsl:with-param> -->
<!--         </xsl:call-template> -->
<!--       </xsl:with-param> -->
<!--     </xsl:call-template> -->
<!--   </xsl:template> -->
  

</xsl:stylesheet>

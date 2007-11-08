<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
		xmlns:sch="http://blah/foo"
		xmlns:t="test">

  <xsl:include href="../dtd/test-driver.xsl"/>
  <xsl:include href="equal.xsl"/>
  <t:test name="two-unequal-same-attrs-same-values">
    <sch:assert>foo bar <sch:name far="boo"/> far <sch:name far='boo'/></sch:assert>
  </t:test>
  <xsl:template match="t:test[@name='two-unequal-same-attrs-same-values']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected" select="true()"/>
      <xsl:with-param name="actual">
	<xsl:call-template name="equal">
	  <xsl:with-param name="node1" select="document('')//t:test[@name='two-unequal-same-attrs-same-values']/sch:assert/sch:name[1]"/>
	  <xsl:with-param name="node2" select="document('')//t:test[@name='two-unequal-same-attrs-same-values']/sch:assert/sch:name[2]"/>
	</xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="two-unequal-same-attrs-different-values">
    <sch:assert>foo bar <sch:name far="boo"/> far <sch:name far='bar'/></sch:assert>
  </t:test>
  <xsl:template match="t:test[@name='two-unequal-same-attrs-different-values']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected" select="''"/>
      <xsl:with-param name="actual">
	<xsl:call-template name="equal">
	  <xsl:with-param name="node1" select="document('')//t:test[@name='two-unequal-same-attrs-different-values']/sch:assert/sch:name[1]"/>
	  <xsl:with-param name="node2" select="document('')//t:test[@name='two-unequal-same-attrs-different-values']/sch:assert/sch:name[2]"/>
	</xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="two-equal-same-num-attrs">
    <sch:assert>foo bar <sch:name fee="boo"/> far <sch:name far='boo'/></sch:assert>
  </t:test>
  <xsl:template match="t:test[@name='two-equal-same-num-attrs']">
    <xsl:message>Test is: two-equal-same-num-attrs</xsl:message>
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected" select="''"/>
      <xsl:with-param name="actual">
	<xsl:call-template name="equal">
	  <xsl:with-param name="node1" select="document('')//t:test[@name='two-equal-same-num-attrs']/sch:assert/sch:name[1]"/>
	  <xsl:with-param name="node2" select="document('')//t:test[@name='two-equal-same-num-attrs']/sch:assert/sch:name[2]"/>
	</xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="two-unequal-same-num-attrs">
    <sch:assert>foo bar <sch:name fee="boo"/> far <sch:name far='bar'/></sch:assert>
  </t:test>
  <xsl:template match="t:test[@name='two-unequal-same-num-attrs']">
    <xsl:message>Test is: two-unequal-same-num-attrs</xsl:message>
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected" select="''"/>
      <xsl:with-param name="actual">
	<xsl:call-template name="equal">
	  <xsl:with-param name="node1" select="document('')//t:test[@name='two-unequal-same-num-attrs']/sch:assert/sch:name[1]"/>
	  <xsl:with-param name="node2" select="document('')//t:test[@name='two-unequal-same-num-attrs']/sch:assert/sch:name[2]"/>
	</xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="two-unequal">
    <sch:assert>foo bar <sch:name/> far <sch:name far='bar'/></sch:assert>
  </t:test>
  <xsl:template match="t:test[@name='two-unequal']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected" select="''"/>
      <xsl:with-param name="actual">
	<xsl:call-template name="equal">
	  <xsl:with-param name="node1" select="document('')//t:test[@name='two-unequal']/sch:assert/sch:name[1]"/>
	  <xsl:with-param name="node2" select="document('')//t:test[@name='two-unequal']/sch:assert/sch:name[2]"/>
	</xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="two-equal">
    <sch:assert>foo bar <sch:name/> far <sch:name/></sch:assert>
  </t:test>
  <xsl:template match="t:test[@name='two-equal']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected" select="true()"/>
      <xsl:with-param name="actual">
	<xsl:call-template name="equal">
	  <xsl:with-param name="node1" select="document('')//t:test[@name='two-equal']/sch:assert/sch:name[1]"/>
	  <xsl:with-param name="node2" select="document('')//t:test[@name='two-equal']/sch:assert/sch:name[2]"/>
	</xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="simple">
    <sch:assert>foo bar <sch:name/></sch:assert>
  </t:test>
  <xsl:template match="t:test[@name='simple']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected" select="true()"/>
      <xsl:with-param name="actual">
	<xsl:call-template name="equal">
	  <xsl:with-param name="node1" select="document('')//t:test[@name='simple']/sch:assert/sch:name[1]"/>
	  <xsl:with-param name="node2" select="document('')//t:test[@name='simple']/sch:assert/sch:name[1]"/>
	</xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected" select="''"/>
      <xsl:with-param name="actual">
	<xsl:call-template name="equal">
	  <xsl:with-param name="node1" select="document('')//t:test[@name='simple']/sch:assert/sch:name[2]"/>
	  <xsl:with-param name="node2" select="document('')//t:test[@name='simple']/sch:assert/sch:name[1]"/>
	</xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected" select="''"/>
      <xsl:with-param name="actual">
	<xsl:call-template name="equal">
	  <xsl:with-param name="node1" select="document('')//t:test[@name='simple']/sch:assert/sch:name[1]"/>
	  <xsl:with-param name="node2" select="document('')//t:test[@name='simple']/sch:assert/sch:name[2]"/>
	</xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>
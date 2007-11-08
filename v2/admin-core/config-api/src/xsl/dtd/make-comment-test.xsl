<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:t="test"
                xmlns:rng="http://relaxng.org/ns/structure/1.0"
                xmlns:c="http://www.sun.com/make-comment"
                xmlns:x="http://www.w3.org/1999/xhtml"
                >
  <xsl:import href="../test/test-driver.xsl"/>
  <xsl:import href="make-comment.xsl"/>

  <xsl:output method="xml"/>

  <!--

  This test ensures that those comments that occur before the
  attributes are output before the attributes, then the attributes in
  sorted order, then the comments that occur after the attributes are
  output

  -->
  <t:test name='before-after-comments'>
    <rng:define name="test-attlist" combine="interleave">
      <x:div class='dtd'>
        This is the comment before the attributes
      </x:div>
      <rng:attribute name="name">
        <x:div class='dtd'>
          This is the name attribute
        </x:div>
      </rng:attribute>
      <rng:optional>
        <rng:attribute name="optional">
          <x:div class='dtd'>
            This is an optional attribute
          </x:div>
        </rng:attribute>
      </rng:optional>
      <x:div class='dtd'>
        This is the comment after the attributes
      </x:div>
    </rng:define>
  </t:test>
  <xsl:template match="t:test[@name='before-after-comments']">
    <xsl:call-template name="t:assertEquals">
      <!--
        These tests are tricky because of the white space (especially
        the white space to the RIGHT of the actual text!! - the lines
        are padded to $line-width (default 79)).

        The easiest way of getting this stuff to work is by simply
        omitting the expected output and then running the test. If the
        output looks good then copy it into the expected param,
        whitespace and all!
      -->
      <xsl:with-param name="expected">
  attributes
    This is the comment before the attributes                         
    name                                                                       
        This is the name attribute                                    
    optional                                                                   
        This is an optional attribute                                 
    This is the comment after the attributes                          
</xsl:with-param>
      <xsl:with-param name="actual">
        <xsl:apply-templates select="document('')//t:test[@name='before-after-comments']/*" mode="make-comment"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="ordered-list">
    <x:ol>
      <x:li>one</x:li>
      <x:li>two</x:li>
      <x:li>three</x:li>
    </x:ol>
  </t:test>
  <xsl:template match="t:test[@name='ordered-list']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">1 one
2 two
3 three

</xsl:with-param>
     <xsl:with-param name="actual">
        <xsl:apply-templates select="./x:ol" mode="make-comment"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="ordered-list-with-margin">
    <x:ol>
      <x:li>one</x:li>
      <x:li>two</x:li>
      <x:li>three</x:li>
    </x:ol>
  </t:test>
  <xsl:template match="t:test[@name='ordered-list-with-margin']">
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">   1 one
   2 two
   3 three

</xsl:with-param>
     <xsl:with-param name="actual">
        <xsl:apply-templates select="./x:ol" mode="make-comment">
          <xsl:with-param name="margin" select='3'/>
        </xsl:apply-templates>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <t:test name="element">
    <rng:define name="child">
      <rng:element name="child">
        <rng:empty/>
      </rng:element>
    </rng:define>
    <rng:define name="parent">
      <rng:element name='parent'>
        <rng:ref name="child"/>
      </rng:element>
    </rng:define>
  </t:test>
  <xsl:template match="t:test[@name='element']">
        <xsl:apply-templates select="./rng:*" mode="make-comment"/>
    <xsl:call-template name="t:assertEquals">
      <xsl:with-param name="expected">
&lt;!-- child
-->

&lt;!-- parent
-->

</xsl:with-param>
<xsl:with-param name='actual'>
        <xsl:apply-templates select="document('')//t:test[@name='element']/rng:*" mode="make-comment"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>

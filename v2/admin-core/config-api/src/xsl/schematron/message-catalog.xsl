<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
		xmlns:sch="http://www.ascc.net/xml/schematron"
		xmlns:t="http:///xsl-tests"
		xmlns:m="messages"
		exclude-result-prefixes="sch t">

  <xsl:import href="message-handler.xsl"/>
  <!-- this paramater allows the use of this stylesheet as a driver for
       both modes of operation - the catalog creation mode (the default), and
       the catalog use mode (selected by defining a non-null value for "use"
  -->

  <xsl:param name="use"/>
  <!--
    This stylesheet is the driver for splitting a single message
    representation into two different kinds:

      + the catalog kind is the kind that you store in a properties
      file and is formatted into a java.text.MessageFormat string - it
      contains formal arguments referenced by number.

      + the use kind is the kind that is used at run time - it
      consists of a reference to the required message and teh correct
      number of arguments.

   In addition, assertion messages provide a location param as the
   first argument, whereas diagnostic messages don't.

   The work is actually done in another stylesheet - this one simply
   provides a means to separate out the act of traversing through a
   schematron file from the act of composig specific message
   kinds. This separation is needed because the traversal is done in
   different ways for the different kinds, and yet they share much
   similarity (for example, the formal and actual message argument
   numbers must agree!)
  -->
  
  <xsl:output method="xml" indent="yes" omit-xml-declaration="no" standalone="yes"/>

  <xsl:template match="/">
    <m:messages>
      <xsl:choose>
	<xsl:when test="$use">
	  <xsl:apply-templates select="//sch:assert |
			       //sch:diagnostic" mode="use"/>	  
	</xsl:when>
	<xsl:otherwise>
	  <xsl:apply-templates select="//sch:assert | //sch:diagnostic"/>
	</xsl:otherwise>
      </xsl:choose>
      

    </m:messages>
  </xsl:template>


  <!-- THis template is named 'cos I was testing it in a test harness
       where naming made sense - this might not hold, but it doesn't hurt
       so I've left it -->
  <xsl:template name="parameterize" match="sch:assert[@id] |
		sch:diagnostic[@id]" >
    <xsl:call-template name="make-message-format">
      <xsl:with-param name="id" select="@id"/>
      <xsl:with-param name="message-body" select="."/>
      <xsl:with-param name="location" select="local-name() = 'assert'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="sch:assert[@id] | sch:diagnostic[@id]"
		mode="use">
    <xsl:call-template name="make-message-use">
      <xsl:with-param name="id" select="@id"/>
      <xsl:with-param name="message-body" select="."/>
      <xsl:with-param name="location" select="local-name() = 'assert'"/>
    </xsl:call-template>
  </xsl:template>
<!--   <xsl:template match="sch:assert[@id] | sch:diagnostic[@id]" -->
<!-- 		mode="use"> -->
<!--     <xsl:call-template name="make-message-use"> -->
<!--       <xsl:with-param name="id" select="@id"/> -->
<!--       <xsl:with-param name="message-body" select="."/> -->
<!--     </xsl:call-template> -->
<!--   </xsl:template> -->


</xsl:stylesheet>
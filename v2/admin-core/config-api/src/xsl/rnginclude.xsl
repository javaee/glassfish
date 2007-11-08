<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0"
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:x="http://www.w3.org/1999/xhtml"
               xmlns:text="http://www.ora.com/XSLTCookbook/namespaces/text"
               xmlns:rng="http://relaxng.org/ns/structure/1.0">
  <!-- Set the output to be XML with an XML declaration and use indentation -->
  <xsl:output method="xml" omit-xml-declaration="no" indent="yes" standalone="yes"/>
  <xsl:include href="text/text.wrap.xslt"/>
  <!-- -->
  <!-- match schema and call recursive template to extract included schemas -->
  <!-- -->
  <xsl:template match="/rng:grammar | /rng:element | /rng:start">
    <!-- call the schema definition template ... -->
    <xsl:call-template name="gatherSchema">
      <!-- ... with current node as the $schemas parameter ... -->
      <xsl:with-param name="schemas" select="."/>
      <!-- ... and any includes in the $include parameter -->
      <xsl:with-param name="includes" select="document(/rng:grammar/rng:include/@href
                      | //rng:externalRef/@href)"/>
    </xsl:call-template>
  </xsl:template>
  <!-- -->
  <!-- gather all included schemas into a single parameter variable -->
  <!-- -->
  <xsl:template name="gatherSchema">
    <xsl:param name="schemas"/>
    <xsl:param name="includes"/>
    <xsl:choose>
      <xsl:when test="count($schemas) &lt; count($schemas | $includes)">
        <!-- when $includes includes something new, recurse ... -->
        <xsl:call-template name="gatherSchema">
          <!-- ... with current $includes added to the $schemas parameter ... -->
          <xsl:with-param name="schemas" select="$schemas | $includes"/>
          <!-- ... and any *new* includes in the $include parameter -->
          <xsl:with-param name="includes" select="document($includes/rng:grammar/rng:include/@href
                          | $includes//rng:externalRef/@href)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <!-- we have the complete set of included schemas, so now let's output the embedded schematron -->
        <xsl:call-template name="output">
          <xsl:with-param name="schemas" select="$schemas"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- -->
  <!-- output the schematron information -->
  <!-- -->
  <xsl:template name="output">
    <xsl:param name="schemas"/>
    <!-- -->
    <rng:grammar datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes">
      <xsl:copy-of select="$schemas//rng:grammar/rng:define | $schemas//rng:start"/>
    </rng:grammar>
  </xsl:template>
  <!-- -->


</xsl:transform>

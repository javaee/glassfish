<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:key name="test-results" match="test-result" use="concat(@ruleId,'_',@assertionId,'_',@id)"/>
  
   <xsl:output method="xml" indent="yes"/>
   <xsl:template match="test-results">
     <test-results count="{@count}">
       <xsl:apply-templates mode="accumulate" select="test-result">
         <xsl:sort select="@ruleId"/>
         <xsl:sort select="@assertionId"/>
         <xsl:sort select="@id"/>
       </xsl:apply-templates>
     </test-results>
   </xsl:template>

   <xsl:template match="test-result" mode="accumulate">
<!--      <xsl:variable name="first" select="//test-result[@ruleId=current()/@ruleId][@assertionId=current()/@assertionId][@id=current()/@id][1]"/> -->
     <xsl:variable name="first" select="key('test-results', concat(@ruleId,'_',@assertionId,'_',@id))[1]"/>
     <xsl:if test="generate-id(.) = generate-id($first)">
       <xsl:apply-templates select="."/>
     </xsl:if>
   </xsl:template>

   <xsl:template match="test-result">
     <xsl:copy>
       <xsl:apply-templates select="@*" mode="copy"/>
       <xsl:apply-templates select="key('test-results', concat(@ruleId,'_',@assertionId,'_',@id))/*"/>
<!--        <xsl:apply-templates select="//test-result[@ruleId=current()/@ruleId][@id=current()/@id][@assertionId=current()/@assertionId]/*"/> -->
     </xsl:copy>
   </xsl:template>

   <xsl:template match="@*" mode="copy">
     <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
   </xsl:template>
   
   <xsl:template match="assertion | nonAssertion">
     <xsl:copy/>
   </xsl:template>

   <xsl:template match="comment()"/>

</xsl:stylesheet>

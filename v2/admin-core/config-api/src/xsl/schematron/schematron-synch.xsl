<?xml version="1.0" ?>
<!--
This is a modified version of schematron-basic
It provides location information when dealing with schematrons
-->
<!-- Basic metastylesheet for the Schematron XML Schema Language.
	http://www.ascc.net/xml/resource/schematron/schematron.html

 Copyright (c) 2000,2001 Rick Jelliffe and Academia Sinica Computing Center, Taiwan

 This software is provided 'as-is', without any express or implied warranty. 
 In no event will the authors be held liable for any damages arising from 
 the use of this software.

 Permission is granted to anyone to use this software for any purpose, 
 including commercial applications, and to alter it and redistribute it freely,
 subject to the following restrictions:

 1. The origin of this software must not be misrepresented; you must not claim
 that you wrote the original software. If you use this software in a product, 
 an acknowledgment in the product documentation would be appreciated but is 
 not required.

 2. Altered source versions must be plainly marked as such, and must not be 
 misrepresented as being the original software.

 3. This notice may not be removed or altered from any source distribution.
-->

<!-- Schematron basic -->

<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias"
   xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo"
   xmlns:sch="http://www.ascc.net/xml/schematron">

<xsl:import href="skeleton1-5.xsl"/>
<xsl:param name="diagnose">yes</xsl:param>
<!-- <xsl:template name="process-prolog"> -->
<!--    <axsl:output method="text" /> -->
<!-- <xsl:copy-of select="document('location.xsl')//xsl:template"/> -->
<!--   <xsl:for-each select="//sch:rule[not(@abstract='true')]"> -->
<!--     <xsl:variable name="primaryKey" select="substring-before(./sch:extends/@rule | ./sch:assert/@id, '-IsPrimaryKey')"/> -->
<!--     <xsl:if test="$primaryKey"> -->
<!--       <axsl:template match="{@context}" mode="schematron-get-full-path"> -->
<!--         <axsl:apply-templates select="parent::*" mode="schematron-get-full-path"/> -->
<!--         <axsl:text>/</axsl:text> -->
<!--         <axsl:value-of select="name()"/> -->
<!--         <axsl:apply-templates select="@{$primaryKey}" mode="sfp"/> -->
<!--       </axsl:template> -->
<!--     </xsl:if> -->
<!--   </xsl:for-each> -->
<!--   <axsl:template match="@*" mode="sfp"> -->
<!--     <axsl:text>[@</axsl:text> -->
<!--     <axsl:value-of select="name()"/> -->
<!--     <axsl:text>='</axsl:text> -->
<!--     <axsl:value-of select="."/> -->
<!--     <axsl:text>']</axsl:text> -->
<!--   </axsl:template> -->
<!-- </xsl:template> -->
<xsl:template name="process-prolog">
   <axsl:output method="text" />
   <axsl:template match="sch:pattern[@id] | sch:rule[@id]" mode="schematron-get-full-path">
     <axsl:apply-templates select="parent::*" mode="schematron-get-full-path"/>
     <axsl:text>/</axsl:text>
     <axsl:value-of select="name()"/>
     <axsl:apply-templates select="@id" mode="sfp"/>
   </axsl:template>
  <axsl:template match="@*" mode="sfp">
    <axsl:text>[@</axsl:text>
    <axsl:value-of select="name()"/>
    <axsl:text>='</axsl:text>
    <axsl:value-of select="."/>
    <axsl:text>']</axsl:text>
  </axsl:template>
</xsl:template>


<xsl:template name="process-root">
   <xsl:param name="title" />
   <xsl:param name="contents" />
   <xsl:value-of select="$title" />
   <xsl:text>&#10;</xsl:text>
   <xsl:copy-of select="$contents" />
</xsl:template>

<xsl:template name="process-rule">
</xsl:template>
<!-- use default rule for process-pattern: ignore name and see -->
<!-- use default rule for process-name: output name -->
<!-- use default rule for process-assert and process-report:
     call process-message -->

  <!-- there's a bug in the skeleton-1.5 version of this template -
  the implementation of checking that key without a match attribute is
  only contained within a sch:rule is wrong. It actually tests that a
  key without a match attribute should be a sibling of a rule - it should be
  testing that a key without a match attribute should be the child of a
  rule!
-->
  <xsl:template match="sch:key | key " mode="do-keys">
    <xsl:if test="not(@name)">
      <xsl:message>Markup Error: no name attribute in &lt;key&gt;</xsl:message>
    </xsl:if>
<!--     <xsl:if test="not(@match) and not(../sch:rule)"> -->
    <xsl:if test="not(@match) and not(parent::sch:rule)">
      <xsl:message>Markup Error:  no match attribute on &lt;key&gt; outside &lt;rule&gt;</xsl:message>
    </xsl:if>
    <xsl:if test="not(@path)">
      <xsl:message>Markup Error: no path attribute in &lt;key&gt;</xsl:message>
    </xsl:if>
    <xsl:call-template name="IamEmpty"/>

    <xsl:choose>
      <xsl:when test="@match">
        <axsl:key match="{@match}" name="{@name}" use="{@path}"/>
      </xsl:when>
      <xsl:otherwise>
        <axsl:key name="{@name}" match="{parent::sch:rule/@context}" use="{@path}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


	<xsl:template name="process-assert">
		<xsl:param name="role"/>
		<xsl:param name="test"/>
                <xsl:param name="diagnostics"/>
		<!-- unused parameters: id, icon,  subject -->
		<xsl:call-template name="process-message">
			<xsl:with-param name="pattern" select="$test"/>
			<xsl:with-param name="role" select="$role"/>
		</xsl:call-template>
                <xsl:if test="$diagnose = 'yes'">
                  <xsl:call-template name="diagnosticsSplit">
                    <xsl:with-param name="str" select="$diagnostics"/>
                  </xsl:call-template>
                </xsl:if>
                <axsl:text xml:space="preserve">&#10;</axsl:text>
        </xsl:template>

<xsl:template name="process-message">
   <xsl:param name="pattern" />
   <xsl:param name="role" />
   <xsl:text>&#xa;</xsl:text>
<!-- Line: <axsl:value-of select="ext:line-number()"/> <xsl:apply-templates mode="text" /> -->
(<axsl:apply-templates mode="schematron-get-full-path" select="."/>) Assertion failed: <xsl:apply-templates mode="text" />
</xsl:template>

<xsl:template match="code" mode="text">
  <xsl:message terminate="yes">
    <xsl:value-of select="."/>
  </xsl:message>
</xsl:template>

<xsl:template name="process-diagnostic">
  <xsl:text>&#xa;</xsl:text>
  <xsl:apply-templates mode="text"/>
</xsl:template>


</xsl:stylesheet>


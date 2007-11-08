<?xml version="1.0" ?>
<!-- Implmentation for the Schematron XML Schema Language.
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

<!-- Schematron message -->

<xsl:stylesheet
 version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias"
 xmlns:sch="http://www.ascc.net/xml/schematron"
 xmlns:t="http:///xsl-tests">

  <xsl:import href="skeleton1-5.xsl"/>

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


  <!-- added the ruleId parameter so that the concrete rule ID can be
       passed down for test identification purposes -->
  <xsl:template match="sch:extends | extends">
    <xsl:param name="ruleId"/>
    <xsl:if test="not(@rule)"><xsl:message>Markup Error: no rule attribute in &lt;extends&gt;</xsl:message></xsl:if>
    <xsl:if test="not(//sch:rule[@abstract='true'][@id= current()/@rule] )                     and not(//rule[@abstract='true'][@id= current()/@rule])">
      <xsl:message>Reference Error: the abstract rule  "<xsl:value-of select="@rule"/>" has been referenced but is not declared</xsl:message>
    </xsl:if>
    <xsl:call-template name="IamEmpty"/>
    <xsl:if test="//sch:rule[@id=current()/@rule]">
      <xsl:apply-templates select="//sch:rule[@id=current()/@rule]" mode="extends">
	<!--pass down ruleId-->
	<xsl:with-param name="ruleId" select="$ruleId"/>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>

  <!-- Added ruleId generation and passdown -->
  <xsl:template match="sch:rule[not(@abstract='true')] | rule[not(@abstract='true')]">
    <xsl:if test="not(@context)">
      <xsl:message>Markup Error: no context attribute in &lt;rule&gt;</xsl:message>
    </xsl:if>
    <axsl:template match="{@context}" priority="{4000 - count(preceding-sibling::*)}" mode="M{count(../preceding-sibling::*)}">
      <xsl:call-template name="process-rule">
	<xsl:with-param name="id" select="@id"/>
	<xsl:with-param name="context" select="@context"/>
	<xsl:with-param name="role" select="@role"/>
      </xsl:call-template>
      <xsl:apply-templates>
	<xsl:with-param name="ruleId" select="@id"/>
      </xsl:apply-templates>
      <axsl:apply-templates mode="M{count(../preceding-sibling::*)}"/>
    </axsl:template>
  </xsl:template>

  <!-- Added ruleId as a param, passed down thru' stack -->
  <xsl:template match="sch:rule[@abstract='true'] | rule[@abstract='true']" mode="extends">
    <xsl:param name="ruleId"/>
    <xsl:if test="@context">
      <xsl:message>Markup Error: context attribute on abstract &lt;rule&gt;</xsl:message>
    </xsl:if>
    <xsl:apply-templates><xsl:with-param name="ruleId" select="$ruleId"/></xsl:apply-templates>
  </xsl:template>

  

  <!-- Added ruleId to qualify test cases -->
  <xsl:template match="sch:assert | assert">
    <xsl:param name="ruleId"/>
    <xsl:if test="not(@test)">
      <xsl:message>Markup Error: no test attribute in &lt;assert&gt;</xsl:message>
    </xsl:if>
    <axsl:choose>
      <axsl:when test="{@test}">
	<xsl:call-template name="process-assert-true">
	  <xsl:with-param name="role" select="@role"/>
	  <xsl:with-param name="id" select="@id"/>
	  <xsl:with-param name="test" select="normalize-space(@test)"/>
	  <xsl:with-param name="icon" select="@icon"/>
	  <xsl:with-param name="subject" select="@subject"/>
	  <xsl:with-param name="diagnostics" select="@diagnostics"/>
	  <xsl:with-param name="ruleId" select="$ruleId"/>
	</xsl:call-template>  
      </axsl:when>
      <axsl:otherwise>
	<xsl:call-template name="process-assert">
	  <xsl:with-param name="role" select="@role"/>
	  <xsl:with-param name="id" select="@id"/>
	  <xsl:with-param name="test" select="normalize-space(@test)"/>
	  <xsl:with-param name="icon" select="@icon"/>
	  <xsl:with-param name="subject" select="@subject"/>
	  <xsl:with-param name="diagnostics" select="@diagnostics"/>
	  <xsl:with-param name="ruleId" select="$ruleId"/>
	</xsl:call-template>  
      </axsl:otherwise>
    </axsl:choose>
  </xsl:template>
  
  <xsl:template name="process-prolog">
    <xsl:attribute name="exclude-result-prefixes">t sch</xsl:attribute>
    <axsl:output method="xml" indent="yes" />
    <axsl:include href="testName.xsl"/>
    <axsl:param name="test.dir">tests</axsl:param>
    <axsl:template name="getNumber">
      <axsl:param name="id"/>
      <axsl:param name="attr" select="'0'"/>
      <axsl:choose>
	<axsl:when test="not($attr)">0</axsl:when>
	<axsl:when test="string(number($attr))='NaN'">
	  <axsl:message>Non number found in test id: <axsl:value-of select="$id"/></axsl:message>
	</axsl:when>
	<axsl:otherwise>
	  <axsl:value-of select="number($attr)"/>
	</axsl:otherwise>
      </axsl:choose>
    </axsl:template>

<!-- DELETE     <axsl:template name="make-test-result"> -->
<!-- DELETE       <axsl:param name="elem"/> -->
<!-- DELETE       <axsl:if test="not($elem)"> -->
<!-- DELETE 	<axsl:message terminate="yes">make-test-result: No element name provided!</axsl:message> -->
<!-- DELETE       </axsl:if> -->
<!-- DELETE       <axsl:variable name="test" select="ancestor::t:test"/> -->
<!-- DELETE       <axsl:variable name="numExpectedAssertions"> -->
<!-- DELETE 	<axsl:call-template name="getNumber"> -->
<!-- DELETE 	  <axsl:with-param name="id" select="$test/@id"/> -->
<!-- DELETE 	  <axsl:with-param name="attr" select="$test/@expectedAssertions"/> -->
<!-- DELETE 	</axsl:call-template> -->
<!-- DELETE       </axsl:variable> -->
<!-- DELETE       <axsl:variable name="numExpectedNonAssertions"> -->
<!-- DELETE 	<axsl:call-template name="getNumber"> -->
<!-- DELETE 	  <axsl:with-param name="id" select="$test/@id"/> -->
<!-- DELETE 	  <axsl:with-param name="attr" select="$test/@expectedNonAssertions"/> -->
<!-- DELETE 	</axsl:call-template> -->
<!-- DELETE       </axsl:variable> -->
<!-- DELETE       <test-result id="{{$test/@id}}" -->
<!-- DELETE 		   ruleId="{{$test/@ruleId}}" -->
<!-- DELETE 		   expectedAssertions="{{$numExpectedAssertions}}" -->
<!-- DELETE 		   expectedNonAssertions="{{$numExpectedNonAssertions}}" -->
<!-- DELETE 		   assertionId="{{$test/@assertionId}}"> -->
<!-- DELETE 	<axsl:element name="{{$elem}}"/> -->
<!-- DELETE       </test-result> -->
<!-- DELETE     </axsl:template> -->

    <axsl:template name="make-non-assertion-test-result">
<!-- DELETE -->
<!--       <axsl:call-template name="make-test-result"> -->
<!-- 	<axsl:with-param name="elem">nonAssertion</axsl:with-param> -->
<!--       </axsl:call-template> -->
<nonAssertion/>
    </axsl:template>

    <axsl:template name="make-assertion-test-result">
<!-- DELETE -->
<!--       <axsl:call-template name="make-test-result"> -->
<!-- 	<axsl:with-param name="elem">assertion</axsl:with-param> -->
<!--       </axsl:call-template> -->
<assertion/>
    </axsl:template>
  </xsl:template>


  <xsl:template name="process-root">
    <xsl:param name="title" />
    <xsl:param name="icon" />
    <xsl:param name="contents" />
    <!-- Note - this expression is same as in test-extractor.xsl::match="/" and
	 needs to be synchronized with it -->
    <axsl:variable name="tests" select="//t:test[not(ancestor::sch:rule[@abstract='true'])]"/>
    <axsl:choose>
      <axsl:when test="not($tests)">
	<axsl:message terminate="yes">No tests found!</axsl:message>
      </axsl:when>
      <axsl:otherwise>
	<axsl:element name="test-results">
	  <axsl:attribute name="count"><axsl:value-of select="count($tests)"/></axsl:attribute>
	  <axsl:for-each select="$tests">
	    <axsl:variable name="numExpectedAssertions">
	      <axsl:call-template name="getNumber">
		<axsl:with-param name="id" select="@id"/>
		<axsl:with-param name="attr" select="@expectedAssertions"/>
	      </axsl:call-template>
	    </axsl:variable>
	    <axsl:variable name="numExpectedNonAssertions">
	      <axsl:call-template name="getNumber">
		<axsl:with-param name="id" select="@id"/>
		<axsl:with-param name="attr" select="@expectedNonAssertions"/>
	      </axsl:call-template>
	    </axsl:variable>
	    <test-result id="{{@id}}"
			 ruleId="{{@ruleId}}"
			 expectedAssertions="{{$numExpectedAssertions}}"
			 expectedNonAssertions="{{$numExpectedNonAssertions}}"
			 assertionId="{{@assertionId}}">
	      <axsl:variable name="testFileName">
		<axsl:call-template name="getTestFileName">
		  <axsl:with-param name="test" select="."/>
		  <axsl:with-param name="test.dir" select="$test.dir"/>
		</axsl:call-template>
	      </axsl:variable>
	      <axsl:for-each select="document($testFileName)">
		<xsl:copy-of select="$contents"/>
	      </axsl:for-each>
	    </test-result>
          </axsl:for-each>
        </axsl:element>
      </axsl:otherwise>
    </axsl:choose>
  </xsl:template>
  <!-- use default rule for process-pattern: ignore name and see -->
  <!-- use default rule for process-name:  output name -->
  <!-- use default rule for process-assert and process-report:
       call process-message -->

  <xsl:template name="process-assert-true">
    <xsl:param name="role"/>
    <xsl:param name="id"/>
    <xsl:param name="test"/>
    <xsl:param name="icon"/>
    <xsl:param name="subject"/>
    <xsl:param name="diagnostics"/>
    <xsl:param name="ruleId"/>
    <axsl:variable name="test-elem" select="ancestor::t:test"/>
    <axsl:if test="$test-elem and '{$id}' = $test-elem/@assertionId and '{$ruleId}' = $test-elem/@ruleId">
<!--  DELETE     <axsl:call-template name="make-non-assertion-test-result"/> -->
<nonAssertion/>
    </axsl:if>
  </xsl:template>


  <xsl:template name="process-assert">
    <xsl:param name="role"/>
    <xsl:param name="id"/>
    <xsl:param name="test"/>
    <xsl:param name="icon"/>
    <xsl:param name="subject"/>
    <xsl:param name="diagnostics"/>
    <xsl:param name="ruleId"/>
    <axsl:variable name="test-elem" select="ancestor::t:test"/>
    <axsl:if test="$test-elem and '{$id}' = $test-elem/@assertionId and '{$ruleId}' = $test-elem/@ruleId">
<!-- DELETE      <axsl:call-template name="make-assertion-test-result"/> -->
<assertion/>
    </axsl:if>
  </xsl:template>
  
</xsl:stylesheet>

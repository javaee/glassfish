<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:sch="http://www.ascc.net/xml/schematron"
                xmlns:t="http:///xsl-tests"
                xmlns:str="http://www.ora.com/XSLTCookbook/namespaces/strings"
                exclude-result-prefixes="t sch">

  <xsl:import href="str.dup.xslt"/>
  <xsl:output method="html" indent="yes"></xsl:output>


  <xsl:template match="/sch:schema">
    <html>
      <title>Validity criteria for <code>domain.xml</code></title>
      <body>
        <h1>Validity criteria for <code>domain.xml</code></h1>

        <h2>Introduction</h2>

        This document expresses validity criteria for
        <code>domain.xml</code> as collections of assertions that can
        be made about the document. If any one of those assertions
        proves false then the document is invalid. We use <a
        href="http://xml.ascc.net/resource/schematron/Schematron2000.html">Schematron</a>
        as the rule engine for organizing and applying the validity
        checks.
        <p>
          Schematron Assertions are explained in natural language for
          documentation, but realized internally as <a
          href="http://www.w3.org/TR/xpath">XPath</a>
          expressions. They are predicates about the elements,
          attributes and values which appear in the source document
          (<code>domain.xml</code>).
        </p>
        <p>
          Each individual assertion has a small lexical scope, and
          makes a statement about only part of the source document. In
          particular an assertion assumes that a specific element
          within the source document is the object of the assertion
          (known as the "current object").
        </p>
        <p>
          Assertions are written so as not to overlap in
          meaning. For example, an assertion which establishes that
          an attribute has a mandatory value is written separately from an
          assertion that states that that value is the name of some
          other element in the document.
        </p>
        <p>
          Schematron Rules provide a mechanism for connecting an
          element from the source document to the assertions that are
          to be triggered with that element as the current
          object. This is achieved using an <a
          href="http://www.w3.org/TR/xslt#patterns">XSLT
          Pattern</a>. When an element matches the pattern (we say
          informally that it matches the rule) then that element is
          used as the current object for every assertion within the
          matching rule. The assertions are applied in some order -
          typically lexical order, but that isn't guaranteed.
        </p>
        <p>
          Schematron Patterns provide a mechanism for grouping rules,
          and for determining which rules are to be applied to
          elements and in what order. A pattern simply consists of a
          collection of rules in a specific order. The pattern ensures
          that, for any element from the source document, the first
          rule that matches that element will be the rule used to
          check that element, and no other rule from that pattern will
          be used to check that element.
        </p>
        <p>
          Schematron Phases provide a grouping mechanism for
          Patterns. However we don't use phases. This has the effect
          that every pattern is used, in some arbitrary order.
        </p>
        <xsl:apply-templates select="sch:pattern" mode="contents"/>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="sch:pattern" mode="contents">
    <h2>Pattern: <a href="#{generate-id(@name)}"><xsl:value-of select="@name"/></a></h2>
    <table cellpadding="2" cellspacing="2" border="1" style="width: 100%;">
      <tbody>
        <tr>
          <th style="width: 20%">
            <xsl:choose>
              <xsl:when test="sch:rule/@abstract='true'">
                Rule Id
              </xsl:when>
              <xsl:otherwise>
                Context Expression
              </xsl:otherwise>
            </xsl:choose>
          </th>
          <th style="width: 80%">
            Assertion
          </th>
        </tr>
        <xsl:apply-templates select="sch:rule" mode="contents"/>
      </tbody>
    </table>
  </xsl:template>

  <xsl:template name="link-to-rule-perhaps">
    <xsl:param name="node"/>
    <xsl:param name="link"/>
    <xsl:choose>
      <xsl:when test="$link">
        <a href="{concat('#', generate-id($node/..))}"><xsl:value-of select="$node"/></a>        
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$node"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>
  <xsl:template match="sch:rule" mode="contents">
    <xsl:variable name="asserts" select="sch:assert[not(@test='true()')] | //sch:rule[@id=current()/sch:extends/@rule]/sch:assert[not(@test='true()')]"/>
    <tr>
      <td rowspan="{count($asserts)}" >
        <xsl:choose>
          <xsl:when test="not(@abstract) or @abstract='false'">
            <xsl:call-template name="link-to-rule-perhaps">
              <xsl:with-param name="node" select="@context"/>
              <xsl:with-param name="link" select="$asserts"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="link-to-rule-perhaps">
              <xsl:with-param name="node" select="@id"/>
              <xsl:with-param name="link" select="$asserts"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <xsl:if test="not($asserts)">
        <td><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
      </xsl:if>
        <xsl:for-each select="$asserts">
          <xsl:choose>
          <xsl:when test="position() > 1">
            <tr><xsl:apply-templates select="." mode="contents"/></tr>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="." mode="contents"/>
          </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
    </tr>
  </xsl:template>

  <xsl:template match="sch:assert" mode="contents">
    <td>
      <xsl:choose>
          <xsl:when test="@test='true()'">
            No assertion
          </xsl:when>
          <xsl:otherwise>
            <a href="#{generate-id()}"><xsl:apply-templates/></a>
          </xsl:otherwise>
        </xsl:choose>
    </td>
  </xsl:template>

  <xsl:template match="sch:pattern">
    <h2>Pattern: <a name="{generate-id()}"><xsl:value-of select="@name"></xsl:value-of></a></h2>
    <dl>
      <xsl:apply-templates></xsl:apply-templates>
    </dl>
  </xsl:template>

  <xsl:template match="sch:p">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="sch:rule[@abstract='true']">
    <a name="{generate-id()}"/>
      <dt><h3>Abstract Rule: <xsl:value-of select="@id"/></h3></dt>
    <dd><xsl:apply-templates/></dd>
  </xsl:template>
  
  <xsl:template match="sch:rule[not(@abstract='true')]">
    <a name="{generate-id()}"/>
      <dt><h3><code><xsl:value-of select="@context"/></code></h3></dt>
    <dd><xsl:apply-templates/></dd>
  </xsl:template>

  <xsl:template match="sch:assert[@test='true()']">
    <p>
      No assertions
    </p>
  </xsl:template>

  <xsl:template match="sch:assert">
    <p>
      <a name="{generate-id()}">
        <strong>Assertion: </strong>
      </a>
    <xsl:apply-templates/>
    </p>
    <p>
      <xsl:call-template name="makeExamples">
        <xsl:with-param name="tests" select="../t:test[@assertionId=current()/@id]"/>
      </xsl:call-template>
    </p>
  </xsl:template>

  <xsl:template match='sch:extends'>
      <p>
        Extends: 
        <a href="#{generate-id(//sch:rule[@id=current()/@rule])}">
          <xsl:value-of select="@rule"/>
        </a>
      </p>
      <xsl:variable name="extensions">
            <xsl:apply-templates select="." mode="getAssertionIds"/>
      </xsl:variable>
      <xsl:call-template name="makeExamples">
        <xsl:with-param name="tests" select="../t:test[contains($extensions, @assertionId)]"/>
      </xsl:call-template>
  </xsl:template>

  <xsl:template match="sch:extends" mode="getAssertionIds">
    <xsl:apply-templates select="//sch:rule[@id=current()/@rule]" mode="getAssertionIds"/>
  </xsl:template>

  <xsl:template match="sch:name">
    <xsl:choose>
      <xsl:when test="ancestor::sch:rule[not(@abstract='true')]">
        <code><xsl:value-of select="ancestor::sch:rule/@context"/></code>
      </xsl:when>
      <xsl:otherwise>
       <em>element name</em>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!--
    This rule retuns all the assertions reachable from a rule
    referenced by an extension.
  -->
  <xsl:template match="sch:rule" mode="getAssertionIds">
    <xsl:for-each select="sch:assert/@id">
      <xsl:value-of select="."/>
    </xsl:for-each>
    <xsl:for-each select="sch:extends">
      <xsl:apply-templates select="//sch:rule[@id=current/@rule]" mode="getAssertionIds"/>
    </xsl:for-each>
<!--     <xsl:variable name="result"> -->
<!--       <xsl:apply-templates select="//sch:rule[@id=current/sch:extends/@rule]" mode="getAssertionIds"/> -->
<!--     </xsl:variable> -->
<!--     <xsl:value-of select="$result | sch:assert"/> -->
  </xsl:template>

  <xsl:template match="sch:*"/>
  <xsl:template match="t:*"/>
  <xsl:template match="*">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="@*" mode="html-output">
    <xsl:text> </xsl:text><xsl:value-of select="name()"/>
    <xsl:text>="</xsl:text><xsl:value-of select="."/><xsl:text>"</xsl:text>
  </xsl:template>


  <xsl:template match="*" mode="html-output">
    <xsl:param name="indentation">2</xsl:param>
      <xsl:call-template name="indent">
        <xsl:with-param name="distance" select="$indentation"/>
      </xsl:call-template>
      <xsl:text disable-output-escaping="yes">&amp;lt;</xsl:text><xsl:value-of select="name()"/>
      <xsl:apply-templates select="@*" mode="html-output"/>
      <xsl:choose>
        <xsl:when test="not(*)">
        <xsl:text disable-output-escaping="yes">/&amp;gt;</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text disable-output-escaping="yes">&amp;gt;</xsl:text>
          <xsl:text>&#10;</xsl:text>
          <xsl:apply-templates select="*" mode="html-output">
            <xsl:with-param name="indentation" select="$indentation + 2"/>
          </xsl:apply-templates>
          <xsl:call-template name="indent">
            <xsl:with-param name="distance" select="$indentation"/>
          </xsl:call-template>
          <xsl:text disable-output-escaping="yes">&amp;lt;/</xsl:text>
          <xsl:value-of select="name()"/>
          <xsl:text disable-output-escaping="yes">&amp;gt;</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>&#10;</xsl:text>
  </xsl:template>

  <xsl:template name="indent">
    <xsl:param name="distance">0</xsl:param>
    <xsl:call-template name="str:dup">
      <xsl:with-param name="input">&#xa0;</xsl:with-param>
      <xsl:with-param name="count" select="$distance"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="makeExamples">
    <xsl:param name="tests"/>
    <xsl:variable name="valid" select="$tests[(not(@expectedAssertions) or (@expectedAssertions = 0)) and @expectedNonAssertions &gt; 0]"/>
      <xsl:variable name="invalid" select="$tests[@expectedAssertions &gt; 0]"/>
      <xsl:if test="$valid">
        Example of valid input&#10;
        <pre>
          <xsl:apply-templates select="$valid[1]/*" mode="html-output"/>
        </pre>
      </xsl:if>
      <xsl:if test="$invalid">
        Example of invalid input&#10;
        <pre>
          <xsl:apply-templates select="$invalid[1]/*" mode="html-output"/>
        </pre>
      </xsl:if>
  </xsl:template>

</xsl:stylesheet>

<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rng="http://relaxng.org/ns/structure/1.0"
                xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0"
                xmlns:x="http://www.w3.org/1999/xhtml"
                >

  <xsl:output method="xml" indent="yes"/>
  <xsl:include href="dtd/make-entity.xsl"/>
  <xsl:include href="dtd/make-element.xsl"/>
  <xsl:include href="dtd/make-choice.xsl"/>
  <xsl:include href="dtd/make-seq.xsl"/>
  <xsl:include href="dtd/make-comment.xsl"/>
  <xsl:param name="purpose"/>
  <xsl:param name="copyright.text.comment"/>
  <xsl:param name="dtd.comment"/>
  
  <xsl:template match='rng:grammar'>
    <xsl:message>Purpose is:  <xsl:value-of select="$purpose"/></xsl:message>
<xsl:comment>
  <xsl:text> <xsl:value-of select="$copyright.text.comment"/> 
  </xsl:text>
</xsl:comment>
<xsl:comment>
  <xsl:text> <xsl:value-of select="$dtd.comment"/> 
  </xsl:text>
</xsl:comment>
<xsl:text>&#10;</xsl:text>

<xsl:call-template name="make-entities">
  <xsl:with-param name="sources" select="rng:define[rng:data] | rng:define[rng:choice]"/>
</xsl:call-template>

<xsl:comment> ELEMENTS </xsl:comment>
<xsl:apply-templates select="rng:define[not(rng:data)][not(rng:choice)]">
</xsl:apply-templates>
<xsl:text>&#10;</xsl:text>

  </xsl:template>
  
  <xsl:template match="comment()">
    <xsl:text>&#xa;</xsl:text>
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- Most top level data values we simply represent as an ENTITY with CDATA -->
  <xsl:template match="/rng:grammar/rng:define[rng:data]">
    <xsl:call-template name="make-entity">
      <xsl:with-param name="name" select="@name"/>
      <xsl:with-param name="value">CDATA</xsl:with-param>
    </xsl:call-template>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <!-- these top level data values will not be made into an ENTITY - this might change in the future -->
  <xsl:variable name="no-entity"> unix-fs-safe-name-type
  jmx-safe-key-type file-type reduced-name-type name-type port-type classname-type IPAddress-type jndi-unique-type web-service-name-type</xsl:variable>
  <xsl:template match="/rng:grammar/rng:define[contains($no-entity, concat(' ', @name))][rng:data]"/>

  <!-- a top level define with a choice child is an entity enumeration -->
  <xsl:template match="/rng:grammar/rng:define[./rng:choice]">
    <xsl:apply-templates select="." mode="make-comment"/>
    <xsl:text>&#xa;</xsl:text>
    <xsl:call-template name="make-entity">
      <xsl:with-param name="name" select="@name"/>
      <xsl:with-param name="value">
        <xsl:call-template name="make-choice">
          <xsl:with-param name="values" select="rng:choice/rng:value"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <!-- defines with non-empty elements translate to ELEMENTS. The first thing to do is to extract any comments before continuing the processing
  -->
  <xsl:template match="/rng:grammar/rng:define[rng:element]">
    <xsl:text>&#xa;</xsl:text>

    <xsl:apply-templates select="." mode="make-comment"/>
    
    <xsl:apply-templates/>
  </xsl:template>


  <!-- non-empty elements translate to ELEMENTS
       The children which are not attribute lists, and which are
       non-empty, are selected as children of the new ELEMENT, in the
       given sequence.
  -->
  <xsl:template match="/rng:grammar/rng:define/rng:element">
    <xsl:variable name="children" select="./rng:*[not(contains(@name, $attlist))][local-name(.) != 'empty']"/>
    <xsl:call-template name="make-element">
      <xsl:with-param name="name" select="@name"/>
        <xsl:with-param name="contents">
          <xsl:if test="$children">
            <xsl:call-template name="make-seq">
              <xsl:with-param name="values" select="$children"/>
            </xsl:call-template>
          </xsl:if>
        </xsl:with-param>
    </xsl:call-template>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <!-- swallow empty attributes -->
  <xsl:template match="/rng:grammar/rng:define[contains(@name, 'attlist')][./rng:empty]" priority="1"/>

  <!-- definitions with the string $attlist in their name define ATTLISTS -->
  <xsl:template match="/rng:grammar/rng:define[contains(@name, $attlist)]">
    <xsl:text>&#xa;</xsl:text>
    <xsl:text disable-output-escaping="yes">&lt;!ATTLIST </xsl:text>
    <xsl:value-of  select="substring-before(@name, $attlist)"/>
    <xsl:text>&#xa;</xsl:text>
    <xsl:apply-templates select=".//rng:attribute"/>
<!--     <xsl:for-each select=".//rng:attribute"> -->
<!--       <xsl:if test="position() > 1"><xsl:text>&#xa;</xsl:text></xsl:if> -->
<!--       <xsl:apply-templates select="." mode="attribute"/> -->
<!--     </xsl:for-each> -->
    <xsl:text disable-output-escaping='yes'>&gt;</xsl:text>
    <xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <!-- optional content translates to '?' -->
  <xsl:template match="rng:optional">
    <xsl:apply-templates/><xsl:text>?</xsl:text>
  </xsl:template>

  <xsl:template match="rng:attribute">
    <xsl:if test="position() > 1"><xsl:text>&#xa;</xsl:text></xsl:if>
    <xsl:text>    </xsl:text>
    <xsl:value-of select="@name"/>
    <xsl:text> </xsl:text>
    <xsl:choose>
      <xsl:when test="rng:ref | rng:choice">
	<xsl:apply-templates select="rng:ref | rng:choice" mode="entity"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>CDATA</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text> </xsl:text>
    <xsl:choose>
      <xsl:when test="@a:defaultValue">
        <xsl:text>"</xsl:text><xsl:value-of select="@a:defaultValue"/><xsl:text>"</xsl:text>
      </xsl:when>
      <xsl:when test="parent::rng:optional">
        <xsl:text>#IMPLIED</xsl:text>
      </xsl:when>
      <xsl:otherwise>
	<xsl:text>#REQUIRED</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="rng:value">
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>

  <xsl:template match="a:defaultValue">
    <xsl:text> "</xsl:text><xsl:value-of select="."/><xsl:text>"</xsl:text>
  </xsl:template>
  
  <xsl:template match="rng:zeroOrMore">
    <xsl:apply-templates /><xsl:text>*</xsl:text>
  </xsl:template>

  <xsl:template match="rng:oneOrMore">
    <xsl:apply-templates /><xsl:text>+</xsl:text>
  </xsl:template>

  <xsl:template match="rng:ref">
    <xsl:value-of select="@name"/>
  </xsl:template>

  <xsl:template match="rng:ref" mode="entity">
    <xsl:choose>
    <xsl:when test="contains($no-entity, concat(' ', @name))">
      <xsl:text>CDATA</xsl:text>
    </xsl:when>
    <xsl:otherwise>
        <xsl:text>%</xsl:text><xsl:value-of select="@name"/><xsl:text>;</xsl:text>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="rng:text">
    <xsl:text>#PCDATA</xsl:text>
  </xsl:template>

  <xsl:template match="rng:choice" mode="entity">
    <xsl:call-template name="make-choice">
      <xsl:with-param name="values" select="*"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="rng:choice">
    <xsl:call-template name="make-choice">
      <xsl:with-param name="values" select="*"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="text()"/><!-- swallow -->
  <xsl:template match="text()" mode="optional"/>
  <xsl:template match="text()" mode="attribute"/>
  <xsl:template match="rng:start"/>

  <xsl:template name="make-entities">
    <xsl:param name="sources"/>
    <xsl:if test="$sources">
<xsl:comment> ENTITIES </xsl:comment>
<xsl:apply-templates select="$sources">
</xsl:apply-templates>
<xsl:text>&#10;</xsl:text>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>

<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:rng="http://relaxng.org/ns/structure/1.0"
                xmlns:text="http://www.ora.com/XSLTCookbook/namespaces/text"
                xmlns:str="http://www.ora.com/XSLTCookbook/namespaces/strings"
                xmlns:c="http://www.sun.com/make-comment"
                xmlns:x="http://www.w3.org/1999/xhtml"
                >

  <xsl:import href="../text/text.wrap.xslt"/>
  <xsl:strip-space elements="*"/>
  <!-- the amount that of each indentation -->
  <xsl:variable name="offset" select="4"/>
  <!-- the suffix that denotes a name is an attribute list -->
  <xsl:variable name="attlist">-attlist</xsl:variable>
  <!-- the width of a line -->
  <xsl:variable name="line-width" select="79"/>

  <!--
    this key provides the ability to lookup an element name by its
    children's names
  --> 
  <xsl:key name="parentByChildName"
           match="rng:element"
           use=".//rng:ref/@name"/> 
  <!--
    this key provides the ability to lookup an element name by the
    name of an entity used within its attributes
  -->
  <xsl:key name="parentByEntityName"
           match="rng:define"
           use=".//rng:attribute//rng:ref/@name"/>
  <!--
    Enumerations and data are defined by having immediate child choice elements.
  -->
  <xsl:template match="rng:define[./rng:choice]" mode="make-comment">
    <xsl:text>&#10;</xsl:text>
    <xsl:comment><xsl:text> </xsl:text><xsl:value-of select="@name"/>
    <xsl:text>&#10;</xsl:text>
    <xsl:apply-templates select="x:div[@class='dtd']" mode="make-comment">
      <xsl:with-param name="margin" select="$offset"/>
    </xsl:apply-templates>
    <xsl:apply-templates select=".//rng:value[@x:desc]" mode="make-comment">
      <xsl:with-param name="margin" select="$offset"/>
    </xsl:apply-templates>

    <xsl:call-template name="entity-used-in"/>
    </xsl:comment>

  </xsl:template>
  <xsl:template name="entity-used-in">
    <xsl:variable name="attlists" select="key('parentByEntityName',
                  @name)"/>
    <xsl:if test="$attlists">
      <xsl:variable name="elements">
        <xsl:for-each select="$attlists">
          <xsl:sort select="@name"/>
          <xsl:value-of select="substring-before(@name,$attlist)"/>
          <xsl:if test="position() != last()">
            <xsl:text>, </xsl:text>
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>
      <xsl:call-template name="c:make-heading">
        <xsl:with-param name="text">Used in:</xsl:with-param>
      </xsl:call-template>
      <xsl:call-template name="text:wrap">
        <xsl:with-param name="input" select="$elements"/>
        <xsl:with-param name="margin" select="$offset"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <!--   
    Elements are defined by having immediate element
    elements. Comments for an element incorporate the comments for the
    attributes too - the attributes are found in the corresponding
    define element, whose name is the name of this define with
    $attlist appended.
  -->
  <xsl:template match="rng:define[rng:element]" mode="make-comment">
    <xsl:text>&#10;</xsl:text>
    <xsl:comment><xsl:text> </xsl:text><xsl:value-of select="@name"/>
    <xsl:text>&#10;</xsl:text>
    <xsl:apply-templates select="rng:element/x:div[@class='dtd']" mode="make-comment">
      <xsl:with-param name="margin" select="$offset"/>
    </xsl:apply-templates>
   <xsl:variable name="commented-child-elements"
                  select=".//rng:ref[not(contains(@name, $attlist))][./x:div[@class='dtd']]"/>
    <xsl:if test="$commented-child-elements">
      <xsl:call-template name="c:make-heading">
        <xsl:with-param name="text">children</xsl:with-param>
        <xsl:with-param name="level" select="2"/>
      </xsl:call-template>
      <xsl:apply-templates select="$commented-child-elements" mode="make-comment">
        <xsl:with-param name="margin" select="$offset"/>
      </xsl:apply-templates>
    </xsl:if>
    <xsl:apply-templates
     select="//rng:define[@name=concat(current()/@name, $attlist)]"
     mode="make-comment"/>
    <xsl:variable name="parents" select="key('parentByChildName',@name)"/>    <xsl:if test="$parents">
      <xsl:call-template name="c:make-heading">
        <xsl:with-param name='text'>Used in:</xsl:with-param>
      </xsl:call-template>
      <xsl:variable name="list">
        <xsl:if test="$parents">
          <xsl:for-each select="$parents">
              <xsl:value-of select="@name"/>
              <xsl:sort select="@name"/>
              <xsl:if test="position() != last()">
                <xsl:text>, </xsl:text>
              </xsl:if>
          </xsl:for-each>
        </xsl:if>

      </xsl:variable>

      <xsl:call-template name="text:wrap">

        <xsl:with-param name="input" select="$list"/>

        <xsl:with-param name="margin" select="$offset"/>

      </xsl:call-template>

    </xsl:if>

    </xsl:comment>
    <xsl:text>&#10;</xsl:text>

  </xsl:template>
  <!--
    Attributes are defined by having a name that ends in $attlist
  -->
  <xsl:template match="rng:define[contains(@name,$attlist)]" mode="make-comment">
      <xsl:variable name="commented-attributes"
                    select=".//rng:attribute[x:div[@class='dtd'] or
                    .//rng:value[@x:desc]]"/>
      <xsl:if test="$commented-attributes | ./x:div[@class='dtd']">
        <xsl:call-template name="c:make-heading">
          <xsl:with-param name="text">attributes</xsl:with-param>
        </xsl:call-template>
        <xsl:apply-templates
         select="*[self::x:div[@class='dtd']][not(preceding::rng:attribute)]" mode="make-comment">
          <xsl:with-param name="margin" select="$offset"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="$commented-attributes" mode="make-comment">
          <xsl:sort select="@name"/>
          <xsl:with-param name="margin" select="$offset"/>
        </xsl:apply-templates>
        <xsl:apply-templates
         select="*[self::x:div[@class='dtd']][not(following::rng:attribute)]" mode="make-comment">
          <xsl:with-param name="margin" select="$offset"/>
        </xsl:apply-templates>
      </xsl:if>
  </xsl:template>
  <xsl:template match="rng:attribute" mode="make-comment">
    <xsl:param name="margin"/>
    <xsl:if test="not($margin)">
      <xsl:message terminate="yes">No margin supplied - exiting</xsl:message>
    </xsl:if>
    <xsl:call-template name="c:make-dt-entry">
      <xsl:with-param name="dt" select="@name"/>
      <xsl:with-param name="dd" select="x:div[@class='dtd']"/>
      <xsl:with-param name="margin" select="$margin"/>
    </xsl:call-template>
    <xsl:apply-templates select=".//rng:value[@x:desc]" mode="make-comment">
      <xsl:with-param name="margin" select="$margin + $offset"/>
    </xsl:apply-templates>
  </xsl:template>
  <xsl:template match="rng:ref" mode="make-comment">
    <xsl:param name="margin"/>
    <xsl:if test="not($margin)">
      <xsl:message terminate="yes">No margin supplied - exiting</xsl:message>
    </xsl:if>
    <xsl:call-template name="c:make-dt-entry">
      <xsl:with-param name="margin" select="$margin"/>
      <xsl:with-param name="dt" select="@name"/>
      <xsl:with-param name="dd" select="x:div[@class='dtd']"/>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match='text()' mode="make-comment">
    <xsl:param name="margin"/>
    <xsl:if test="not($margin)">
      <xsl:message terminate="yes">No margin supplied - exiting</xsl:message>
    </xsl:if>
    <xsl:if test="normalize-space()">
      <xsl:call-template name="text:wrap">
        <xsl:with-param name="margin" select="$margin"/>
        <xsl:with-param name="input" select="normalize-space(.)"/>
      </xsl:call-template>
      <xsl:if test="not(position() = last())">
        <xsl:text>&#10;</xsl:text>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  <xsl:template match="rng:value[@x:desc]" mode="make-comment">
    <xsl:param name="margin"/>
    <xsl:if test="not($margin)">
      <xsl:message terminate="yes">No margin supplied - exiting</xsl:message>
    </xsl:if>
    <xsl:call-template name="c:make-dt-entry">
      <xsl:with-param name="margin" select="$margin"/>
      <xsl:with-param name="dt" select="."/>
      <xsl:with-param name="dd" select="@x:desc"/>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="@*" mode="make-comment">
    <xsl:param name="margin"/>
    <xsl:if test="not($margin)">
      <xsl:message terminate="yes">No margin supplied - exiting</xsl:message>
    </xsl:if>
    <xsl:call-template name="text:wrap">
      <xsl:with-param name="margin" select="$margin"/>
    </xsl:call-template>
  </xsl:template>
  <!--
    c:make-dt-entry dt [dd] margin  - make an entry, using dt as the term
    to be defined, dd as the definition, with the left margin starting
    at margin. Indent is $offset, and line width is $line-width
    (global variables).
    This simply outputs dt using the given margin, followed by a new
    line, and then calls apply-templates on dd in make-comment mode,
    with a new margin.
  -->
  <xsl:template name="c:make-dt-entry">
    <xsl:param name="dt"/>
    <xsl:param name="dd"/>
    <xsl:param name="margin"/>
    <xsl:if test="not($dt)">
      <xsl:message terminate="yes">No definition term given - exiting</xsl:message>
    </xsl:if>
    <xsl:if test="not($margin)">
      <xsl:message terminate="yes">No margin term given - exiting</xsl:message>
    </xsl:if>
    <xsl:variable name="dt-margin" select="$margin"/>
    <xsl:variable name="dd-margin" select="$dt-margin + $offset"/>
    <xsl:call-template name="text:wrap">
      <xsl:with-param name="margin" select="$dt-margin"/>
      <xsl:with-param name="width" select="$line-width"/>
      <xsl:with-param name="input" select="normalize-space($dt)"/>
    </xsl:call-template>
    <xsl:if test="not($dd)">
      <xsl:text>&#10;</xsl:text>
    </xsl:if>
    <xsl:apply-templates select="$dd" mode="make-comment">
      <xsl:with-param name="margin" select="$dd-margin"/>
    </xsl:apply-templates>
  </xsl:template>
  <xsl:template match="*" mode="make-comment"/>
  <!--
    make-heading text level - make a heading for the given text at the
    given level. text is normalized before being ouput
  -->
  <xsl:template name="c:make-heading">
    <xsl:param name="text"/>
    <xsl:param name='level' select='2'/>
    <xsl:if test='not($text)'>
      <xsl:message terminate="yes">No text for heading - exiting</xsl:message>
    </xsl:if>
    <xsl:choose>
      <xsl:when test="$level = 2">
        <xsl:text>&#10;  </xsl:text>
        <xsl:value-of select="normalize-space($text)"/>
        <xsl:text>&#10;</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">Unrecognized heading - exiting</xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="x:div[@class='dtd']" mode="make-comment">
    <xsl:param name="margin"/>
    <xsl:if test="not($margin)">
      <xsl:message terminate="yes">No margin supplied - exiting</xsl:message>
    </xsl:if>
    <xsl:apply-templates mode="make-comment">
      <xsl:with-param name="margin" select="$margin"/>
    </xsl:apply-templates>
  </xsl:template>
  <xsl:template match="x:dd" mode="make-comment">
    <xsl:param name="margin"/>
    <xsl:apply-templates mode="make-comment">
      <xsl:with-param name="margin" select="$margin"/>
    </xsl:apply-templates>
  </xsl:template>
  <xsl:template match="x:dl" mode="make-comment">
    <xsl:param name="margin"/>
    <xsl:variable name="x-children" select="x:dt | x:dd"/>
    <xsl:for-each select="$x-children">
      <xsl:if test="position() mod 2 != 0">
        <xsl:variable name="index" select="position()"/>
        <xsl:call-template name="c:make-dt-entry">
          <xsl:with-param name="margin" select="$margin"/>
          <xsl:with-param name="dt" select="."/>
          <xsl:with-param name="dd" select="$x-children[$index + 1]"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>&#10;</xsl:text>
  </xsl:template>
  <xsl:template match="x:h2" mode="make-comment">
    <xsl:call-template name="c:make-heading">
      <xsl:with-param name="text" select="."/>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="x:ol" mode="make-comment">
    <xsl:param name="margin"/>
    <xsl:for-each select="x:li">
      <xsl:call-template name="str:dup">
        <xsl:with-param name="count" select="$margin"/>
        <xsl:with-param name="input" select="' '"/>
      </xsl:call-template>
      <xsl:number count="*" format="1 "/>
      <xsl:value-of select="."/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>
    <xsl:text>&#10;</xsl:text>
  </xsl:template>
  <xsl:template match="x:p" mode="make-comment">
    <xsl:param name="margin"/>
    <xsl:if test="not($margin)">
      <xsl:message terminate="yes">No margin supplied - exiting</xsl:message>
    </xsl:if>
    <xsl:call-template name="text:wrap">
      <xsl:with-param name="margin" select="$margin"/>
      <xsl:with-param name="width" select="$line-width"/>
    </xsl:call-template>
<!--     <xsl:if test="not(position() = last())"> -->
      <xsl:text>&#10;</xsl:text>
<!--     </xsl:if> -->
  </xsl:template>
  <xsl:template match="x:*" mode="make-comment">
    <xsl:param name="margin"/>
    <xsl:apply-templates mode="make-comment">
      <xsl:with-param name="margin" select="$margin"/>
    </xsl:apply-templates>
  </xsl:template>
</xsl:stylesheet>

<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
		xmlns:m="messages"
		xmlns:sch="http://www.ascc.net/xml/schematron"
		xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias"
		exclude-result-prefixes="sch">

  <!--
    make-message-format - makes a java.text.MessageFormat message, wrapping it in a m:message element for XML procesing
  -->
  <xsl:template name="make-message-format">
    <xsl:param name="id"/>
    <xsl:param name="location"/>
    <xsl:param name="message-body"/>
    <xsl:if test="not($message-body)">
      <xsl:message terminate="yes">No message body - exiting</xsl:message>
    </xsl:if>
    <xsl:if test="not($id)">
      <xsl:message terminate="yes">No message id - exiting</xsl:message>
    </xsl:if>

    <xsl:variable name="first-arg">
      <xsl:if test="$location">{0}:</xsl:if>
    </xsl:variable>
    <xsl:variable name="msg">
      <xsl:apply-templates mode="message-format">
	<xsl:with-param name="params"
			select="$message-body/sch:name[not(@path) and not(preceding-sibling::sch:name[not(@path)])] |
			$message-body/sch:name[@path and not(@path=preceding-sibling::sch:name/@path)] |
			$message-body/sch:value-of[@select and
			not(@select=preceding-sibling::sch:value-of/@select)]"/>
	<xsl:with-param name="first-arg-offset">
	  <xsl:choose>
	    <xsl:when test="$location">0</xsl:when>
	    <xsl:otherwise>1</xsl:otherwise>
	  </xsl:choose>
	</xsl:with-param>
      </xsl:apply-templates>
    </xsl:variable>
    <m:message id="{@id}">
      <xsl:value-of select="normalize-space(concat($first-arg, ' ', $msg))"/>
    </m:message>
  </xsl:template>

  <!-- for each sch:name or sch:value-of argument to the message
  return a string thus: {N}, where N is the ordinal number of the
  argument (starts at 0 for the first arg)
  -->
  
  <xsl:template match="sch:name | sch:value-of" mode="message-format">
	<!-- params contains a list of the first occurrence of each
	     sch:name or sch:value child, in the order in which they
	     occurred.

	     This is used to calculate the parameter number for this child.
	-->
    <xsl:param name="params"/>
    <!-- first-arg-offset contains the offset from zero that each
    param will take -->
    <xsl:param name="first-arg-offset">1</xsl:param>
    <xsl:variable  name="attr" select="@path | @select"/>
    <xsl:variable name="num">
      <xsl:for-each select="$params">
	<xsl:if test="string(@path |@select) = string($attr)">
	  <xsl:value-of select="position()"/>
	</xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:text>{</xsl:text><xsl:value-of select="$num - $first-arg-offset"/><xsl:text>}</xsl:text>
<!--     <xsl:text>{</xsl:text><xsl:value-of select="$num - 1"/><xsl:text>}</xsl:text> -->

  </xsl:template>


  <xsl:template name="make-message-use">
    <xsl:param name="id"/>
    <xsl:param name="message-body"/>
    <xsl:param name="location"/>
    <xsl:if test="not($message-body)">
      <xsl:message terminate="yes">No message body - exiting</xsl:message>
    </xsl:if>
    <xsl:if test="not($id)">
      <xsl:message terminate="yes">No message id for message:
      <xsl:value-of select="$message-body"/>
      - exiting</xsl:message>
    </xsl:if>

    <m:message id="{@id}">
      <xsl:if test="$location">
	<m:param num='0'>
	  <xsl:copy-of select="$location"/>
	</m:param>
      </xsl:if>
      <xsl:apply-templates mode="text"
			select="$message-body/sch:name[not(@path) and not(preceding-sibling::sch:name[not(@path)])] |
			$message-body/sch:name[@path and not(@path=preceding-sibling::sch:name/@path)] |
			$message-body/sch:value-of[@select and not(@select=preceding-sibling::sch:value-of/@select)]">
	<xsl:with-param name="first-arg-offset">
	  <xsl:choose>
	    <xsl:when test="$location">0</xsl:when>
	    <xsl:otherwise>1</xsl:otherwise>
	  </xsl:choose>
	</xsl:with-param>
      </xsl:apply-templates>
    </m:message>

  </xsl:template>
<!--   <xsl:template name="make-message-use"> -->
<!--     <xsl:param name="id"/> -->
<!--     <xsl:param name="message-body"/> -->
<!--     <xsl:if test="not($message-body)"> -->
<!--       <xsl:message terminate="yes">No message body - exiting</xsl:message> -->
<!--     </xsl:if> -->
<!--     <xsl:if test="not($id)"> -->
<!--       <xsl:message terminate="yes">No message id for message: -->
<!--       <xsl:value-of select="$message-body"/> -->
<!--       - exiting</xsl:message> -->
<!--     </xsl:if> -->
<!--     <m:message id="{@id}"> -->
<!--       <xsl:apply-templates mode="text" -->
<!-- 			select="$message-body/sch:name[not(@path) and not(preceding-sibling::sch:name[not(@path)])] | -->
<!-- 			$message-body/sch:name[@path and not(@path=preceding-sibling::sch:name/@path)] | -->
<!-- 			$message-body/sch:value-of[@select and not(@select=preceding-sibling::sch:value-of/@select)]"/> -->
<!--     </m:message> -->

<!--   </xsl:template> -->



<xsl:template match="text() | * " mode="text"/><!-- squish text output -->


<xsl:template match="sch:name | sch:value-of" mode="text">
  <xsl:param name="first-arg-offset"/>
  <m:param num="{position() - $first-arg-offset}">
    <xsl:apply-templates mode="make-param" select="."/>
  </m:param>
</xsl:template>


<xsl:template match="sch:value-of" mode="make-param">
  <xsl:choose>
    <xsl:when test="not(@select)">
      <axsl:value-of select="."/>
    </xsl:when>
    <xsl:otherwise>
      <axsl:value-of select="{@select}"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
<xsl:template match="sch:name" mode="make-param">
  <xsl:choose>
    <xsl:when test="not(@path)">
      <axsl:value-of select="name(.)"/>
    </xsl:when>
    <xsl:otherwise>
      <axsl:value-of select="name({@path})"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


</xsl:stylesheet>
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<axsl:stylesheet xmlns:axsl="http://www.w3.org/1999/XSL/Transform" xmlns:sch="http://www.ascc.net/xml/schematron" version="1.0">
<axsl:output xmlns:m="messages" indent="yes" method="xml"/>
<axsl:template xmlns:m="messages" mode="sfp" match="@*">
<axsl:text>[@</axsl:text>
<axsl:value-of select="name()"/>
<axsl:text>='</axsl:text>
<axsl:value-of select="."/>
<axsl:text>']</axsl:text>
</axsl:template>
<axsl:template mode="schematron-get-full-path" match="*|@*">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:if test="count(. | ../@*) = count(../@*)">@</axsl:if>
<axsl:value-of select="name()"/>
<axsl:text>[</axsl:text>
<axsl:value-of select="1+count(preceding-sibling::*[name()=name(current())])"/>
<axsl:text>]</axsl:text>
</axsl:template>
<axsl:template match="/">
<m:messages xmlns:m="messages">
<axsl:apply-templates mode="M0" select="/"/>
<axsl:apply-templates mode="M1" select="/"/>
</m:messages>
</axsl:template>
<axsl:template mode="M0" priority="3999" match="foo">
<axsl:choose>
<axsl:when test="some/test"/>
<axsl:otherwise>
<m:message xmlns:m="messages" id="t1">
<m:param num="0">
<axsl:apply-templates select="." mode="schematron-get-full-path"/>
</m:param>
</m:message>
<axsl:text xmlns:m="messages" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M0"/>
</axsl:template>
<axsl:template mode="M0" priority="-1" match="text()"/>
<axsl:template mode="M1" priority="4000" match="foo">
<axsl:choose>
<axsl:when test="a/test"/>
<axsl:otherwise>
<m:message xmlns:m="messages" id="a1">
<m:param num="0">
<axsl:apply-templates select="." mode="schematron-get-full-path"/>
</m:param>
<m:param num="1">
<axsl:value-of select="name(.)"/>
</m:param>
<m:param num="2">
<axsl:value-of select="name(..)"/>
</m:param>
<m:param num="3">
<axsl:value-of select="name(fir/text())"/>
</m:param>
</m:message>
<m:message xmlns:m="messages" id="d1">
<m:param num="0">
<axsl:value-of select="./text()"/>
</m:param>
<m:param num="1">
<axsl:value-of select="1"/>
</m:param>
<m:param num="2">
<axsl:value-of select="."/>
</m:param>
<m:param num="3">
<axsl:value-of select="fir/text()"/>
</m:param>
</m:message>
<axsl:text xmlns:m="messages" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M1"/>
</axsl:template>
<axsl:template mode="M1" priority="-1" match="text()"/>
<axsl:template priority="-1" match="text()"/>
</axsl:stylesheet>

<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.

    The contents of this file are subject to the terms of either the GNU
    General Public License Version 2 only ("GPL") or the Common Development
    and Distribution License("CDDL") (collectively, the "License").  You
    may not use this file except in compliance with the License.  You can
    obtain a copy of the License at
    https://oss.oracle.com/licenses/CDDL+GPL-1.1
    or LICENSE.txt.  See the License for the specific
    language governing permissions and limitations under the License.

    When distributing the software, include this License Header Notice in each
    file and include the License file at LICENSE.txt.

    GPL Classpath Exception:
    Oracle designates this particular file as subject to the "Classpath"
    exception as provided by Oracle in the GPL Version 2 section of the License
    file that accompanied this code.

    Modifications:
    If applicable, add the following below the License Header, with the fields
    enclosed by brackets [] replaced by your own identifying information:
    "Portions Copyright [year] [name of copyright owner]"

    Contributor(s):
    If you wish your version of this file to be governed by only the CDDL or
    only the GPL Version 2, indicate your decision by adding "[Contributor]
    elects to include this software in this distribution under the [CDDL or GPL
    Version 2] license."  If you don't indicate a single choice of license, a
    recipient has the option to distribute your version of this file under
    either the CDDL, the GPL Version 2 or to extend the choice of license to
    its licensees as provided above.  However, if you add GPL Version 2 code
    and therefore, elected the GPL Version 2 license, then the option applies
    only if the new code is made subject to such option by the copyright
    holder.

-->

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

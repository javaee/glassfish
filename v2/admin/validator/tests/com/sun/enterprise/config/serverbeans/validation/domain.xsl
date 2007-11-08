<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<axsl:stylesheet xmlns:axsl="http://www.w3.org/1999/XSL/Transform" xmlns:sch="http://www.ascc.net/xml/schematron" version="1.0" xmlns:t="http:///xsl-tests" t:dummy-for-xmlns="">
<axsl:output xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" method="text"/>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="admin-object-resource">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@jndi-name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="appclient-module">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="application-ref">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@ref"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="audit-module">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="auth-realm">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="cluster">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="config">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="connector-connection-pool">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="connector-module">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="connector-resource">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@jndi-name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="custom-resource">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@jndi-name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="ejb-module">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="external-jndi-resource">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@jndi-name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="http-listener">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@id"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="iiop-listener">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@id"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="j2ee-application">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="jacc-provider">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="jdbc-connection-pool">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="jdbc-resource">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@jndi-name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="jms-host">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="jmx-connector">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="lb-cluster">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="lb-instance">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="lifecycle-module">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="loadbalancer-config">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="mail-resource">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@jndi-name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="node-agent">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="profiler">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="resource-adapter-config">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@resource-adapter-name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="resource-ref">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@ref"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="security-map">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="server">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="server-ref">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@ref"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="system-property">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="thread-pool">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@thread-pool-id"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="virtual-server">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@id"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="web-module">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="schematron-get-full-path" match="persistence-manager-factory-resource">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:value-of select="name()"/>
<axsl:apply-templates mode="sfp" select="@jndi-name"/>
</axsl:template>
<axsl:template xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" mode="sfp" match="@*">
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
<axsl:key xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" use="@name" match="config" name="configs"/>
<axsl:key xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" use="@jndi-name" match="jdbc-resource" name="jdbc-resources"/>
<axsl:key xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" use="@name" match="applications/*" name="applications"/>
<axsl:key xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" use="@jndi-name |                @resource-adapter-name | @name" match="resources/*" name="resources"/>
<axsl:key xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" use="@name" match="configs/* | servers/* |               clusters/* | node-agents/* | lb-configs/*" name="system-objects"/>
<axsl:template match="/">
<axsl:apply-templates mode="M1" select="/"/>
<axsl:apply-templates mode="M2" select="/"/>
<axsl:apply-templates mode="M3" select="/"/>
<axsl:apply-templates mode="M4" select="/"/>
</axsl:template>
<axsl:template mode="M1" priority="-1" match="text()"/>
<axsl:template mode="M2" priority="3999" match="admin-object-resource">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@jndi-name=current()/@jndi-name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their jndi-name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@jndi-name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3998" match="admin-service">
<axsl:choose>
<axsl:when test="@system-jmx-connector-name and jmx-connector[@name=current()/@system-jmx-connector-name]"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: An<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element which has a system-jmx-connector-name attribute uses that attribute to refer to an existing jmx-connector child element.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3997" match="appclient-module">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3996" match="application-ref">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@ref=current()/@ref])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their ref attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@ref"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(contains('1yestrueon', @enabled)) or key('applications',@ref)"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>elements which are enabled must refer to existing applications
Reference "<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@ref"/>
<axsl:text xml:space="preserve"> </axsl:text>" not found.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(key('applications',@ref)[@object-type='system-admin'])                   or                   key('configs',current()/../@config-ref)/admin-service[@type!='server']                   "/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Administrative applications must only be deployed to a DAS server<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(key('applications',@ref)[@object-type='system-instance'])                   or                   key('configs',current()/../@config-ref)/admin-service[@type = 'server']                   "/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Instance applications must only be deployed to non-DAS servers<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(string(normalize-space(@virtual-servers)))                   or                   count(//virtual-server[ancestor::config[@name=current()/parent::server[not(@name=//cluster/server-ref/@ref)]/@config-ref]][contains(concat(normalize-space(current()/@virtual-servers),','),                            concat(normalize-space(@id), ','))] |                   //virtual-server[ancestor::config[@name=//cluster[server-ref/@ref=current()/../@name]/@config-ref]][contains(current()/@virtual-servers, @id)])=                   string-length(translate(normalize-space(@virtual-servers),                   translate(normalize-space(@virtual-servers), ',', ''),                   ''))+1"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: All virtual servers in the virtual-servers list exist as children of the config associated with the parent server element.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3995" match="applications">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3994" match="audit-module">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3993" match="auth-realm">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3992" match="availability-service">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3991" match="backend-principal">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3990" match="cluster">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(@config-ref='default-config')"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: No reference is made to the default-configconfig element.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not (@config-ref) or key('configs',@config-ref)"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>elements config-ref attribute names an existing config element.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3989" match="clusters">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3988" match="config">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3987" match="configs">
<axsl:choose>
<axsl:when test="config/admin-service[@type='das-and-server'                   or @type='das']"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: There must be an admin-service element configured for the DAS
There must be one (and only one) admin-service for the DAS. An admin-service for the DAS is an admin-service element whose type attribute has the value "das" or "das-and-server"<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="count(config/admin-service[@type='das-and-server'                   or @type='das']) &lt; 2"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: There must be only one admin-service element configured for the DAS
There must be one (and only one) admin-service for the DAS. An admin-service for the DAS is an admin-service element whose type attribute has the value "das" or "das-and-server"<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="config[@name='default-config']"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: There must be a config element named "default-config"<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="count(config[@name='default-config']) &lt; 2"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: There must be only one config element named "default-config" <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3986" match="connection-pool">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3985" match="connector-connection-pool">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3984" match="connector-module">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3983" match="connector-resource">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@jndi-name=current()/@jndi-name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their jndi-name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@jndi-name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3982" match="connector-service">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3981" match="custom-resource">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@jndi-name=current()/@jndi-name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their jndi-name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@jndi-name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3980" match="das-config">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3979" match="description">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3978" match="domain">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3977" match="ejb-container">
<axsl:choose>
<axsl:when test="@steady-pool-size &lt;= @max-pool-size"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Steady pool size is not greater than the maximum pool size<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3976" match="ejb-container-availability">
<axsl:choose>
<axsl:when test="not(contains('1yestrueon', ../@availability-enabled) and                  (not(string(@availability-enabled)) or                  contains('1yestrueon', @availability-enabled)) and                  not(string(normalize-space(@sfsb-store-pool-name))))              or key('jdbc-resources', ../@store-pool-name)"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: The store-pool-name attribute of the parent of an<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element must reference an existing jdbc-resource when the<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element is enabled but doesn't have an sjsb-store-pool-name attribute.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(contains('1yestrueon', ../@availability-enabled) and                    (not(string(@availability-enabled)) or contains('1yestrueon', @availability-enabled))                   and string(normalize-space(@sfsb-store-pool-name)))               or key('jdbc-resources',@sfsb-store-pool-name)"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: An<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element whose parent is enabled, which is enabled itself, and which has a non-empty sfsb-store-pool-name attribute references an existing jdbc-resource<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3975" match="ejb-module">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3974" match="ejb-timer-service">
<axsl:choose>
<axsl:when test="not(@timer-datasource) or key('jdbc-resources',@timer-datasource)"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: If an<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element has a timer-datasource attribute then the value of that attribute must be the jndi-name of a jdbc-resource element.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3973" match="external-jndi-resource">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@jndi-name=current()/@jndi-name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their jndi-name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@jndi-name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3972" match="health-checker">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3971" match="http-access-log">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3970" match="http-file-cache">
<axsl:choose>
<axsl:when test="@medium-file-size-limit-in-bytes &lt;= @medium-file-space-in-bytes"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: The medium file size limit is not greater than the medium file space<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="@small-file-size-limit-in-bytes &lt;= @small-file-space-in-bytes"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: The small file size limit is not greater than the small file space<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3969" match="http-listener">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@id=current()/@id])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their id attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@id"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(@default-virtual-server) or ../virtual-server[@id = current()/@default-virtual-server]"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: If an<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element references a default virtual server then that virtual server must exist as a peer of the referencing<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>
This http-listener (id=<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@id"/>
<axsl:text xml:space="preserve"> </axsl:text>) refers to a default-virtual-server (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@default-virtual-server"/>
<axsl:text xml:space="preserve"> </axsl:text>) but this server is not part of this config (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="../../@name"/>
<axsl:text xml:space="preserve"> </axsl:text>)<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3968" match="http-protocol">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3967" match="http-service">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3966" match="idempotent-url-pattern">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3965" match="iiop-listener">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@id=current()/@id])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their id attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@id"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3964" match="iiop-service">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3963" match="j2ee-application">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3962" match="jacc-provider">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3961" match="java-config">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3960" match="jdbc-connection-pool">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="@steady-pool-size &lt;= @max-pool-size"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Steady pool size is not greater than the maximum pool size<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(@connection-validation-method='table') or @validation-table-name"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: When the connection-validation-method attribute has the value 'table' then the validation-table-name attribute must be supplied.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3959" match="jdbc-resource">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@jndi-name=current()/@jndi-name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their jndi-name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@jndi-name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(contains('1yestrueon', @enabled)) or ../jdbc-connection-pool[@name=current()/@pool-name]"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: When the<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>is enabled then the pool-name attribute names an existing jdbc-connection-pool<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3958" match="jms-host">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3957" match="jms-service">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3956" match="jmx-connector">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="../../security-service/auth-realm[@name=current()/@auth-realm-name]"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: The auth-realm element named in the auth-realm-name attribute exists.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3955" match="jvm-options">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3954" match="keep-alive">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3953" match="lb-cluster">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3952" match="lb-instance">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3951" match="lb-web-module">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3950" match="lifecycle-module">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3949" match="loadbalancer-config">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3948" match="loadbalancers">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3947" match="log-service">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3946" match="mail-resource">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@jndi-name=current()/@jndi-name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their jndi-name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@jndi-name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3945" match="manager-properties">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3944" match="mdb-container">
<axsl:choose>
<axsl:when test="@steady-pool-size &lt;= @max-pool-size"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Steady pool size is not greater than the maximum pool size<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3943" match="module-log-levels">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3942" match="module-monitoring-levels">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3941" match="monitoring-service">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3940" match="node-agent">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3939" match="node-agents">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3938" match="orb">
<axsl:choose>
<axsl:when test="count(ancestor::config//thread-pool[contains(concat(normalize-space(current()/@use-thread-pool-ids),','),                                                      concat(normalize-space(@thread-pool-id),','))])                   =                   string-length(translate(normalize-space(current()/@use-thread-pool-ids),                   translate(normalize-space(current()/@use-thread-pool-ids), ',',''),                   '')) + 1"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Within an <axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element all the thread pools referenced by the use-thread-pool-ids attribute exist.
Location: config name=<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="ancestor::config/@name"/>
<axsl:text xml:space="preserve"> </axsl:text>orb @use-thread-pool-ids=<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@use-thread-pool-ids"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3937" match="principal">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3936" match="profiler">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3935" match="property">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3934" match="quorum-service">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3933" match="request-processing">
<axsl:choose>
<axsl:when test="@initial-thread-count &lt;= @thread-count"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: The initial-thread-count is no greater than the thread-count<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3932" match="resource-adapter-config">
<axsl:choose>
<axsl:when test="not(preceding-sibling::resource-adapter-config[@resource-adapter-name=current()/@resource-adapter-name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>elements are uniquely named by their resource-adapter-name attribute.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(string(normalize-space(@thread-pool-ids)))              or              not(//config[@name=//server[not(@name=//server-ref/@ref)][resource-ref/@ref=current()/@resource-adapter-name]/@config-ref] [count(thread-pool[contains(concat(normalize-space(current()/@thread-pool-ids),','),                                                      concat(normalize-space(@thread-pool-id),','))])                           &lt;                           string-length(translate(normalize-space(current()/@thread-pool-ids),                                                   translate(normalize-space(current()/@thread-pool-ids), ',',''),                                         '')) + 1] | //config[@name=//cluster[resource-ref/@ref=current()/@resource-adapter-name]/@config-ref]                          [count(thread-pool[contains(concat(normalize-space(current()/@thread-pool-ids),','),                                                      concat(normalize-space(@thread-pool-id),','))])                           &lt;                           string-length(translate(normalize-space(current()/@thread-pool-ids),                                                   translate(normalize-space(current()/@thread-pool-ids), ',',''),                                         '')) + 1] )"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: All the thread pools referenced by the thread-pool-ids attribute exist in the related config.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3931" match="resource-ref">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@ref=current()/@ref])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their ref attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@ref"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(contains('1yestrueon', @enabled)) or key('resources', @ref)"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: resource-ref elements which are enabled must refer to existing resources
Reference "<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@ref"/>
<axsl:text xml:space="preserve"> </axsl:text>" not found.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(key('resources',@ref)[@object-type='system-admin'])                   or                   key('configs',current()/../@config-ref)/admin-service[@type!='server']                   "/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Administrative resources must only be deployed to a DAS server<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(key('resources',@ref)[@object-type='system-instance'])                   or                   key('configs',current()/../@config-ref)/admin-service[@type = 'server']                   "/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Instance resources must only be deployed to non-DAS servers<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3930" match="resources">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3929" match="security-map">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3928" match="security-service">
<axsl:choose>
<axsl:when test="//jacc-provider[@name=current()/@jacc]"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: The referenced jacc-provider exists<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3927" match="server">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(@config-ref='default-config')"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: No reference is made to the default-configconfig element.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not (@config-ref) or key('configs',@config-ref)"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>elements config-ref attribute names an existing config element.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="                   not(//config[@name=//server[@name=current()/@name][not(//server-ref/@ref=current()/@name)]/@config-ref]/admin-service[@type!='server'] |                       //config[@name=//cluster[server-ref/@ref=current()/@name]/@config-ref]/admin-service[@type!='server'])                         or not(@node-agent-ref)"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>elements which are configured to run the das do not also run a node agent<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(@node-agent-ref) or //node-agent[@name=current()/@node-agent-ref]"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>elements which refer to a node-agent must reference an existing node-agent
This server ("<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>") is referring to an unknown node-agent "<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@node-agent-ref"/>
<axsl:text xml:space="preserve"> </axsl:text>"<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="count(//cluster[server-ref/@ref=current()/@name]) &lt; 2"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>elements are not referenced by more than one cluster element.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3926" match="server-ref">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@ref=current()/@ref])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their ref attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@ref"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="//server[@name=current()/@ref]"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>elements refer to existing server elements<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3925" match="servers">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3924" match="session-config">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3923" match="session-manager">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3922" match="session-properties">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3921" match="ssl">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3920" match="ssl-client-config">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3919" match="store-properties">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3918" match="system-property">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3917" match="thread-pool">
<axsl:choose>
<axsl:when test="not(preceding-sibling::thread-pool[@thread-pool-id=current()/@thread-pool-id])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>elements are uniquely named by their thread-pool-id attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@thread-pool-id"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="@min-thread-pool-size &lt; @max-thread-pool-size"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Within a <axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element the min-thread-pool-size is less than the max-thread-pool-size<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3916" match="thread-pools">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3915" match="transaction-service">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3914" match="user-group">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3913" match="virtual-server">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@id=current()/@id])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their id attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@id"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="count(../http-listener[contains(concat(normalize-space(current()/@http-listeners),','),                                                         concat(normalize-space(@id),','))])                   =                   string-length(translate(normalize-space(current()/@http-listeners),                   translate(normalize-space(current()/@http-listeners), ',', ''),                   '')) + 1"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Every http-listener within the list of http listeners exists as a sibling of the<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(@default-web-module) or //web-module[@name=current()/@default-web-module]"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Within a<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>elment if the default-web-module attribute has a value then that value must be the name of an existing web-module<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(string(normalize-space(@default-web-module))) or                   //cluster[@config-ref=current()/ancestor::config/@name]/application-ref[@ref=current()/@default-web-module] |                   //server[not(@name=//server-ref/@ref)][@config-ref=current()/ancestor::config/@name]/application-ref[@ref=current()/@default-web-module]"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: The default-web-module has been deployed to every server and cluster which uses the containing config
Location: config name=<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="ancestor::config/@name"/>
<axsl:text xml:space="preserve"> </axsl:text>virtual-server name=<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@id"/>
<axsl:text xml:space="preserve"> </axsl:text>
default-web-module=<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@default-web-module"/>
<axsl:text xml:space="preserve"> </axsl:text>has not been deployed to any server or cluster referenced by the containing config.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3912" match="web-container">
<axsl:choose>
<axsl:when test="true()"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3911" match="web-container-availability">
<axsl:choose>
<axsl:when test="not(contains('1yestrueon', ../@availability-enabled) and                  (not(string(@availability-enabled)) or                  contains('1yestrueon', @availability-enabled)) and                  not(string(normalize-space(@http-session-store-pool-name))))              or key('jdbc-resources', ../@store-pool-name)"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: The store-pool-name attribute of the parent of an<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element must reference an existing jdbc-resource when the<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element is enabled but doesn't have a sjsb-store-pool-name attribute.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(contains('1yestrueon', ../@availability-enabled) and                    (not(string(@availability-enabled)) or contains('1yestrueon', @availability-enabled))                   and string(normalize-space(@http-session-store-pool-name)))               or key('jdbc-resources',@http-session-store-pool-name)"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: A<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element whose parent is enabled, which is enabled itself, and which has a non-empty http-session-store-pool-name attribute references an existing jdbc-resource<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="3910" match="web-module">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@name=current()/@name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M2"/>
</axsl:template>
<axsl:template mode="M2" priority="-1" match="text()"/>
<axsl:template mode="M3" priority="3999" match="applications/*">
<axsl:choose>
<axsl:when test="count(key('applications', @name)) = 1"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Applications are uniquely named by their name attribute.
Two or more applications under the &lt;applications&gt; element have the same name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M3"/>
</axsl:template>
<axsl:template mode="M3" priority="3998" match="resources/*">
<axsl:choose>
<axsl:when test="count(key('resources', @name) |                   key('resources', @jndi-name) | key('resources',                   @resource-adapter-name)) = 1"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Resources are uniquely named by either their name, jndi-name or resource-adapter-name attribute.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M3"/>
</axsl:template>
<axsl:template mode="M3" priority="3997" match="configs/* | servers/* |               clusters/* | node-agents/* | lb-configs/*">
<axsl:choose>
<axsl:when test="count(key('system-objects', @name)) = 1"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: System objects are uniquely named by their name attribute.
Two or more system objects (clusters, configs, lb-configs, node-agents servers) have the same name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M3"/>
</axsl:template>
<axsl:template mode="M3" priority="-1" match="text()"/>
<axsl:template mode="M4" priority="4000" match="applications/*">
<axsl:choose>
<axsl:when test="//application-ref[@ref=current()/@name]"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Every application is referenced by some application-ref<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M4"/>
</axsl:template>
<axsl:template mode="M4" priority="3999" match="availability-service">
<axsl:choose>
<axsl:when test="not(contains('1yestrueon', @availability-enabled))                   or (ejb-container-availability | web-container-availability)"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: <axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>elements which are enabled have children<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M4"/>
</axsl:template>
<axsl:template mode="M4" priority="3998" match="config[not(@name='default-config')]">
<axsl:choose>
<axsl:when test="//server[not(@name=//server-ref/@ref)][@config-ref=current()/@name] | //cluster[@config-ref=current()/@name]"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Every<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>is referenced by either a cluster, or a server not in a cluster.<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M4"/>
</axsl:template>
<axsl:template mode="M4" priority="3997" match="ejb-container-availability">
<axsl:choose>
<axsl:when test="not((contains('0noofffalse', ../@availability-enabled) or                    (string(@availability-enabled) and                     contains('0noofffalse', @availability-enabled)))                    and                   (string(normalize-space(../@store-pool-name)) and                    not(string(normalize-space(@sfsb-store-pool-name)))))              or key('jdbc-resources', normalize-space(../@store-pool-name))"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: A disabled<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element's parent's store-pool-name attribute should reference an existing jdbc-resource<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not((contains('0noofffalse', ../@availability-enabled) or                    (string(@availability-enabled) and                     contains('0noofffalse', @availability-enabled)))                    and                  string(normalize-space(@sfsb-store-pool-name)))              or key('jdbc-resources', normalize-space(@sfsb-store-pool-name))"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: A disabled<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element's sfsb-store-pool-name references a known jdbc-resource<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M4"/>
</axsl:template>
<axsl:template mode="M4" priority="3996" match="persistence-manager-factory-resource">
<axsl:choose>
<axsl:when test="not(preceding-sibling::*[name()=name(current())][./@jndi-name=current()/@jndi-name])"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Sibling elements of the same kind (<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>) are uniquely named by their jndi-name attribute.
Duplicate name:<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="@jndi-name"/>
<axsl:text xml:space="preserve"> </axsl:text>
<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not(@jdbc-resource-jndi-name) or key('jdbc-resources',@jdbc-resource-jndi-name)"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: If a<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element references a jdbc resource then that jdbc resource must exist<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M4"/>
</axsl:template>
<axsl:template mode="M4" priority="3995" match="web-container-availability">
<axsl:choose>
<axsl:when test="not((contains('0noofffalse', ../@availability-enabled) or                    (string(@availability-enabled) and                     contains('0noofffalse', @availability-enabled)))                    and                   (string(normalize-space(../@store-pool-name)) and                    not(string(normalize-space(@http-session-store-pool-name)))))              or key('jdbc-resources', normalize-space(../@store-pool-name))"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: A disabled<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element's parent's store-pool-name attribute should reference an existing jdbc-resource<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:choose>
<axsl:when test="not((contains('0noofffalse', ../@availability-enabled) or                    (string(@availability-enabled) and                     contains('0noofffalse', @availability-enabled)))                    and                  string(normalize-space(@http-session-store-pool-name)))              or key('jdbc-resources', normalize-space(@http-session-store-pool-name))"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: A disabled<axsl:text xml:space="preserve"> </axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:text xml:space="preserve"> </axsl:text>element's http-session-store-pool-name references a known jdbc-resource<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M4"/>
</axsl:template>
<axsl:template mode="M4" priority="3994" match="resources/*">
<axsl:choose>
<axsl:when test="//resource-ref[@ref=(current()/@name |                   current()/@jndi-name | current()/resource-adapter-name)]"/>
<axsl:otherwise>


(<axsl:apply-templates xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" select="." mode="schematron-get-full-path"/>) Assertion failed: Every resource is referenced by some resource-ref<axsl:text xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo" xml:space="preserve">
</axsl:text>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M4"/>
</axsl:template>
<axsl:template mode="M4" priority="-1" match="text()"/>
<axsl:template priority="-1" match="text()"/>
</axsl:stylesheet>

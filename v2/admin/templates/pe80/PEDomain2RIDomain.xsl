<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="1.0">

    <xsl:output method="xml" indent="no"
        doctype-public="-//Sun Microsystems Inc.//DTD Sun ONE Application Server 8.0//EN"
        doctype-system="http://www.sun.com/software/appserver/dtds/sun-domain_1_2.dtd"/> 

    <!-- identity transformation -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!--remove the admingui web module-->
    <xsl:template match="web-module[@name='admingui']">
    </xsl:template>

    <!--remove the admingui web module-->
    <xsl:template match="application-ref[@ref='admingui']">
    </xsl:template>

    <!--remove the com_sun_web_ui web module-->
    <xsl:template match="web-module[@name='com_sun_web_ui']">
    </xsl:template>

    <!--remove the jvm-option -Dcom.sun.web.console.appbase-->
    <xsl:template match="jvm-options[starts-with(current(), '-Dcom.sun.web.console.appbase')]">
    </xsl:template>
</xsl:stylesheet>


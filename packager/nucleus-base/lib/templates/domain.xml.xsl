<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml"
          indent="yes"
          doctype-public="-//Sun Microsystems Inc.//DTD Application Server 9.1 Domain//EN"
	  doctype-system="http://www.sun.com/software/appserver/dtds/sun-domain_1_3.dtd">
  </xsl:output>
  
  <xsl:template match="/">
      <xsl:apply-templates select="node()|@*"/>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template> 

    <xsl:template match="jmx-connector">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:element name="ssl">
        <xsl:attribute name="cert-nickname">s1as</xsl:attribute>
      </xsl:element>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="java-config">
    <xsl:copy>                    
      <xsl:apply-templates select="node()|@*"/>
      <xsl:comment>
        Use the following jvm-options element to disable the quick startup:
	com.sun.enterprise.server.ss.ASQuickStartup=false
      </xsl:comment>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

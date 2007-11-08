<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"   xmlns:text="http://www.ora.com/XSLTCookbook/namespaces/text" exclude-result-prefixes="text" xmlns:x="foobar">
  <xsl:output method="text"/>
  <xsl:include href="text.wrap.xslt"/>
  <xsl:template match="x:div">
    <xsl:call-template name="text:wrap">
      <xsl:with-param name="width" select="20"/>
      <xsl:with-param name="align" select="'right'"/>
      <xsl:with-param name="margin" select="4"/>
    </xsl:call-template>
  </xsl:template>
  <x:div>
            The configuration defines the configuration of a server
        instance that can be shared by other server instances. The
        availability-service and are SE/EE only.
  </x:div>
</xsl:stylesheet>

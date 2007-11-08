<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:ext="http://xml.apache.org/xalan/org.apache.xalan.lib.NodeInfo">
  <xsl:output method="text"/>
  <xsl:template match="root">This is the id: <xsl:value-of select="@id"/></xsl:template>
</xsl:stylesheet>

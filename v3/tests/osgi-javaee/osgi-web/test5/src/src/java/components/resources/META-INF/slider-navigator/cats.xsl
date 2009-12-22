<?xml version="1.0" encoding="UTF-8" ?>

<!-- Copyright 2005 Sun Microsystems, Inc.  All rights reserved.
  You may not modify, use, reproduce, or distribute this software except
  in compliance with the terms of the License at: 
  http://developer.sun.com/berkeley_license.html
  $Id: cats.xsl,v 1.1 2005/11/19 00:54:42 inder Exp $ -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

  <!-- supposed to be set externally to specify the entries higher than -->
  <xsl:param name="id"/>

  <xsl:template match="/">
    <h5>
      About this cat : <xsl:value-of select="$id"/> 
    </h5>
    
    <script type="text/javascript">
    <xsl:comment>
    <![CDATA[
      function updateField() {
      }
    // ]]>
    </xsl:comment>
    </script>
    
    <table>
    <tr><th></th><th>Date</th><th>Name</th><th>Description</th></tr>
    <xsl:apply-templates select="cat-list/cat[name=$id]"/>
    </table>
  </xsl:template>
  
  <!-- process each element cat -->
  <xsl:template match="cat">
    <xsl:variable name="upperDir">../</xsl:variable>
    <xsl:variable name="imagePath" select="image"/>
    <tr>
      <td>
        <img>
          <xsl:attribute name="src">
            <xsl:value-of select="concat($upperDir, $imagePath)"/>
          </xsl:attribute>
        </img>
      </td>
      <td>
      <xsl:value-of select="date"/>
      </td>
      <td>
      <xsl:value-of select="name"/>
      </td>
      <td>
      <xsl:value-of select="description"/>
      </td>
    </tr>
    
  </xsl:template>

</xsl:stylesheet>
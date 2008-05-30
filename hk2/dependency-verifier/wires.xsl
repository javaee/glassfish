<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
  <body>
    <h2>Package wiring details</h2>
    <table border="1">
    <tr bgcolor="#9acd32">
      <th align="left">Package</th>
      <th align="left">Exporter(s)</th>
      <th align="left">Importer(s)</th>
    </tr>
    <xsl:for-each select="Wires/Package">
    <tr>
      <td><xsl:value-of select="@name"/></td>
      <td><xsl:value-of select="Exporters"/></td>
      <td><xsl:value-of select="Importers"/></td>
    </tr>
    </xsl:for-each>
    </table>
  </body>
  </html>
</xsl:template>

</xsl:stylesheet>

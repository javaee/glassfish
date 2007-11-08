  <xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="text()">
    <h1><xsl:value-of select="."/></h1>
  </xsl:template>

  </xsl:stylesheet>

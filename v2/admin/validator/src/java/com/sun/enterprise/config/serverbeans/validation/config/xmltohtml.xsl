<?xml version="1.0" encoding="ISO-8859-1"?>

                  <xsl:stylesheet version="1.0"
                  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

                  <xsl:template match="/">
                    <html>
                    <body>
                      <h2>Configuration Validation - Result </h2>
                      <table border="1">
                      <tr bgcolor="#9acd32">
                        <th align="center">Element Name</th>
                        <th align="center">KEY ID</th>
                        <th align="center">Summary</th>
                      </tr>
                      <xsl:for-each select="elements/element">
                      <tr>
			<td><xsl:value-of select="name"/></td>
			<td><xsl:value-of select="key"/></td>
			<td><xsl:value-of select="failed"/></td> 
                      </tr>
                      </xsl:for-each>
		      <xsl:for-each select="elements">
	              <tr>
			 <xsl:value-of select="description"/> 
		      </tr>			
                      </xsl:for-each>
                      </table>
                    </body>
                    </html>
                  </xsl:template>

                  </xsl:stylesheet>

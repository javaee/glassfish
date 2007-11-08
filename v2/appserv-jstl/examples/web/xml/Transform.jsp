<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<html>
<head>
  <title>JSTL: XML Support -- Transform</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Parse / Expr</h3>

<c:set var="xml">
  <a><b>header!</b></a>
</c:set>

<c:set var="xsl">
  <?xml version="1.0"?>
  <xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="text()">
    <h1><xsl:value-of select="."/></h1>
  </xsl:template>

  </xsl:stylesheet>
</c:set>

Prints "header" as a header:<br />
<x:transform doc="${xml}" xslt="${xsl}"/>

<hr />

Prints "header" in normal size:<br />
<x:transform doc="${xml}" xslt="${xsl}" var="doc"/>
<x:out select="$doc//h1"/>

<hr size="5" />

<hr />
<h3>Transformations using output from XPath expressions</h3>

<x:parse var="xml" doc="${xml}" />
<x:set var="miniDoc" select="$xml//b" />
<x:transform xslt="${xsl}" doc="${miniDoc}" />
<hr />

<h3>Inline transformations</h3>

<x:transform xslt="${xsl}">
  <a>
   <b>
    <c>Paragraph one!</c>
    <c>Paragraph foo!</c>
   </b>
  </a>
</x:transform>

</body>
</html>

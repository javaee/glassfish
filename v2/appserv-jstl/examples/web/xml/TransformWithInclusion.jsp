<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<html>
<head>
  <title>JSTL: XML Support -- Transform</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Parse / Expr</h3>

<c:set var="xml">
<?xml version="1.0"?>

<!DOCTYPE project [
    <!ENTITY included SYSTEM "included.xml">
]>
<root>
  &included;
</root>
</c:set>

<c:set var="xsl">
  <?xml version="1.0"?>
  <xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:import href="/xml/includedStylesheet.xsl" />

  </xsl:stylesheet>
</c:set>

<x:transform doc="${xml}" docSystemId="foo.xml" xslt="${xsl}"/>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<html>
<head>
  <title>JSTL: XML Support -- Parse</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Parse from Objects and URLs</h3>

<c:set var="xmlText">
<?xml version="1.0"?>

<!DOCTYPE project [
    <!ENTITY included SYSTEM "included.xml">
]>
<root>
  &included;
</root>
</c:set>    

<x:parse var="a" doc="${xmlText}" systemId="foo.xml"/>

<x:out select="$a//*"/>

</body>
</html>

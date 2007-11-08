<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<html>
<head>
  <title>JSTL: XML Support -- Parse</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Parse from Objects and URLs</h3>

<c:set var="xmlText">
  <a>
   <b>
    <c>
     foo
    </c>
   </b>
   <d>
     bar
   </d>
  </a>
</c:set>    

<x:parse var="a" doc="${xmlText}" />

<x:out select="$a//c"/>
<x:out select="$a/a/d"/>

</body>
</html>

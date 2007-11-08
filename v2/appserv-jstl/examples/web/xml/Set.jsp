<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<html>
<head>
  <title>JSTL: XML Support -- Parse / Set / Out</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Parse / Set / Out</h3>

<x:parse var="a">
  <a>
   <b>
    <c>
     foo
    </c>
   </b>
   <d>
     <e>
       bar
     </e>
   </d>
  </a>
</x:parse>

<x:set var="d" select="$a//d"/>
<x:out select="$d/e"/>

<hr />

</body>
</html>

<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<html>
<head>
  <title>JSTL: XML Support -- Parse / If</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Parse / If</h3>

<x:parse var="a">
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
</x:parse>

<x:if select="$a//c">
  $a//c exists
</x:if>

<br />

<x:if select="$a/a/d">
  $a/a/d exists
</x:if>

<br />

<x:if select="$a/w/o/l">
  $a/w/o/l exists
</x:if>

<hr />

</body>
</html>

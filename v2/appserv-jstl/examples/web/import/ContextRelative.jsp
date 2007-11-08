<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: I/O Support -- Context-relative URL example</title>
</head>
<body bgcolor="#FFFFFF">
Assuming you have the "examples" webapp installed, here's a file from it...

URL:<c:out value="${_contextUrl}"/><br>
Name:<c:out value="${_contextName}"/>

<blockquote>
 <pre>
  <c:import url="${_contextUrl}" context="${_contextName}"/>
 </pre>
</blockquote>

</body>
</html>

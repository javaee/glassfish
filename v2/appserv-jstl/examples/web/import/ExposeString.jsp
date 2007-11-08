<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: I/O Support -- String exposure</title>
</head>
<body bgcolor="#FFFFFF">
<h3>String exposure</h3>

<c:import var="cnn" url="http://www.cnn.com/cnn.rss"/>

<h4>CNN's RSS XML feed:</h4>
<blockquote>
 <pre>
  <c:out value="${cnn}"/>
 </pre>
</blockquote>

</body>
</html>

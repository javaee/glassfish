<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: I/O Support -- String exposure for relative URL</title>
</head>
<body bgcolor="#FFFFFF">
<h3>String exposure for relative URL</h3>

<h4>Escaped (raw HTML)</h4>

<c:import var="cnn" url="LocalSample.jsp"/>

<c:out value="${cnn}"/>

</body>
</html>

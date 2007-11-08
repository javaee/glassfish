<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: Iterator Support -- Simple Iteration Example</title>
</head>

<body bgcolor="#FFFFFF">
<h3>Simple Iteration</h3>

<h4>Customer list</h4>

<c:forEach var="customer" items="${customers}">
  ${customer}<br>
</c:forEach>

</body>
</html>

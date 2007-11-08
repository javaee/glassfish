<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: Conditional Support -- Simple Conditional Execution Example</title>
</head>
<body bgcolor="#FFFFFF">

<h3>Simple Conditional Execution</h3>

<h4>Customers living in the USA</h4>

<c:forEach var="customer" items="${customers}">
  <c:if test="${customer.address.country == 'USA'}">
    ${customer}<br>
  </c:if>
</c:forEach>
</body>
</html>

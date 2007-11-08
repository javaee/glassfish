<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: Conditional Support -- Mutually Exclusive Conditional Execution Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Mutually Exclusive Conditional Execution</h3>

<h4>USA:blue Canada:red Others:green</h4>

<c:forEach var="customer" items="${customers}">
  <c:choose>
    <c:when test="${customer.address.country == 'USA'}">
      <font color="blue">
    </c:when>
    <c:when test="${customer.address.country == 'Canada'}">
      <font color="red">
    </c:when>
    <c:otherwise>
      <font color="green">	
    </c:otherwise>	
  </c:choose>
  ${customer}</font><br>
</c:forEach>
</body>
</html>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ex" uri="/jstl-examples-taglib" %>

<html>
<head>
  <title>JSTL: Conditional Support -- Custom Logic Tag Example</title>
</head>
<body bgcolor="#FFFFFF">

<h3>Custom Logic Tag</h3>

<h4>Customers living in the USA</h4>

<h4>Simple Conditional Execution</h4>
<c:forEach var="customer" items="${customers}">
  <ex:usCustomer customer="${customer}">
    <c:out value="${customer}"/><br>
  </ex:usCustomer>
</c:forEach>

<h4>Mutually Exclusive Conditional Execution</h4>

<c:forEach var="customer" items="${customers}">
  <ex:usCustomer customer="${customer}" var="isUsCustomer"/>
  <c:choose>
    <c:when test="${isUsCustomer}">
      <font color="blue">
    </c:when>
    <c:otherwise>
      <font color="green">	
    </c:otherwise>	
  </c:choose>
  <c:out value="${customer}"/></font><br>
</c:forEach>
</body>
</html>

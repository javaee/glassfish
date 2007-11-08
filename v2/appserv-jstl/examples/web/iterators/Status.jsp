<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: Iterator Support -- Iteration Status Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Iteration Status</h3>

<h4>Using status information: current, index, count, first, last</h4>
<table border="1">
  <tr>
    <th>index</th>
    <th>count</th>
    <th>last name</th>
    <th>first name</th>
    <th>first?</th>
    <th>last?</th>
  </tr>
  <c:forEach var="customer" items="${customers}" varStatus="status">
    <tr>
      <td><c:out value="${status.index}"/></td>
      <td><c:out value="${status.count}"/></td>
      <td><c:out value="${status.current.lastName}"/></td>
      <td><c:out value="${status.current.firstName}"/></td>
      <td><c:out value="${status.first}"/></td>
      <td><c:out value="${status.last}"/></td>
    </tr>
    <c:if test="${status.last}">
      <c:set var="count" value="${status.count}"/>
    </c:if>  
  </c:forEach>
</table>
<p>There are <c:out value="${count}"/> customers in the list.

<p>

<h4>Iteration using range attributes</h4>
<c:forEach var="i" begin="100" end="200" step="5" varStatus="status">
  <c:if test="${status.first}">
    begin:<c:out value="${status.begin}">begin</c:out> &nbsp; &nbsp; 
      end:<c:out value="${status.end}">end</c:out> &nbsp; &nbsp; 
     step:<c:out value="${status.step}">step</c:out><br>
    sequence: 
  </c:if>  
  <c:out value="${i}"/> 
  <c:if test="${status.last}">
    <br>There are <c:out value="${status.count}"/> numbers in the list.
  </c:if>  
</c:forEach>
<p>
</body>
</html>

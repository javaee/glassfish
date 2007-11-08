<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: Expression Language Support -- Set Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>&lt;c:set&gt;</h3>

<h4>Setting application scope attribute "customerTable"</h4>

<c:set var="customerTable" scope="application">
<table border="1">
    <c:forEach var="customer" items="${customers}">
    <tr>
	  <td>${customer.lastName}</td>
	  <td><c:out value="${customer.address}" default="no address specified"/></td>
	  <td>
	    <c:out value="${customer.address}">
		  <font color="red">no address specified</font>
		</c:out>
      </td>
	</tr>
  </c:forEach>
</table>
</c:set>

<p> 
Using customerTable in another JSP page 
<a href="../ShowSource.jsp?filename=/elsupport/Set2.jsp"><img src="../images/code.gif" width="24" height="24" border="0"></a> 
<a href="../elsupport/Set2.jsp"><img src="../images/execute.gif" width="24" height="24" border="0"></a> 
</body>
</html>

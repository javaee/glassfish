<%@ taglib prefix="scriptfree" uri="http://jakarta.apache.org/taglibs/standard/scriptfree" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title>JSTL: Tag Library Validator -- ScriptFree OK</title>
</head>
<body bgcolor="#FFFFFF">

<h3>This JSP page is free of scripting elements</h3>

<table border="1">
  <c:forEach var="customer" items="${customers}">
    <tr>
	  <td><c:out value="${customer.lastName}"/></td>
	  <td><c:out value="${customer.phoneHome}" default="no home phone specified"/></td>
	  <td>
	    <c:out value="${customer.phoneCell}" escapeXml="false">
		  <font color="red">no cell phone specified</font>
		</c:out>
      </td>
    </tr>
  </c:forEach>
</table>
</body>
</html>

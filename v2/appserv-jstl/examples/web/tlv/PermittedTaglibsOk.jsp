<%@ taglib prefix="permittedTaglibs" uri="http://jakarta.apache.org/taglibs/standard/permittedTaglibs" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: Tag Library Validator -- ScriptFree OK</title>
</head>
<body bgcolor="#FFFFFF">

<h3>This JSP page only uses permitted taglibs (which in this case are the JSTL taglibs)</h3>

<table>
<c:forEach var="customer" items="${customers}" varStatus="status">
  <tr>
    <jsp:useBean type="javax.servlet.jsp.jstl.core.LoopTagStatus" id="status"/>
    <c:choose>
      <c:when test="${status.count % 2 == 1}">
	    <td bgcolor="#FFFF66">
	  </c:when>
	  <c:otherwise>
	    <td bgcolor="#99FFCC">
	  </c:otherwise>
    </c:choose>
    <c:out value="${customer}"/></td>
  </tr>
</c:forEach> 
</table>

</body>
</html>

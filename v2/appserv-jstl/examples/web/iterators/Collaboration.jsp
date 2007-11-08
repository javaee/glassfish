<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ex" uri="/jstl-examples-taglib" %>

<html>
<head>
  <title>JSTL: Iterator Support 2-- Collaboration Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Collaboration</h3>

<h4>Without custom tags</h4>

<table>
<c:forEach var="customer" items="${customers}" varStatus="status">
  <tr>
    <jsp:useBean type="javax.servlet.jsp.jstl.core.LoopTagStatus" id="status"/>
    <c:choose>
      <c:when test="<%= status.getCount() % 2 == 1 %>">
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

<h4>Using custom tags &lt;even&gt; and &lt;odd&gt;</h4>

<table>
<c:forEach var="customer" items="${customers}">
  <tr>
    <ex:odd><td bgcolor="#FFFF66"></ex:odd>
    <ex:even><td bgcolor="#99FFCC"></ex:even>
    <c:out value="${customer}"/></td>
  </tr>
</c:forEach> 
</table>
</body>
</html>

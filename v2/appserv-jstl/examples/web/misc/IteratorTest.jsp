<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: Miscellaneous -- Various Iterator Tests Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Various tests for Iterator tags</h3>

<h4>Iteration with only end specified (no items): end="10"</h4>
(illegal)
<%--
<c:forEach var="i" end="10">
  <c:out value="${i}"/> &#149; 
</c:forEach>
--%>

<h4>Iteration with only begin specified (no items): begin="10"</h4>
(illegal)
<%--
<c:forEach var="i" begin="10">
  ${i} &#149; 
</c:forEach>
--%>

<h4>Iteration with only begin specified (with items): begin="2"</h4>

<c:forEach var="i" items="${customers}" begin="2" varStatus="status">
  index: ${status.index} &#149; 
  count: ${status.count} &#149; 
  item: ${i}<br>
</c:forEach>

<h4>Iteration with only end specified (with items): end="1"</h4>

<c:forEach var="i" items="${customers}" end="1" varStatus="status">
  index: ${status.index} &#149; 
  count: ${status.count} &#149; 
  item:  ${i}><br>
</c:forEach>

<h4>Iteration with begin > end</h4>
<c:catch var="ex">
  <c:forEach var="i" items="${customers}" begin="3" end="1" varStatus="status">
    index: ${status.index} &#149; 
    count: ${status.count} &#149; 
    item:  ${i}<br>
  </c:forEach>
  <c:forTokens var="i" items="one,two,three,four,five" delims="," begin="3" end="1" varStatus="status">
    index: ${status.index} &#149; 
    count: ${status.count} &#149; 
    item:  ${i}<br>
  </c:forTokens>
</c:catch>
<c:if test="${ex != null}">
  Exception: ${ex}
</c:if>

</body>
</html>

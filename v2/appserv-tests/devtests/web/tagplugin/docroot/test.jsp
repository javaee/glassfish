<html>
<%-- A test for tag plugins for <c:if>, <c:forEach>, and <c:choose> --%>

Testing tag plugins for &lt;c:if>, &lt;c:forEach>, and &lt;c:choose>
<br/>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="count" value="1"/>

<c:forEach var="index" begin="1" end="3">
  <c:choose>
    <c:when test="${index==3}">
      <c:set var="count" value="${count+30}"/>
    </c:when>
    <c:when test="${index==1}">
      <c:set var="count" value="${count+10}"/>
    </c:when>
    <c:otherwise>
      <c:set var="count" value="${count+100}"/>
    </c:otherwise>
  </c:choose>
</c:forEach>

Count is ${count}, should be 141
<br/>
<c:if test="${count==141}">
  Tag Plugin Test: PASS
</c:if>
</html>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%! enum Suit {club, diamond, heart, spade} %>
<%
   Suit h = Suit.heart;
   Suit h2 = Suit.heart;
   Suit d = Suit.diamond;

   pageContext.setAttribute("H", h);
   pageContext.setAttribute("H2", h2);
   pageContext.setAttribute("D", d);
%>

<c:if test="${H == H2}">
PASS
</c:if>
<c:if test="${H == 'heart'}">
PASS
</c:if>
${D}
<c:choose>
  <c:when test="${D == 'club'}"> club </c:when>
  <c:when test="${D == 'diamond'}"> diamond </c:when>
  <c:otherwise>FAIL</c:otherwise>
</c:choose>

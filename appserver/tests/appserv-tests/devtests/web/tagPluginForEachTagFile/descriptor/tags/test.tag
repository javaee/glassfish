<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="end" required="true" %>

<c:forEach var="i" begin="1" end="${end}">
  Hello World
</c:forEach>

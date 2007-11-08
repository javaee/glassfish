<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 
<%!
   public String throwEx() throws Exception {
      throw new java.lang.Exception("exception thrown by throwEx");  
   }
%>
 
<h3>begin</h3>

<c:catch var="ex2">
  <c:out value="${null}">
    <% throwEx(); %>
  </c:out>
</c:catch>
<c:out value="${ex2}" default="exception is null - 2" escapeXml="false"/>

<h3>end</h3>

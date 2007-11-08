<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ex" uri="/jstl-examples-taglib" %>


<c:set var="first"><c:out value="${param['first']}" default="1"/></c:set>
<c:set var="pageSize"><c:out value="${param['pageSize']}" default="30"/></c:set>
<c:set var="last" value="${first+pageSize-1}"/>

<c:if test="${first != 1}">
   <c:url var="prevURL" value="Extensability.jsp">
      <c:param name="first" value="${first - pageSize}"/>
   </c:url>
</c:if>

<c:url var="nextURL" value="Extensability.jsp">
      <c:param name="first" value="${last+1}"/>
</c:url>

<table><tr><th>#</th></th><th>Code</th><th colspan="2" align="left">Name</th></tr>
<ex:locales var="locale" varStatus="status" varTotal="total" begin="${first}" end="${last}">
   <tr>
      <td><c:out value="${status.index}"/></td>
      <td><c:out value="${locale}"/></td>
      <td><c:out value="${locale.displayName}"/></td>
      <td><ex:defaultLocale><b>(default locale)</b></ex:defaultLocale></td>
   </tr>
</ex:locales>
</table>

<c:if test="${last > total}">
   <c:set var="last" value="${total}"/>
   <c:remove var="nextURL"/>
</c:if>

<br>
Showing locales <c:out value="${first}"/> to <c:out value="${last}"/> of <c:out value="${total}"/><br>
<br>
<c:if test="${not empty prevURL}">
<a href="<c:out value="${prevURL}"/>">previous</a>&nbsp;&nbsp;&nbsp;
</c:if>
<c:if test="${not empty nextURL}">
<a href="<c:out value="${nextURL}"/>">next</a>&nbsp;&nbsp;&nbsp;
</c:if>
<br>


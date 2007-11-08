<%@ taglib prefix="permittedTaglibs" uri="http://jakarta.apache.org/taglibs/standard/permittedTaglibs" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ex" uri="/jstl-examples-taglib" %>

<html>
<head>
  <title>JSTL: Tag Library Validator -- PermittedTaglibs Error</title>
</head>
<body bgcolor="#FFFFFF">

<h3>This JSP page uses taglibs that are not permitted. A translation error will occur.</h3>

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

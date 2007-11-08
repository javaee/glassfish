<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
  <title>JSTL: Formatting/I18N Support -- Request Encoding Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>German Umlaut characters decoded incorrectly:</h3>

<ul>
 <li>a umlaut: <c:out value="${param.a_umlaut}"/>
 <li>o umlaut: <c:out value="${param.o_umlaut}"/>
 <li>u umlaut: <c:out value="${param.u_umlaut}"/>
</ul>

</body>
</html>

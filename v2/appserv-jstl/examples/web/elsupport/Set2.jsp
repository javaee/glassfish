<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title>JSTL: Expression Language Support -- Set 2 Example</title>
</head>
<body bgcolor="#FFFFFF">

<h3>&lt;c:set&gt;</h3>

<h4>Using "customerTable" application scope attribute defined in Set.jsp a first time</h4>
<c:out value="${customerTable}" escapeXml="false"/>

<h4>Using "customerTable" application scope attribute defined in Set.jsp a second time</h4>
<c:out value="${customerTable}" escapeXml="false" />
</body>
</html>

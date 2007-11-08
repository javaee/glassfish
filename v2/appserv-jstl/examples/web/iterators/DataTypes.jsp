<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: Iterator Support -- Data Types Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Data Types</h3>

<h4>Array of primitives (int)</h4>

<c:forEach var="i" items="${intArray}">
  <c:out value="${i}"/> &#149;
</c:forEach>

<h4>Array of objects (String)</h4>

<c:forEach var="string" items="${stringArray}">
  <c:out value="${string}"/><br>
</c:forEach>

<h4>Enumeration (warning: this only works until enumeration is exhausted!)</h4>

<c:forEach var="item" items="${enumeration}" begin="2" end="10" step="2">
  <c:out value="${item}"/><br>
</c:forEach>

<h4>Properties (Map)</h4>

<c:forEach var="prop" items="${numberMap}" begin="1" end="5">
  <c:out value="${prop.key}"/> = <c:out value="${prop.value}"/><br>
</c:forEach>

<h4>String (Comma Separated Values)</h4>

<c:forEach var="token" items="bleu,blanc,rouge">
  <c:out value="${token}"/><br>
</c:forEach>
</body>
</html>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: Iterator Support -- forTokens Example</title>
</head>
<body bgcolor="#FFFFFF">

<h3>&lt;forTokens&gt;</h3>

<h4>String with '|' delimiter</h4>

<c:forTokens var="token" items="bleu,blanc,rouge|vert,jaune|blanc,rouge"
              delims="|">
  <c:out value="${token}"/> &#149;
</c:forTokens>

<h4>String with '|' and ',' delimiters</h4>

<c:forTokens var="token" items="bleu,blanc,rouge|vert,jaune|blanc,rouge"
              delims="|,">
  <c:out value="${token}"/> &#149;
</c:forTokens>
</body>
</html>

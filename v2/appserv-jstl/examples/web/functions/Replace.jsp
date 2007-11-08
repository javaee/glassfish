<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
  <title>JSTL Functions &#149; Replace</title>
</head>
<body bgcolor="#FFFFFF">

<h2>Replace</h2>

<c:set var="s1" value="There is a castle on a cloud"/>
<c:set var="s2" value="one - two - three - one - two - three - one - two - three"/>
<h4>fn:replace</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>Substring Before</th>
    <th>Substring After</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>e</td>
    <td>*</td>
    <td>${fn:replace(s1, "e", "*")}</td>
  </tr>
  <tr>
    <td>${s2}</td>
    <td>-</td>
    <td>&#149;</td>
    <td>${fn:replace(s2, "-", "&#149;")}</td>
  </tr>
  <tr>
    <td>${s2}</td>
    <td>two</td>
    <td>empty</td>
    <td>${fn:replace(s2, "two", "")}</td>
  </tr>
  <tr>
    <td>${s2}</td>
    <td>empty string</td>
    <td>one</td>
    <td>${fn:replace(s2, "", "one")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>one</td>
    <td>two</td>
    <td>&nbsp;${fn:replace(undefined, "one", "two")}</td>
  </tr>
</table>

</body>
</html>

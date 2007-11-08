<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
  <title>JSTL Functions &#149; Length</title>
</head>
<body bgcolor="#FFFFFF">

<c:set var="s1" value="There is a castle on a cloud"/>

<h4>fn:length</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>${fn:length(s1)}</td>
  </tr>
  <tr>
    <td>${customers}</td>
    <td>${fn:length(customers)}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>${fn:length(undefined)}</td>
  </tr>
  <tr>
    <td>empty string</td>
    <td>${fn:length("")}</td>
  </tr>
</table>

</body>
</html>

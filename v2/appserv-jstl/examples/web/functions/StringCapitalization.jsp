<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
  <title>JSTL Functions &#149; String Capitalization</title>
</head>
<body bgcolor="#FFFFFF">

<h2>String Capitalization</h2>

<c:set var="s1" value="There is a CASTLE on a CLOUD"/>

<h4>fn:toLowerCase</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>${fn:toLowerCase(s1)}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>&nbsp;${fn:toLowerCase(undefined)}</td>
  </tr>
  <tr>
    <td>empty</td>
    <td>&nbsp;${fn:toLowerCase("")}</td>
  </tr>
</table>

<h4>fn:toUpperCase</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>${fn:toUpperCase(s1)}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>&nbsp;${fn:toUpperCase(undefined)}</td>
  </tr>
  <tr>
    <td>empty</td>
    <td>&nbsp;${fn:toUpperCase("")}</td>
  </tr>
</table>

</body>
</html>

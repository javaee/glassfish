<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
  <title>JSTL Functions &#149; Escape Xml</title>
</head>
<body bgcolor="#FFFFFF">

<h2>Escape XML</h2>

<c:set var="s1" value="There is a castle on a cloud"/>

<h4>fn:escapeXml</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>${fn:escapeXml(s1)}</td>
  </tr>
  <tr>
    <td><foo>body of foo</foo></td>
    <td>${fn:escapeXml("<foo>body of foo</foo>")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>&nbsp;${fn:escapeXml(undefined)}</td>
  </tr>
  <tr>
    <td>empty string</td>
    <td>&nbsp;${fn:escapeXml("")}</td>
  </tr>
</table>

</body>
</html>

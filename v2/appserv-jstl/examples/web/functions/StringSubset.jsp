<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
  <title>JSTL Functions &#149; String Subset</title>
</head>
<body bgcolor="#FFFFFF">

<h2>String Subset</h2>

<c:set var="zip" value="75843-5643"/>
<c:set var="s1" value="There is a castle on a cloud"/>

<h4>fn:substring</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>beginIndex</th>
    <th>endIndex</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${zip}</td>
    <td>6</td>
    <td>-1</td>
    <td>P.O. Box: ${fn:substring(zip, 6, -1)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>11</td>
    <td>17</td>
    <td>${fn:substring(s1, 11, 17)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>12</td>
    <td>5</td>
    <td>&nbsp;${fn:substring(s1, 12, 5)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>23</td>
    <td>-1</td>
    <td>${fn:substring(s1, 23, -1)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>23</td>
    <td>999</td>
    <td>${fn:substring(s1, 23, 999)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>-1</td>
    <td>-1</td>
    <td>${fn:substring(s1, -1, -1)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>99</td>
    <td>12</td>
    <td>&nbsp;${fn:substring(s1, 99, 12)}</td>
  </tr>
  <tr>
    <td>empty string</td>
    <td>2</td>
    <td>6</td>
    <td>&nbsp;${fn:substring("", 2, 6)}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>2</td>
    <td>6</td>
    <td>&nbsp;${fn:substring(undefined, 2, 6)}</td>
  </tr>
</table>

<h4>fn:substringAfter</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>substring</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${zip}</td>
    <td>-</td>
    <td>P.O. Box: ${fn:substringAfter(zip, "-")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>There</td>
    <td>${fn:substringAfter(s1, "There")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>on a</td>
    <td>${fn:substringAfter(s1, "on a")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>not found</td>
    <td>&nbsp;${fn:substringAfter(s1, "not found")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>null</td>
    <td>${fn:substringAfter(s1, undefined)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>empty string</td>
    <td>${fn:substringAfter(s1, "")}</td>
  </tr>
  <tr>
    <td>empty string</td>
    <td>castle</td>
    <td>&nbsp;${fn:substringAfter("", "castle")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>castle</td>
    <td>&nbsp;${fn:substringAfter(undefined, "castle")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>empty string</td>
    <td>&nbsp;${fn:substringAfter(undefined, "")}</td>
  </tr>
</table>

<h4>fn:substringBefore</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>substring</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${zip}</td>
    <td>-</td>
    <td>Zip without P.O. Box: ${fn:substringBefore(zip, "-")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>on a</td>
    <td>${fn:substringBefore(s1, "on a")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>castle</td>
    <td>${fn:substringBefore(s1, "castle")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>null</td>
    <td>&nbsp;${fn:substringBefore(s1, undefined)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>empty string</td>
    <td>&nbsp;${fn:substringBefore(s1, "")}</td>
  </tr>
  <tr>
    <td>empty string</td>
    <td>castle</td>
    <td>&nbsp;${fn:substringBefore("", "castle")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>castle</td>
    <td>&nbsp;${fn:substringBefore(undefined, "castle")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>empty string</td>
    <td>&nbsp;${fn:substringBefore(undefined, "")}</td>
  </tr>
</table>

</body>
</html>

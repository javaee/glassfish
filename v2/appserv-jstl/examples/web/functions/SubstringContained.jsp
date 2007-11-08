<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
  <title>JSTL Functions &#149; Substring Contained in String</title>
</head>
<body bgcolor="#FFFFFF">

<h2>Substring Contained in String</h2>

<c:set var="s1" value="There is a castle on a cloud"/>

<h4>fn:contains</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>Substring</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>castle</td>
    <td>${fn:contains(s1, "castle")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>CASTLE</td>
    <td>${fn:contains(s1, "CASTLE")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>null</td>
    <td>${fn:contains(s1, undefined)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>empty string</td>
    <td>${fn:contains(s1, "")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>castle</td>
    <td>${fn:contains(undefined, "castle")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>empty string</td>
    <td>${fn:contains(undefined, "")}</td>
  </tr>
</table>

<h4>fn:containsIgnoreCase</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>Substring</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>castle</td>
    <td>${fn:containsIgnoreCase(s1, "castle")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>CASTLE</td>
    <td>${fn:containsIgnoreCase(s1, "CASTLE")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>CaStLe</td>
    <td>${fn:containsIgnoreCase(s1, "CaStLe")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>null</td>
    <td>${fn:containsIgnoreCase(s1, undefined)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>empty string</td>
    <td>${fn:containsIgnoreCase(s1, "")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>castle</td>
    <td>${fn:containsIgnoreCase(undefined, "castle")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>empty string</td>
    <td>${fn:containsIgnoreCase(undefined, "")}</td>
  </tr>
</table>

<h4>fn:startsWith</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>Substring</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>castle</td>
    <td>${fn:startsWith(s1, "castle")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>There is</td>
    <td>${fn:startsWith(s1, "There is")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>null</td>
    <td>${fn:startsWith(s1, undefined)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>empty string</td>
    <td>${fn:startsWith(s1, "")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>castle</td>
    <td>${fn:startsWith(undefined, "castle")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>empty string</td>
    <td>${fn:startsWith(undefined, "")}</td>
  </tr>
</table>

<h4>fn:endsWith</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>Substring</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>castle</td>
    <td>${fn:endsWith(s1, "castle")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>cloud</td>
    <td>${fn:endsWith(s1, "cloud")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>null</td>
    <td>${fn:endsWith(s1, undefined)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>empty string</td>
    <td>${fn:endsWith(s1, "")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>castle</td>
    <td>${fn:endsWith(undefined, "castle")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>empty string</td>
    <td>${fn:endsWith(undefined, "")}</td>
  </tr>
</table>

<h4>fn:indexOf</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>Substring</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>castle</td>
    <td>${fn:indexOf(s1, "castle")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>cloud</td>
    <td>${fn:indexOf(s1, "cloud")}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>null</td>
    <td>${fn:indexOf(s1, undefined)}</td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>empty string</td>
    <td>${fn:indexOf(s1, "")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>castle</td>
    <td>${fn:indexOf(undefined, "castle")}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>empty string</td>
    <td>${fn:indexOf(undefined, "")}</td>
  </tr>
  <c:set var="text" value="Products List (2003/05/01)"/>
  <tr>
    <td colspan="3">Display text in between brackets</td>
  </tr>
  <tr>
    <td>${text}</td>
    <td>'(' and ')'</td>
    <td>${fn:substring(text, fn:indexOf(text, '(')+1, fn:indexOf(text, ')'))}</td>
  </tr>
</table>

<p>


</body>
</html>

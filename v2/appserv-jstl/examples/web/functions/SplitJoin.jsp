<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ex" uri="/jstl-examples-taglib" %>

<html>
<head>
  <title>JSTL Functions &#149; Split/Join</title>
</head>
<body bgcolor="#FFFFFF">

<h2>Split/Join</h2>

<c:set var="s1" value="There is a castle on a cloud"/>
<c:set var="s3" value="one|two|three|four"/>
<c:set var="s5" value="one|two+three*four"/>

<h4>fn:split</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>Delimiters</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>empty string</td>
    <td>${ex:display(fn:split(s1, ""))}</td>
  </tr>
  <tr>
    <td>${s3}</td>
    <td>|</td>
    <td>${ex:display(fn:split(s3, "|"))}</td>
  </tr>
  <tr>
    <td>${s3}</td>
    <td>+</td>
    <td>${ex:display(fn:split(s3, "+"))}</td>
  </tr>
  <tr>
    <td>${s5}</td>
    <td>|+</td>
    <td>${ex:display(fn:split(s5, "|+"))}</td>
  </tr>
  <tr>
    <td>empty string</td>
    <td>empty string</td>
    <td>&nbsp;${ex:display(fn:split("", ""))}</td>
  </tr>
</table>

<c:set var="a1" value='${fn:split(s1, " ")}'/>
<h4>fn:join</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input Array</th>
    <th>Separator</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>${ex:display(a1)}</td>
    <td> + </td>
    <td>${fn:join(a1, " + ")}</td>
  </tr>
  <tr>
    <td>${ex:display(a1)}</td>
    <td>&lt;sep></td>
    <td>${fn:join(a1, " &lt;sep> ")}</td>
  </tr>
  <tr>
    <td>${ex:display(a1)}</td>
    <td>empty string</td>
    <td>${fn:join(a1, "")}</td>
  </tr>
  <tr>
    <td>${ex:display(a1)}</td>
    <td>null</td>
    <td>${fn:join(a1, null)}</td>
  </tr>
  <tr>
    <td>null</td>
    <td>empty string</td>
    <td>&nbsp;${fn:join(null, "")}</td>
  </tr>
</table>

</body>
</html>

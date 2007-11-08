<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
  <title>JSTL Functions &#149; Trim</title>
</head>
<body bgcolor="#FFFFFF">

<h2>Trim</h2>

<c:set var="s1" value="There is a castle on a cloud"/>
<c:set var="custId" value=" 123 "/>

<h4>fn:trim</h4>
<table cellpadding="5" border="1">
  <tr>
    <th align="left">Input String</th>
    <th>Result</th>
  </tr>
  <tr>
    <td><pre>${custId} (whithout trim)</pre></td>
    <td><c:url value="http://acme.com/cust"><c:param name="custId" value="${custId}"/></c:url></td>
  </tr>
  <tr>
    <td><pre>${custId} (whith trim)</pre></td>
    <td><c:url value="http://acme.com/cust"><c:param name="custId" value="${fn:trim(custId)}"/></c:url></td>
  </tr>
  <tr>
    <td>${s1}</td>
    <td>${fn:trim(s1)}</td>
  </tr>
  <tr>
    <td><pre>    3 spaces before and after   </pre></td>
    <td><pre>${fn:trim("    3 spaces before and after   ")}</pre></td>
  </tr>
  <tr>
    <td>null</td>
    <td>&nbsp;${fn:trim(undefined)}</td>
  </tr>
  <tr>
    <td>empty string</td>
    <td>&nbsp;${fn:trim("")}</td>
  </tr>
</table>

</body>
</html>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>JSTL: I/O Support -- URL Encoding Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>URL Encoding</h3>

<h4>&lt;c:url&gt;</h4>

Disable cookies in your browser to see URL rewriting.

<table border="1" bgcolor="#dddddd">
 <tr>
  <td>"base", param=ABC</td>
  <td><c:url value="base"><c:param name="param" value="ABC"/></c:url></td>
 </tr>
 <tr>
  <td>"base", param=123</td>
  <td><c:url value="base"><c:param name="param" value="123"/></c:url></td>
 </tr>
 <tr>
  <td>"base", param=&</td>
  <td><c:url value="base"><c:param name="param" value="&"/></c:url></td>
 </tr>
 <tr>
  <td>"base", param="JSTL is fun"</td>
  <td><c:url value="base"><c:param name="param" value="JSTL is fun"/></c:url></td>
 </tr>
 <tr>
  <td>"base", param="ü@foo-bar"</td>
  <td><c:url value="base"><c:param name="param" value="ü@foo-bar"/></c:url></td>
 </tr>
 <tr>
  <td>"base", météo="légère pluie @ Saint-Denis-de-la-Réunion"</td>
  <td><c:url value="base"><c:param name="météo" value="légère pluie @ Saint-Denis-de-la-Réunion"/></c:url></td>
 </tr>
</table>

<p>
Compose the url, then use it in an HTML anchor tag: 
<c:url var="url" value="Encode.jsp">
  <c:param name="arg1" value="value of arg1"/>
  <c:param name="arg2" value="value of arg2"/>
</c:url>
<a href='<c:out value="${url}"/>'>Link back to this page (<c:out value="${url}"/>)</a>
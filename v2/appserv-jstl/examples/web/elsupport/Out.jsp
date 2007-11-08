<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title>JSTL: Expression Language Support -- Expr Example</title>
</head>
<body bgcolor="#FFFFFF">

<h3>&lt;c:out&gt;</h3>

<table border="1">
  <c:forEach var="customer" items="${customers}">
    <tr>
      <td><c:out value="${customer.lastName}"/></td>
      <td><c:out value="${customer.phoneHome}" default="no home phone specified"/></td>
      <td>
        <c:out value="${customer.phoneCell}" escapeXml="false">
          <font color="red">no cell phone specified</font>
        </c:out>
      </td>
    </tr>
  </c:forEach>
</table>

<h4>&lt;c:out&gt; with Reader object</h4>
<%
java.io.Reader reader1 = new java.io.StringReader("<foo>Text for a Reader!</foo>");
pageContext.setAttribute("myReader1", reader1);
java.io.Reader reader2 = new java.io.StringReader("<foo>Text for a Reader!</foo>");
pageContext.setAttribute("myReader2", reader2);
%>
Reader1 (escapeXml=true) : <c:out value="${myReader1}"/><br>
Reader2 (escapeXml=false): <c:out value="${myReader2}" escapeXml="false"/><br>
</body>
</html>

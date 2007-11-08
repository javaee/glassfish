<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="test" tagdir="/WEB-INF/tags" %>

<html>
  <h1>Included text goes here:</h1>
  <table border="1">
    <tr>
      <td><%@ include file="included.jsp" %></td>
    </tr>
  </table>
</html>

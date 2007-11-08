<%@ page import="java.util.*" %>
<%@ page import="java.net.*" %>

<html>
  <body>
    <title>Redirecting ... </title>

    <%
      String url = "/web-getRequestURI/jsp/first.jsp";

      String value = URLEncoder.encode ("e;fgh@y");
      url = url + ";iPlanetDirectoryPro=" + value;

      response.sendRedirect (url);

    %>

  </body>
</html>


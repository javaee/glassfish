<%@ page import="java.util.*" %>
<%@ page import="java.net.*" %>

<html>
  <body>
    <title> First Page </title>

    <%
      String uri = request.getRequestURI();

      //
      // cant print it here bcos of the forward - do it in second.jsp
      //
      request.setAttribute ("uri_in_first_jsp", uri);

      String url = "/jsp/second.jsp";
      String value = URLEncoder.encode ("e;fgh@y"); // same as in main.jsp

      url = url + ";iPlanetDirectoryPro=" + value;
    %>

<jsp:forward page="<%=url%>" />

  </body>
</html>


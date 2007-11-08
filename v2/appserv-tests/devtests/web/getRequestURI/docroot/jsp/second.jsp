<%@ page import="java.util.*" %>
<%@ page import="java.net.*" %>

<html>
  <body>
    <title>Second Page </title>

    <%
      String thisURI = request.getRequestURI();

      //
      // retrieve the one set in first.
      //
      String firstURI = (String)request.getAttribute ("uri_in_first_jsp");

      request.setAttribute("uri_in_first_jsp", firstURI);
      request.setAttribute("uri_in_second_jsp", thisURI);

      out.println ("<br/> request.getRequestURI() of first.jsp: " + firstURI);
      out.println ("<br/> request.getRequestURI() in second.jsp: " + thisURI);
    %>


  </body>
</html>


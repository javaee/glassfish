<%
  String message = (String)request.getAttribute("message");
%>

<html>
  <head><title>SimpleBank Application</title></head>
  <body>
    <center><%=message%></center><br>
    <a href="/installed_libraries_embedded/index.html">Return to Main Page</a>
  </body>
</html>

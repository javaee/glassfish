<%@page import="javax.naming.*"%>
<%@taglib prefix="my" uri="http://java.sun.com/test-taglib"%>
<html>
  <head>
    <title>Hello</title>
  </head>
  <body>
    Message: <%=session.getAttribute("deployment.ejb30.web.jsp") %>
    Message2: <my:custom/>
  </body>
</html>

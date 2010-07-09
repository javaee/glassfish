<%@page language="java"%>
<%@page contentType="text/html" import="java.util.Date"%>
<%@page import="javax.naming.*"%>
<%@page import="javax.rmi.*" %>
<%@page import="java.rmi.*" %>
<%@page import="java.security.cert.X509Certificate" %>

<html>
<head><title>JSP Page Access Profile</title></head>
<body>
This is <%= request.getRemoteUser() %> from index.jsp
</body>
</html>

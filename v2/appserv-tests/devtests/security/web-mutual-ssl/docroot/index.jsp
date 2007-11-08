<%@page language="java"%>
<%@page contentType="text/html" import="java.util.Date"%>
<%@page import="javax.naming.*"%>
<%@page import="javax.rmi.*" %>
<%@page import="java.rmi.*" %>
<%@page import="java.security.cert.X509Certificate" %>

<html>
<head><title>JSP Page Access Profile</title></head>
<body>
<% 
    //out.println("The web user principal = "+request.getUserPrincipal() );
    out.println("this is index.jsp");
/*
    X509Certificate[] certs = (X509Certificate[])request.getAttribute(
            "javax.servlet.request.X509Certificate");
    if (certs != null) {
       for (X509Certificate cert : certs) {
           out.println(cert.toString());
       }
    } else {
       out.println("certs is null");
    }
*/
%>
</body>
</html>

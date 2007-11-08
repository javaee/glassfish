<%@page language="java"%>
<%@page contentType="text/html" import="java.util.Date"%>
<%@page import="javax.naming.*"%>
<%@page import="javax.rmi.*" %>
<%@page import="java.rmi.*" %>
<%@page import="profile.*" %>

<html>
<head><title>JSP Page Access Profile</title></head>
<body>
<% 
    out.println("The web user principal = "+request.getUserPrincipal() );
    out.println();
%>
<H3> Calling the ProfileInfoBean </H3>
<%
    try{
        InitialContext ic = new InitialContext();
        java.lang.Object obj = ic.lookup("jsp2sful");
	out.println("Looked up home!!");
	ProfileInfoHome home = (ProfileInfoHome)PortableRemoteObject.narrow(
					   obj, ProfileInfoHome.class);
	out.println("Narrowed home!!");
        ProfileInfoRemote hr = home.create("a name");
        out.println("Got the EJB!!");
        out.println("<li>User profile: ");
        try {
            out.println(hr.getCallerInfo());
        } catch(AccessException ex) {
            out.println("CANNOT ACCESS getCallerInfo()");
        }
        out.println("<li>Secret info: ");
        try {
            out.println(hr.getSecretInfo());
        } catch(AccessException ex) {
            out.println("CANNOT ACCESS getSecretInfo()");
        }
    } catch(java.rmi.RemoteException e){
        e.printStackTrace();
        out.println(e.toString());
    }
%>
</body>
</html>

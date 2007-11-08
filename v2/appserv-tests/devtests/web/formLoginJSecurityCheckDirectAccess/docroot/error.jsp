<%@ page language="java" %>
<html>
<head><title> A simple Error Page</title></head>
<body>
<h2>A simple Error Page</h2>
<hr>
You could not be authenticated with the information provided. <BR>
Please check your Username and Password.
<hr>
<% 
if ( request.getUserPrincipal() == null )
    out.println("The user principal is: " + request.getUserPrincipal() + "<BR>");
else
    out.println("The user principal is: " + request.getUserPrincipal().getName() + "<BR>");
%>
<br>
</body>
</html>


 

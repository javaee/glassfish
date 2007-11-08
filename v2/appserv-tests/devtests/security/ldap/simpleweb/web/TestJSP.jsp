<%@ page language="java" %>
<%@ page import="javax.naming.*" %>
<%@ page import="javax.rmi.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="javax.sql.*" %>

<%!
	// Security role references.
	private static final String emp_secrole_ref   = "Employee";
	private static final String admin_secrole_ref = "ADMIN";
	private static final String mgr_secrole_ref   = "Manager";
        private static final String user ="munta";
%>

<html>
<head><title>Web Auth Test</title></head>
<body>
<br>Basic Authentication tests from JSP: Test1,Test2,Test3 
<br>Authorization test from JSP: Test4,Test5-> HttpServletRequest.isUserInRole() authorization from JSP.
<hr>
<%
/*
        Principal p = request.getUserPrincipal();     
        String username = p.getName();
        out.println("<br>Test1");
        if (p==null){
                        out.println("<br>Test1.Basic Auth from JSP test Failed!");
                        out.println("<br>Test2.HttpServletRequest.isUserInRole() test Failed!");
                        out.println("<br>INFO:Principal from basic auth JSP is null!");
        }
*/      
                //Check the auth type - request.getAuthType()
                out.println("<br><br>Test1. Postive check for the correct authentication type");
                String authtype=request.getAuthType();
                if ("BASIC".equalsIgnoreCase(authtype) ){
                        out.println("<br>request.getAuthType() test Passed.");
                }else{
                        out.println("<br>request.getAuthType() test Failed!");
                }
                out.println("<br>Info:request.getAuthType() is= "+authtype);

                String username = request.getUserPrincipal().getName();
                out.println("<br><br>Test2. Positive check for the correct principal name");
                if (user.equals(username)){
                        out.println("<br>request.getUserPrincipal() test Passed.");
                }else{
                        out.println("<br>request.getUserPrincipal() test Failed!");
                }
                out.println("<br>Info:request.getUserPrincipal() is= "+username);

                //Check the remote user request.getRemoteUser()- get null if not authenticated
                out.println("<br><br>Test3. Positive check whether given user authenticated");
                username=request.getRemoteUser();
                if (user.equals(username)){
                        out.println("<br>request.getRemoteUser() test Passed.");
                }else{
                        out.println("<br>request.getRemoteUser() test Failed!");
                }
                out.println("<br>Info:request.getRemoteUser() is= "+username);


                // ----Authorization tests ---
                out.println("<br><br>Test4.Positive check whether the user is in proper role");
                boolean isInProperRole=request.isUserInRole(emp_secrole_ref);
                if (isInProperRole){
                        out.println("<br>HttpServletRequest.isUserInRole() test Passed.");
                }else{
                        out.println("<br>HttpServletRequest.isUserInRole() test Failed!");
                }
                out.println("<br>Info:request.isUserInRole(\""+emp_secrole_ref+"\") is= "+isInProperRole);

                out.println("<br><br>Test5.Negative check whether the current user is any other other role");
                boolean isNotInOtherRole=request.isUserInRole(mgr_secrole_ref);
                if (!isNotInOtherRole){
                        out.println("<br>HttpServletRequest.isUserInRole() test Passed.");
                }else{
                        out.println("<br>HttpServletRequest.isUserInRole() test Failed!");
                }
                out.println("<br>Info:request.isUserInRole(\""+mgr_secrole_ref+"\") is= "+isNotInOtherRole);
                
%>
<%
        // Invalidate the session to make the test as independent and always ask for login
        try{
                session.invalidate();
        }catch(Exception ex){
                ex.printStackTrace();
        }
%>


</body>
</html>

<%--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.

    The contents of this file are subject to the terms of either the GNU
    General Public License Version 2 only ("GPL") or the Common Development
    and Distribution License("CDDL") (collectively, the "License").  You
    may not use this file except in compliance with the License.  You can
    obtain a copy of the License at
    https://oss.oracle.com/licenses/CDDL+GPL-1.1
    or LICENSE.txt.  See the License for the specific
    language governing permissions and limitations under the License.

    When distributing the software, include this License Header Notice in each
    file and include the License file at LICENSE.txt.

    GPL Classpath Exception:
    Oracle designates this particular file as subject to the "Classpath"
    exception as provided by Oracle in the GPL Version 2 section of the License
    file that accompanied this code.

    Modifications:
    If applicable, add the following below the License Header, with the fields
    enclosed by brackets [] replaced by your own identifying information:
    "Portions Copyright [year] [name of copyright owner]"

    Contributor(s):
    If you wish your version of this file to be governed by only the CDDL or
    only the GPL Version 2, indicate your decision by adding "[Contributor]
    elects to include this software in this distribution under the [CDDL or GPL
    Version 2] license."  If you don't indicate a single choice of license, a
    recipient has the option to distribute your version of this file under
    either the CDDL, the GPL Version 2 or to extend the choice of license to
    its licensees as provided above.  However, if you add GPL Version 2 code
    and therefore, elected the GPL Version 2 license, then the option applies
    only if the new code is made subject to such option by the copyright
    holder.

--%>

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

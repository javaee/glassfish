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

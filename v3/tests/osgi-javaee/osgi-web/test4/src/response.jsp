<%--
 Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 Use is subject to license terms.
--%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="javax.servlet.http.*" %>

<%
    String user = (String)request.getParameter("username");
    HttpSession httpSession = request.getSession();
    String users = (String)httpSession.getAttribute("users");
    if ( users == null ) {
	users = user;
    }
    else {
	users = users + ", " + user;
    }
    httpSession.setAttribute("users", users);
%>


<h2><font color="black"><fmt:message key="greeting_response" bundle="${resourceBundle}"/>, <%= users %>!</font></h2>














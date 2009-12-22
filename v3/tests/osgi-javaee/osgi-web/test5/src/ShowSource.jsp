<!--
 The contents of this file are subject to the terms
 of the Common Development and Distribution License
 (the License). You may not use this file except in
 compliance with the License.
 
 You can obtain a copy of the License at
 https://javaserverfaces.dev.java.net/CDDL.html or
 legal/CDDLv1.0.txt. 
 See the License for the specific language governing
 permission and limitations under the License.
 
 When distributing Covered Code, include this CDDL
 Header Notice in each file and include the License file
 at legal/CDDLv1.0.txt.    
 If applicable, add the following below the CDDL Header,
 with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"
 
 [Name of File] [ver.__] [Date]
 
 Copyright 2005 Sun Microsystems Inc. All Rights Reserved
-->

<!--
  Displays the content of the file specified in request
  parameter "filename".
  <%-- Warning!  Can be used to retrieve the source code for
       any file in the 'standard-examples' application.
       It is not advisable to insert any sensitive code
       (even as an experiment) into this application --%>
-->

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ex" uri="/WEB-INF/demo.tld" %>

<%-- 
  Let's get back the URL as a String so we can use it to
  demonstrate "c:import"
--%>
<% pageContext.setAttribute("filepath",
     application.
       getResource(request.getParameter("filename")).toExternalForm()); %>
<% pageContext.setAttribute("filename", request.getParameter("filename")); %>

<html>
<head>
  <title>JSTL: Source code for <c:out value="${filename}"/></title>
</head>
<body bgcolor="#FFFFFF">
<h3>Source code for:&nbsp; <c:out value="${filename}"/></h3>

<hr>

<c:import varReader="reader" url="${filepath}">
  <ex:escapeHtml reader="${reader}"/>
</c:import>
<hr>
</body>
</html>

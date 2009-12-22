<%-- Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, 
use, reproduce, or distribute this software except in compliance with the terms of the 
License at: 
 http://developer.sun.com/berkeley_license.html
 --%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/blueprints/ee5/components/ui" prefix="d" %>

<f:view>

<html>
<head>

<title>AJAX enabled Progress Bar</title>
</head>
<body>

  <h:form id="form">
  
  <p>A simple page that has a button that kicks off a long-running 
  process.</p>
  
  <p><h:commandButton value="Start" action="start" /></p>

<p><a href='<%= request.getContextPath() + "/index.jsp" %>'>Back</a> to home page.</p>

  </h:form>
</f:view>

</body>
</html>

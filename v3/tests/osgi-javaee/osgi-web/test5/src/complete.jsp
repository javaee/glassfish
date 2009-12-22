<%-- Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, 
use, reproduce, or distribute this software except in compliance with the terms of the 
License at: 
 http://developer.sun.com/berkeley_license.html
 --%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:view>

<html>
<head>

<title>Page that is shown after the long running task is complete.</title>
</head>
<body>

  <h:form id="form">

  <p>This page is shown after the long running process completes</p>
  
  <p><h:commandButton action="progressbar" value="Start Over" /></p>
  
  
</h:form>
</f:view>

</body>
</html>

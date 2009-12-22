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

<title>AJAX enabled Button and Progress Bar</title>
</head>
<body>

  <h:form id="form">
  <table>

    <tr><td><d:progressBar id="progressBar" 
                           value="#{process.percentage}" 
                           interval="#{process.pollInterval}" 
                           action="complete" />
        </td> <td>&nbsp;</td></tr>
        
  </table>
  
  <p>For a detailed description of this component, please see <a href="https://bpcatalog.dev.java.net/ajax/progress-bar-jsf/">the blueprints
  catalog entry</a>.</p>

</h:form>
</f:view>

</body>
</html>

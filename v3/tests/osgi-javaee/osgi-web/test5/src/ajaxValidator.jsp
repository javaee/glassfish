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

<title>AJAX validator</title>
</head>
<body>

      <h:form>
      
        <p>Call a server side validator using AJAX.  Display the
        result when the onblur event happens.</p>
        
        <p>Enter a string greater than three characters.  Validation is
        fired on the "onkeypress" event.</p>
      
        <p><d:ajaxValidator messageId="input1" eventHook="onkeypress">
          <h:inputText>
            <f:validateLength minimum="3" />
          </h:inputText>
        </d:ajaxValidator></p>
        
        <span id="input1" style="color:red"></span>
        
        <p>Enter a date in all numeric format, like this: 12/12/1995.
        Validation is fired on the "onblur" event.</p>
        
        <p><d:ajaxValidator messageId="input2">
          <h:inputText>
            <f:convertDateTime dateStyle="short"/>
          </h:inputText>
        </d:ajaxValidator></p>
        
        <span id="input2" style="color:blue"></span>
        
        <p><h:commandButton value="reload"/></p>

<p><a href='<%= request.getContextPath() + "/index.jsp" %>'>Back</a> to home page.
</p>
        
      </h:form>
</f:view>

</body>
</html>

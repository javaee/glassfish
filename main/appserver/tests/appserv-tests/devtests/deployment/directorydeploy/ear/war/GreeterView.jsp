<%--
 Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 Use is subject to license terms.
--%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<fmt:setBundle basename="LocalStrings"/>
<% String nameString = request.getParameter("name"); %> 
<% String messageString = (String) request.getAttribute("message"); %> 

<HTML> 
  <HEAD><TITLE><fmt:message key="greeter_title"/></TITLE></HEAD> 
  <BODY BGCOLOR=#FFFFFF> 
    <H2><fmt:message key="hello_world"/> !</H2> 
    <p> 
      <fmt:message key="good"/>  <%= messageString%>, <%= nameString%>.  <fmt:message key="enjoy_your"/> <%= messageString%>. 
    </p> 
  </BODY> 
</HTML> 

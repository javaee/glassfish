<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Congratulations-JSF Test</title>
    </head>
    <body>

    <h1>Congratulations-Success</h1>
    <f:view>
        <h:form>
            <p>You've successfully registered with jAstrologer</p>
            <p>Your name is ;should display here through JSF Tag--<h:outputText value="#{UserBean.name}"/></p>
            <p>Your birthday is;should display here through JSF Tag--<h:outputText value="#{UserBean.birthday}"/></p>
        </h:form>
    </f:view>
    
    <p>You've successfully registered with jAstrologer.</p>
    
        
    </body>
</html>

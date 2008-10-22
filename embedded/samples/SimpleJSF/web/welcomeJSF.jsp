<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%--
    This file is an entry point for JavaServer Faces application.
--%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <f:view>
            <h1><h:outputText value="JavaServer Faces" /></h1>
            <h:form>
                <p>Enter your name: <h:inputText value="name" /></p>
            <p>Enter your birthday: <h:inputText value="birthday" /></p>
            <h:commandButton value="Submit" action="submit" />

            </h:form>
        </f:view>
    </body>
</html>

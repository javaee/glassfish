<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 

--%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Greetings-JSF Test</title>
    </head>
    <body>
        
        <h1>Welcome to jAstrologer</h1>
        
        
        <f:view>
            <h:form>
                <p>Enter your name: <h:inputText value="#{UserBean.name}"/></p>
                <p>Enter your birthday: <h:inputText value="#{UserBean.birthday}"/></p>
                <h:commandButton value="Submit" action="submit" />
            </h:form>
        </f:view>
        
    </body>
</html>
    
    
    
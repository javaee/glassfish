<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<html>
<head><title>Test webapp context.xml JSP Page</title></head>
<body>
<%
    javax.naming.Context initCtx = new javax.naming.InitialContext();
%>

<%=initCtx.lookup("java:comp/env/webapp-env")%>
<%=initCtx.lookup("java:comp/env/jdbc/__default")%>
</body>
</html>

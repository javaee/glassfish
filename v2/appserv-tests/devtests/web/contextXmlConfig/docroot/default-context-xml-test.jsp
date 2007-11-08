<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<html>
<head><title>Test context.xml.default JSP Page</title></head>
<body>
<%
    javax.naming.Context initCtx = new javax.naming.InitialContext();
%>

<%=initCtx.lookup("java:comp/env/virtual-server-env")%>
</body>
</html>

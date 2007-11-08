<%@ page errorPage="/myError.jsp" %>

<%
throw new Throwable("The cake fell in the mud");
%>
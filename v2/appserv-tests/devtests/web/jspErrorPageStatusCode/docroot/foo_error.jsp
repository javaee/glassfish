<%@ page isErrorPage="true" %>
This is foo_error.jsp
<%
out.println("Status code:");
out.println(request.getAttribute("javax.servlet.error.status_code"));
out.println("Exception:");
out.println(request.getAttribute("javax.servlet.error.exception"));

%>

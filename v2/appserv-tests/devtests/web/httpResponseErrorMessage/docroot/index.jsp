<%@page contentType="text/html"%>
<%
    // some unicode characters that result in CRLF being printed
    final String CRLF = "\u010D\u010A";
    final String payload = CRLF + CRLF + "<script type='text/javascript'>document.write('Hi, there!')</script><div style='display:none'>";
    final String message = "Authorization is required to access " + payload;
    response.sendError(403, message);
%>

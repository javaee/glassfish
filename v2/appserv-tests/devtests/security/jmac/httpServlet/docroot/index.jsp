Hello World from 196 HttpServlet AuthModule Test!
<hr>
<%
    try {
        out.println("Hello, " + request.getUserPrincipal() +
            " from " + request.getAttribute("MY_NAME"));
        out.println("PC = " + request.getAttribute("PC"));
    } catch(Throwable t) {
        out.println("Something wrong: " + t);
        t.printStackTrace();
    }
%>
<hr>

Hello World from 196 HttpServlet AuthModule Test!
<hr>
<%
    try {
        out.println("Hello, " + request.getRemoteUser() +" from "
            + request.getAttribute("MY_NAME"));
    } catch(Exception ex) {
        out.println("Something wrong: " + ex);
        ex.printStackTrace();
    }
%>
<hr>

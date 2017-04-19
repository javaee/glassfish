Hello World from 196 HttpServletChallenge AuthModule Test!
<hr>
<%
    try {
        out.println("Hello, " + request.getUserPrincipal() +
            " from " + request.getAttribute("MY_NAME") +
            " with authType " + request.getAuthType());
    } catch(Exception ex) {
        out.println("Something wrong: " + ex);
        ex.printStackTrace();
    }
%>
<hr>

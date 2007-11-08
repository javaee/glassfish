Hello World from 196 HttpServlet AuthModule Test!
<hr>
<%
    try {
        out.println("Hello, CN=SSLTest, OU=Sun Java System Application Server, O=Sun Microsystems, L=Santa Clara, ST=California, C=US from " 
            + request.getAttribute("MY_NAME"));
    } catch(Exception ex) {
        out.println("Something wrong: " + ex);
        ex.printStackTrace();
    }
%>
<hr>

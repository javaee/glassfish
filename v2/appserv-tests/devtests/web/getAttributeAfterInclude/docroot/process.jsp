<%@ page contentType="text/plain" %>

<%

if (request.getAttribute("javax.servlet.include.query_string") != null){
    out.println("javax.servlet.include.query_string::PASS");
} else {
    out.println("javax.servlet.include.query_string::FAIL");
}
%>





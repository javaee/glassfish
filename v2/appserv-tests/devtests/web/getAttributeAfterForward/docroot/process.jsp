<%@ page contentType="text/plain" %>
<%@ page import="java.util.*" %>

<%
ArrayList attrNames = Collections.list(request.getAttributeNames());
if (Boolean.valueOf(attrNames.contains("javax.servlet.include.request_uri")).booleanValue()){
    out.println("javax.servlet.include.request_uri::PASS");
} else {
    out.println("javax.servlet.include.request_uri::FAIL");
}
                        
if (Boolean.valueOf(attrNames.contains("javax.servlet.forward.request_uri")).booleanValue()){
    out.println("javax.servlet.forward.request_uri::PASS");
} else {
    out.println("javax.servlet.forward.request_uri:FAIL");
}

if (request.getAttribute("javax.servlet.include.request_uri").equals("/web-getAttributeAfterForward/process.jsp")){
    out.println("javax.servlet.include.request_uri_name::PASS");
} else {
    out.println("javax.servlet.include.request_uri_name:FAIL");
}


if (request.getAttribute("javax.servlet.forward.request_uri").equals("/web-getAttributeAfterForward/forward.jsp")){
    out.println("javax.servlet.forward.request_uri_name::PASS");
} else {
    out.println("javax.servlet.forward.request_uri_name:FAIL");
}
%>





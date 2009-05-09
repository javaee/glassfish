<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
<head>
<META HTTP-EQUIV="pragma" CONTENT="no-cache">

<title>simple auth quicklook test</title>
</head>
<body>

<%@ page import="java.security.Principal" %>

<HR>
<BR>

<P>Will attempt to retrieve getUserPrincipal() now.

<%
        String user = null;
        Principal userp = request.getUserPrincipal();
        String method = request.getAuthType();
        if (userp != null) {
                user = userp.toString();
        }

%>

<!-- the lines within pre block are parsed by WebTest. -->
<br>
<pre>
RESULT: principal: <%= user %>
RESULT: authtype: <%= method %>
</pre>

</body>
</html>

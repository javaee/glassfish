<%@page contentType="text/html; charset=UTF-8" %>
<html><title>Simple internationalized jsp with resource bundles</title>
<head>
<%@ page import='java.util.*' %>
</head>
<body>
<% request.setCharacterEncoding("UTF-8"); %>
<% ResourceBundle rb = ResourceBundle.getBundle("LocalStrings", request.getLocale()); %>
<br><H1> Hello, the following messages are displayed from a resource bundle</H1>
<br><br>
<H2><%= rb.getString("msg") %></H2>
<br><br>
<H3><%= rb.getString("thanks") %></H3>
<P><BR><A HREF="/i18n-simple">Back to sample home</A></P>
<P STYLE="margin-bottom: 0cm"><FONT SIZE=1><FONT FACE="Verdana, Arial, Helvetica, sans-serif"><FONT COLOR="#000000">Copyright
(c) 2002 Sun Microsystems, Inc. All rights reserved.</FONT></FONT></FONT>
</body>
</html>

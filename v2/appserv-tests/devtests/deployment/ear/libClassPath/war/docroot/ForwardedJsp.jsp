<%@page contentType="text/html; charset=UTF-8" %>
<html>
<body>
<% request.setCharacterEncoding("UTF-8"); %>
<br><H4> Hello <%= request.getParameter("name") %>, </H4><br>
The name entered: <%= request.getParameter("name") %> should display properly.
<br>
<br>
Otherwise try again after changing the charset in the jsp appropriatly.
<br><br>
Thank you.
<P><BR><A HREF="/i18n-simple">Back to sample home</A></P>
<br><P STYLE="margin-bottom: 0cm"><FONT SIZE=1><FONT FACE="Verdana, Arial, Helvetica, sans-serif"><FONT COLOR="#000000">Copyright
(c) 2003 Sun Microsystems, Inc. All rights reserved.</FONT></FONT></FONT>
</body>
</html>

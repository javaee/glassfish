<%@page contentType="text/html" %>
<%-- request.setCharacterEncoding("UTF-8"); --%>
<jsp:useBean id="myusebean"  scope="request" class="samples.i18n.simple.servlet.samplebean.JavaUseBean" >
    <jsp:setProperty  name="myusebean"  property="name" value='<%= request.getParameter("name") %>' />
    <jsp:setProperty  name="myusebean"  property="greet"  value='<%= request.getParameter("greeting") %>' />
</jsp:useBean>
<html>
<body>
<H2> Hello <jsp:getProperty  name="myusebean"  property="name" /> </H2>

<H3> The name entered:  
     <jsp:getProperty  name="myusebean"  property="name" /> </H3>
     <br>
<H3> The greeting message entered: <br>
     <jsp:getProperty  name="myusebean"  property="greet" /> </H3>
     <br>
<H2> Message generated from usebean: <br>
     <jsp:getProperty  name="myusebean"  property="greetString" /> </H2>
    <br>
<P><BR><A HREF="/i18n-simple">Back to sample home</A></P>    
<P STYLE="margin-bottom: 0cm"><FONT SIZE=1><FONT FACE="Verdana, Arial, Helvetica, sans-serif"><FONT COLOR="#000000">Copyright
(c) 2003 Sun Microsystems, Inc. All rights reserved.</FONT></FONT></FONT>
</body>
</html>


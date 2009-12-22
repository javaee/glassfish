<%-- 
    Document   : index
    Created on : 10 Nov, 2009, 12:13:01 PM
    Author     : mohit
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Index Page</title>
    </head>
    <body>
        <table border="1" width="100%">
            <tr>
                <td colspan="2" align="center" height="50" bgcolor="black">
                    <font color="white" style="font-weight:bold">OSGI Framework Controller</font>
                </td>
            </tr>
            <tr>
                <td width="20%" align="center">
                    <a href="install.jsp" target="mainFrame">Install Bundle</a> <br><br>
                    <a href="uninstall.jsp" target="mainFrame">Uninstall Bundle</a> <br><br>
                    <a href="web/bundleviewer" target="mainFrame">View Bundles </a>
                </td>
                <td height="100%">
                    <iframe name="mainFrame" width="100%" height="500">
                        <p>Nothing HERE..</p>
                    </iframe>
                </td>
            </tr>
        </table>
    </body>
</html>

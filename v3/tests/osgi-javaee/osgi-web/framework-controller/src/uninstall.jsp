<%--
    Document   : uninstall
    Created on : 9 Nov, 2009, 12:26:19 PM
    Author     : mohit
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Bundle Uninstaller</title>
    </head>
    <body>
        <form action="web/bundleuninstaller" method="post">
            BundleId : &nbsp;&nbsp;<input type="text" name="bundleId" value="" /> <br>
            <input type="submit" value="Uninstall Bundle" />
        </form>
        <br>
    </body>
</html>
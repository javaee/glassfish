<%--
    Document   : install
    Created on : 6 Nov, 2009, 2:20:52 PM
    Author     : mohit
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Bundle Installer</title>
    </head>
    <body>
        <form action="web/bundleinstaller" method="post">
            Install URL : &nbsp;&nbsp;<input type="text" name="installUrl" value="" /> <br>
            <input type="submit" value="Install Bundle" />
        </form>
        <br>
    </body>
</html>

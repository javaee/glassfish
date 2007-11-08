<%@ page session="false" %>
<%@ page import="java.net.*" %>
<%
    ClassLoader cl = this.getClass().getClassLoader();

    URLConnection conn = cl.getResource("test.txt").openConnection();
    System.out.println("Getting input stream from " + conn);
    conn.getInputStream();

    conn = cl.getResource("/../lib/test.jar").openConnection();
    System.out.println("Getting input stream from " + conn);
    conn.getInputStream();
%>

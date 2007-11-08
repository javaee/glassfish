<%@taglib prefix="my" uri="http://java.sun.com/test-taglib"%>

<%
    String[] args = new String[] { "aaa", "bbb", "ccc" };
    request.setAttribute("args", args);
 %>

<my:custom array="${args}"/>

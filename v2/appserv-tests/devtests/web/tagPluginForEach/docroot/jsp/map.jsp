<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" import="java.util.HashMap" %>

<% HashMap m = new HashMap();
  m.put("One", "One"); m.put("Two", "Two"); m.put("Three", "Three");
  pageContext.setAttribute("map", m);
%>

<c:forEach items="${map}" var="item" >${item}</c:forEach>



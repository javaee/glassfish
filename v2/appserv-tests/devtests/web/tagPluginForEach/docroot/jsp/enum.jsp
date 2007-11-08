<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" import="java.util.Vector" %>

<% Vector v = new Vector();
  v.add("One"); v.add("Two"); v.add("Three");
  pageContext.setAttribute("enumeration", v.elements());
%>

<c:forEach items="${enumeration}" var="item" >${item}</c:forEach>



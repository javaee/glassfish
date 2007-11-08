<%@ page language="java" %>

<% if (request.isUserInRole("asadmin")) { %><li>jsp-security::PASS</li><% } %>
<% if (request.isUserInRole("Admin")) { %>jsp-security::PASS<% } %>
<% if (request.isUserInRole("Aleph")) { %><li>jsp-security::FAIL</li><% } %>


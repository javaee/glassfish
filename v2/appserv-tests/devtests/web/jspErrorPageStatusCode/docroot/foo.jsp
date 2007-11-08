<%@ page errorPage="foo_error.jsp" %>
This is foo.jsp
<% if(true) throw new IllegalStateException(); %>

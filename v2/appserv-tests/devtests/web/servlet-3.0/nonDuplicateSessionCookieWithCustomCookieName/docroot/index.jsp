<%
  session.invalidate();
  session = request.getSession();
  session.invalidate();
  session = request.getSession();
  out.println("Hello");
%>

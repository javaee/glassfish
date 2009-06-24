<%
request.getRequestDispatcher("bar.jsp?somepar=someval&par=" + request.getParameter("blah")).forward(request, response);
%>

<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
   <% 
            RequestDispatcher rd = request.getRequestDispatcher("Zombie");
            rd.forward(request, response);
     %>

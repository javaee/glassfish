<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>

<%
  pageContext.setAttribute("javax.servlet.jsp.jstl.fmt.localizationContext.page",
                           "TestResourceBundle");
  out.println(LocaleSupport.getLocalizedMessage(pageContext,
                                                "greeting.evening"));
%>

<%@ page import="java.util.*" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>

<%
  ResourceBundle rb = ResourceBundle.getBundle("TestResourceBundle",
                                               new Locale("de"));
  LocalizationContext lc = new LocalizationContext(rb);
  pageContext.setAttribute("javax.servlet.jsp.jstl.fmt.localizationContext.page",
                           lc);
  out.println(LocaleSupport.getLocalizedMessage(pageContext,
                                                "greeting.morning"));
%>

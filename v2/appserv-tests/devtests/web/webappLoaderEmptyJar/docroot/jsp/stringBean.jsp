<HTML>
<HEAD>
<TITLE>Using JavaBeans with JSP</TITLE>
</HEAD>

<BODY>
<TABLE BORDER=5 ALIGN="CENTER">
  <TR><TH CLASS="TITLE">
      Using JavaBeans with JSP</TABLE>
 
<jsp:useBean id="stringBean" class="jspbean.StringBean" />

<OL>
<LI>Initial value (getProperty):
    <I><jsp:getProperty name="stringBean" property="message" /></I>
<LI>Initial value (JSP expression):
    <I><%= stringBean.getMessage() %></I>
<LI><jsp:setProperty name="stringBean" property="message" value="Best string bean: Fortex" />
    Value after setting property with setProperty:
    <I><jsp:getProperty name="stringBean" property="message" /></I>
<LI><% stringBean.setMessage("My favorite: Kentucky Wonder"); %>
    Value after setting property with scriptlet:
    <I><%= stringBean.getMessage() %></I>
</OL>
             
</BODY>
</HTML>

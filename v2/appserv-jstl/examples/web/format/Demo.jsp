<%@ page import="javax.servlet.jsp.jstl.core.Config, java.util.*" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
  <title>JSTL: Formatting/I18N Support -- Demo Example</title>
</head>
<body bgcolor="#FFFFFF">

<h3>Demo</h3>
<p>
The following bundles exist in the web application: 'Resources', 'Resources_de', 'Resources_fr', 'Resources_it'. 
'Resources' is the 'base' bundle and contains all messages in English. The italian bundle only holds one message
(key=greetingMorning).
</p>
<c:if test="${!empty param.locale}">
  <fmt:setLocale value="${param.locale}" scope="page"/>
</c:if>

<c:if test="${!empty param.fallback}">
  <% Config.set(request, Config.FMT_FALLBACK_LOCALE, request.getParameter("fallback")); %>
</c:if>

<table>
<tr>
  <td><b>Set application-based locale:</b></td>
  <td>
<a href='?locale=fr&fallback=<c:out value="${param.fallback}"/>'>French</a> &#149;
<a href='?locale=de&fallback=<c:out value="${param.fallback}"/>'>German</a> &#149;
<a href='?locale=it&fallback=<c:out value="${param.fallback}"/>'>Italian</a> &#149;
<a href='?locale=es&fallback=<c:out value="${param.fallback}"/>'>Spanish (no bundle)</a> &#149;
<a href='?locale=&fallback=<c:out value="${param.fallback}"/>'>None</a>
  </td>
</tr>
<tr>
  <td align="right"><b>Set fallback locale:</b></td>
  <td>
<a href='?locale=<c:out value="${param.locale}"/>&fallback=fr'>French</a> &#149;
<a href='?locale=<c:out value="${param.locale}"/>&fallback=de'>German</a> &#149;
<a href='?locale=<c:out value="${param.locale}"/>&fallback=it'>Italian</a> &#149;
<a href='?locale=<c:out value="${param.locale}"/>&fallback=es'>Spanish (no bundle)</a> &#149;
<a href='?locale=<c:out value="${param.locale}"/>&fallback='>None</a>
  </td>
</table>
<p>

Request parameter "locale": <c:out value="${param.locale}"/><br>
<i>(This value is used to set the application based locale for this example)</i>
<p>

Application based locale: <%=Config.find(pageContext, Config.FMT_LOCALE)%><br>
<i>(javax.servlet.jsp.jstl.fmt.locale configuration setting)</i>
<p>

Browser-Based locales: 
<% 
  Enumeration enum_ = request.getLocales();
  while (enum_.hasMoreElements()) {
    Locale locale = (Locale)enum_.nextElement();
    out.print(locale);
    out.print(" ");
  }
%>
<br>
<i>(ServletRequest.getLocales() on the incoming request)</i>
<p>

Fallback locale: <%=Config.find(pageContext, Config.FMT_FALLBACK_LOCALE)%><br>
<i>(javax.servlet.jsp.jstl.fmt.fallbackLocale configuration setting)</i>
<p>

<jsp:useBean id="now" class="java.util.Date" />
<h4>
<fmt:formatDate value="${now}" dateStyle="full"/> &#149;
<fmt:formatDate value="${now}" type="time"/>
</h4>

<p>

<fmt:bundle basename="org.apache.taglibs.standard.examples.i18n.Resources">
<table cellpadding="5" border="1">
  <tr>
    <th align="left">KEY</th>
    <th align="left">VALUE</th>
  </tr>
  <tr>
    <td>greetingMorning</td>
    <td><fmt:message key="greetingMorning"/></td>
  </tr>
  <tr>
    <td>greetingEvening</td>
    <td><fmt:message key="greetingEvening"/></td>
  </tr>
  <tr>
    <td>currentTime</td>
    <td>
      <fmt:message key="currentTime">
        <fmt:param value="${now}"/>
      </fmt:message>
    </td>
  </tr>
  <tr>
    <td>serverInfo</td>
    <td><fmt:message key="serverInfo"/></td>
  </tr>
  <tr>
    <td>undefinedKey</td>
    <td><fmt:message key="undefinedKey"/></td>
  </tr>
</table>
</fmt:bundle>

<p>

</body>
</html>

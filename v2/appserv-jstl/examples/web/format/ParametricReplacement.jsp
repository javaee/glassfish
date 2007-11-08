<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
  <title>JSTL: Formatting/I18N Support -- Parametric Replacement Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Parametric Replacement</h3>

<jsp:useBean id="now" class="java.util.Date" />

<fmt:setLocale value="de"/>
<fmt:setBundle basename="org.apache.taglibs.standard.examples.i18n.Resources" var="deBundle"/>

<fmt:formatDate value="${now}" type="both" var="currentDateString"/>
<fmt:parseDate value="${currentDateString}" type="both" var="currentDate"/>

<ul>
 <li> Using single &lt;param&gt; with 'value' evaluating to String:<br>
  <fmt:message key="currentTime" bundle="${deBundle}">
   <fmt:param value="${currentDateString}"/>
  </fmt:message>
 
 <li> Using single &lt;param&gt; with 'value' evaluating to <tt>java.util.Date</tt>:<br>
  <fmt:message key="currentTime" bundle="${deBundle}">
   <fmt:param value="${currentDate}"/>
  </fmt:message>

 <li> Using single &lt;param&gt; with body:<br>
  <fmt:message key="currentTime" bundle="${deBundle}">
   <fmt:param>
    <fmt:formatDate value="${now}" type="both"/>
   </fmt:param>
  </fmt:message>

 <li> Using multiple parameters:<br>
  <fmt:message key="serverInfo" bundle="${deBundle}">
   <c:forEach var="arg" items="${serverInfoArgs}">
    <fmt:param value="${arg}"/>
   </c:forEach>
  </fmt:message>
</ul>

<ul>
 <fmt:setLocale value="es"/>
 <fmt:setBundle basename="org.apache.taglibs.standard.examples.i18n.Resources" var="esBundle"/>
 <li>Locale set to Spanish yields a locale-less localization context. Formatting still done
     using the preferred locale:<br>
  <fmt:message key="currentTime" bundle="${esBundle}">
   <fmt:param value="${currentDate}"/>
  </fmt:message>
 </li>

 <fmt:setLocale value="en"/>
 <fmt:setBundle basename="org.apache.taglibs.standard.examples.i18n.Resources" var="enBundle"/>
 <li>Locale set to English yields a locale-less localization context. Formatting still done
     using the preferred locale:<br>
  <fmt:message key="currentTime" bundle="${esBundle}">
   <fmt:param value="${currentDate}"/>
  </fmt:message>
 </li>
</ul>

</body>
</html>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
  <title>JSTL: Formatting/I18N Support -- Italian Locale Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Italian Locale</h3>

<fmt:setLocale value="it_IT"/>
<fmt:setBundle basename="org.apache.taglibs.standard.examples.i18n.Resources" var="itBundle" scope="page"/>
<fmt:message key="greetingMorning" bundle="${itBundle}"/>

</body>
</html>

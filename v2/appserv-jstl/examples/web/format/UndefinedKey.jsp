<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
  <title>JSTL: Formatting/I18N Support -- Undefined Key Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Undefined Key</h3>

<fmt:setLocale value="it"/>
<fmt:setBundle basename="org.apache.taglibs.standard.examples.i18n.Resources" var="itBundle"/>
<fmt:message key="invalidKey" bundle="${itBundle}"/>

</body>
</html>

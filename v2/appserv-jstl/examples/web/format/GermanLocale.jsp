<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
  <title>JSTL: Formatting/I18N Support -- German Locale Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>German Locale</h3>

<fmt:setLocale value="de"/>
<fmt:bundle basename="org.apache.taglibs.standard.examples.i18n.Resources">
 <fmt:message>
  greetingMorning
 </fmt:message>
</fmt:bundle>

</body>
</html>

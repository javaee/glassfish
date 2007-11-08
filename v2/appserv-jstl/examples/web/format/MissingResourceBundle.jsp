<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
  <title>JSTL: Formatting/I18N Support -- Missing Resource Bundle Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Missing Resource Bundle</h3>

<ul>
 <li> Implicit collaboration with &lt;bundle&gt; (via ancestry chain):<br>
  <fmt:bundle basename="org.apache.taglibs.standard.examples.i18n.MissingResources">
   <fmt:message key="greetingMorning"/>
  </fmt:bundle>

 <li> Explicit collaboration with &lt;bundle&gt; (via <tt>var</tt> attribute):<br>
  <fmt:setBundle basename="org.apache.taglibs.standard.examples.i18n.MissingResources" var="enBundle"/>
  <fmt:message key="greetingEvening" bundle="${enBundle}"/>
</ul>

</body>
</html>

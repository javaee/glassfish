<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
  <title>JSTL: Formatting/I18N Support -- Date and Time Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Formatting dates and times using <tt>en-US</tt> locale</h3>

<jsp:useBean id="now" class="java.util.Date" />
<fmt:setLocale value="en-US" />

<ul>
 <li> Formatting current date as &quot;GMT&quot;:<br>
  <fmt:timeZone value="GMT">
   <fmt:formatDate value="${now}" type="both" dateStyle="full"
                   timeStyle="full"/>
  </fmt:timeZone>

 <li> Formatting current date as &quot;GMT+1:00&quot;, and parsing
      its date and time components:<br>
  <fmt:timeZone value="GMT+1:00">
   <fmt:formatDate value="${now}" type="both" dateStyle="full"
                   timeStyle="full" var="formatted"/>
   <fmt:parseDate value="${formatted}" type="both" dateStyle="full"
                  timeStyle="full" timeZone="PST" var="parsedDateTime"/>
   Parsed date: <fmt:formatDate value="${parsedDateTime}" type="date"
                                dateStyle="full"/><br>
   Parsed time: <fmt:formatDate value="${parsedDateTime}" type="time"
                                timeStyle="full"/>
  </fmt:timeZone>

 <li> Parsing SHORT version of current time in different time zones:<br>
  <fmt:formatDate value="${now}" type="both" timeStyle="short"
                  var="formatted"/>
  <fmt:parseDate value="${formatted}" type="both" timeStyle="short"
                 timeZone="GMT" var="parsed"/>
  <c:out value="${parsed}"/> (parsed in &quot;GMT&quot;)<br>

  <fmt:parseDate value="${formatted}" type="both" timeStyle="short"
                 timeZone="GMT+1:00" var="parsed"/>
  <c:out value="${parsed}"/> (parsed in &quot;GMT+1:00&quot;)<br>

  <fmt:parseDate value="${formatted}" type="both" timeStyle="short"
                 timeZone="GMT+3:00" var="parsed"/>
  <c:out value="${parsed}"/> (parsed in &quot;GMT+3:00&quot;)<br>

  <fmt:parseDate value="${formatted}" type="both" timeStyle="short"
                 timeZone="PST" var="parsed"/>
  <c:out value="${parsed}"/> (parsed in &quot;PST&quot;)

 <li> Parsing FULL version of current time in different time zones<br>
      (parse result is independent of specified time zone):<br>
  <fmt:formatDate value="${now}" type="both" timeStyle="full" var="formatted"/>
  <fmt:parseDate value="${formatted}" type="both" timeStyle="full"
                 timeZone="GMT" var="parsed"/>
  <c:out value="${parsed}"/> (parsed in &quot;GMT&quot;)<br>

  <fmt:parseDate value="${formatted}" type="both" timeStyle="full"
                 timeZone="GMT+1:00" var="parsed"/>
  <c:out value="${parsed}"/> (parsed in &quot;GMT+1:00&quot;)<br>

  <fmt:parseDate value="${formatted}" type="both" timeStyle="full"
                 timeZone="GMT+3:00" var="parsed"/>
  <c:out value="${parsed}"/> (parsed in &quot;GMT+3:00&quot;)<br>

  <fmt:parseDate value="${formatted}" type="both" timeStyle="full"
                 timeZone="PST" var="parsed"/>
  <c:out value="${parsed}"/> (parsed in &quot;PST&quot;)
</ul>

</body>
</html>

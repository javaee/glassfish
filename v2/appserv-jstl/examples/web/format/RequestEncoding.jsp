<html>
<head>
  <title>JSTL: Formatting/I18N Support -- Request Encoding Example</title>
</head>
<body bgcolor="#FFFFFF">
This page contains two links, and sends German Umlaut characters as request
parameter values to each of the linked pages.<br>
Only one of the linked pages decodes and displays the request parameter values
correctly:

<ul>
 <li><a href="GermanUmlautIncorrect.jsp?a_umlaut=%C3%A4&o_umlaut=%C3%B6&u_umlaut=%C3%BC">German Umlaut characters decoded incorrectly</a>
 <li><a href="GermanUmlautCorrect.jsp?a_umlaut=%C3%A4&o_umlaut=%C3%B6&u_umlaut=%C3%BC">German Umlaut characters decoded correctly</a>
</ul>

</body>
</html>


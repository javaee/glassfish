<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ex" uri="/jstl-examples-taglib" %>

<html>
<head>
  <title>JSTL: I/O Support -- Absolute URL Example</title>
</head>
<body bgcolor="#FFFFFF">
<h3>Absolute URL</h3>

<h4>CNN's RSS XML feed:</h4>
<blockquote>
<ex:escapeHtml>
  <c:import url="http://www.cnn.com/cnn.rss"/>
</ex:escapeHtml>
</blockquote>

</body>
</html>

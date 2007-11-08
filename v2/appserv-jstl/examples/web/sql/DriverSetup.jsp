<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>

<html>
<head>
  <title>JSTL: SQL action examples</title>
</head>
<body bgcolor="#FFFFFF">

<h1>SQL Driver Setup Example</h1>

<code>
<pre>
&lt;sql:setDataSource
  var="example"
  driver="RmiJdbc.RJDriver"
  url="jdbc:rmi://localhost:1099/jdbc:cloudscape:CloudscapeDB;create=true"
/&gt;
</pre>
</code>

</body>
</html>

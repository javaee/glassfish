<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
   <title>Jakarta DBTAGS Taglib Example</title>
</head>
<body bgcolor="white">

<c:set var="myDbUrl" value="${param.dbUrl}" scope="session"/>
<c:set var="myDbDriver" value="${param.dbDriver}" scope="session"/>
<c:set var="myDbUserName" value="${param.dbUserName}" scope="session"/>
<c:set var="myDbPassword" value="${param.dbPassword}" scope="session"/>

<%@ include file="links.html" %>

</body>
</html>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<html>
<head>
   <title>Jakarta DBTAGS Taglib Example</title>
</head>
<body bgcolor="white">

<%
  String _param = request.getParameter("contextName");
  session.setAttribute("_contextName", _param);
  _param = request.getParameter("contextUrl");
  session.setAttribute("_contextUrl", _param);

  _param = request.getParameter("paramValue1");
  session.setAttribute("_paramValue1", _param);
  _param = request.getParameter("paramValue2");
  session.setAttribute("_paramValue2", _param);
  _param = request.getParameter("paramValue3");
  session.setAttribute("_paramValue3", _param);

  _param = request.getParameter("paramName1");
  session.setAttribute("_paramName1", _param);
  _param = request.getParameter("paramName2");
  session.setAttribute("_paramName2", _param);
  _param = request.getParameter("paramName3");
  session.setAttribute("_paramName3", _param);
%>

<%@ include file="relativeLinks.html" %>

</body>
</html>

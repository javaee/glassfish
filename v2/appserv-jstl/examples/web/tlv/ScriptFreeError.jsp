<%@ taglib prefix="scriptfree" uri="http://jakarta.apache.org/taglibs/standard/scriptfree" %>
<html>
<head>
  <title>JSTL: Tag Library Validator -- ScriptFree Error</title>
</head>
<body bgcolor="#FFFFFF">

<h3>This JSP page is NOT free of scripting elements. Translation error will occur.</h3>

<% java.util.Date date = new java.util.Date(); %>

Date is: <%= date %>

</body>
</html>

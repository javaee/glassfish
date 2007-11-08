<html><!-- #BeginTemplate "/Templates/ExamplesTemplate.dwt" -->
<head>
<!-- #BeginEditable "doctitle" --> 
<title>JSTL: Import Tags Examples</title>
<!-- #EndEditable -->
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="stylesheet" href="../global.css" type="text/css">
</head>

<body bgcolor="#FFFFFF" text="#000000">
<table width="100%" border="0" cellpadding="5">
  <tr> 
    <td height="0"><font color="#000099"><b>standard taglib &#149; implementation 
      of the JSP Standard Tag Library &#149; </b></font><font color="#003399"><a href="mailto:taglibs-user@jakarta.apache.org"><b>support</b></a> 
      &nbsp;&nbsp;<b><a href="mailto:taglibs-dev@jakarta.apache.org">development</a>&nbsp;&nbsp; 
      <a href="mailto:jsr-52-comments@jcp.org">comments to JSR-52</a></b></font></td>
  </tr>
  <tr> 
    <td bgcolor="#CCCCFF"><font size="-1">Examples &nbsp;&nbsp;&nbsp;&nbsp;<a href="../index.html">Introduction</a> 
      &#149; <a href="../elsupport/index.html">General Purpose</a> 
      &#149; <a href="../conditionals/index.html">Conditionals</a> 
      &#149; <a href="../iterators/index.html">Iterators</a> &#149; 
      <a href="index.jsp">Import</a> &#149; <a href="../format/index.html">I18N 
      & Formatting</a> &#149; <a href="../xml/index.html">XML</a> 
      &#149; <a href="../sql/index.jsp">SQL</a> &#149; <a href="../functions/index.html">Functions</a> 
      &#149; <a href="../tlv/index.html">TLV</a> &#149; <a href="../functions/index.html"></a><a href="../misc/index.html">Misc.</a></font></td>
  </tr>
</table>
<!-- #BeginEditable "body" --> 
<%@ include file="links.html" %>
<h2>Context Relative Examples</h2>
<p>For the context relative examples you will need to supply an available relative 
  context name and url before executing the tags.</p>
<form name="myform" action="session.jsp" method="get" >
  <table width="90%" border="0" cellspacing="0" cellpadding="0">
    <tr> 
      <td>Context</td>
      <td> 
        <input type="text" size="40" name="contextName" value="/examples">
      </td>
    </tr>
    <tr> 
      <td>Url</td>
      <td> 
        <input type="text" size="40" name="contextUrl" value="/jsp/simpletag/foo.jsp">
      </td>
    </tr>
    <tr> 
      <td>Param name:</td>
      <td>
        <input type="text" size="40" name="paramName1">
      </td>
      <td>Param value:</td>
      <td>
        <input type="text" size="40" name="paramValue1">
      </td>
    </tr>
    <tr> 
      <td>Param name:</td>
      <td>
        <input type="text" size="40" name="paramName2">
      </td>
      <td>Param value:</td>
      <td>
        <input type="text" size="40" name="paramValue2">
      </td>
    </tr>
    <tr> 
      <td>Param name:</td>
      <td>
        <input type="text" size="40" name="paramName3">
      </td>
      <td>Param value:</td>
      <td>
        <input type="text" size="40" name="paramValue3">
      </td>
    </tr>
  </table>
  <p> 
    <input type="submit" name="Submit" value="Submit">
  </p>
</form>
<!-- #EndEditable -->
<hr noshade color="#000099">
<table width="100%" border="0" cellpadding="5">
  <tr> 
    <td height="24"><font color="#000099"><b>standard taglib &#149; implementation 
      of the JSP Standard Tag Library &#149; </b></font><font color="#003399"><a href="mailto:taglibs-user@jakarta.apache.org"><b>support</b></a> 
      &nbsp;&nbsp;<b><a href="mailto:taglibs-dev@jakarta.apache.org">development</a>&nbsp;&nbsp; 
      <a href="mailto:jsr-52-comments@jcp.org">comments to JSR-52</a></b></font></td>
  </tr>
</table>
</body>
<!-- #EndTemplate --></html>

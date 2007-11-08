<html><!-- #BeginTemplate "/Templates/ExamplesTemplate.dwt" -->
<head>
<!-- #BeginEditable "doctitle" --> 
<title>Untitled Document</title>
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
      <a href="../import/index.jsp">Import</a> &#149; <a href="../format/index.html">I18N 
      & Formatting</a> &#149; <a href="../xml/index.html">XML</a> 
      &#149; <a href="index.jsp">SQL</a> &#149; <a href="../functions/index.html">Functions</a> 
      &#149; <a href="../tlv/index.html">TLV</a> &#149; <a href="../functions/index.html"></a><a href="../misc/index.html">Misc.</a></font></td>
  </tr>
</table>
<!-- #BeginEditable "body" --> 
<h1>SQL Tags Examples</h1>
<p>These examples create their own table for demonstration purposes. Every database 
  supports table creation in a slightly different way. For instance, different 
  databases have different types and constraints. The simplest possible table 
  was chosen, however it may still not be supported by your particular database. 
  Check your database documentation or administrator if you have problems creating 
  the table and modify the examples acordingly. Here is the table used in the 
  examples:</p>
<pre><code>
    create table mytable (
      nameid int primary key,
      name varchar(80)
    )
</code></pre>
<p>Enter your Driver Name and DataBase URL to test the Database Tag Library.</p>
<p>NOTE: You will need to have the DataBase Driver classes available to your server's 
  classloader or in your server's CLASSPATH at startup.</p>
<form name="myform" action="session.jsp" method="post" >
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr> 
      <td>Driver:</td>
      <td> 
        <input type="text" size="40" name="dbDriver" value="RmiJdbc.RJDriver">
      </td>
    </tr>
    <tr> 
      <td>URL:</td>
      <td> 
        <input type="text" size="40" name="dbUrl" value="jdbc:rmi://localhost:1099/jdbc:cloudscape:CloudscapeDB;create=true">
      </td>
    </tr>
    <tr> 
      <td colspan="2"> <br>
        <p>The following two fields are <strong>Optional</strong>. They are included 
          for convenience.<br>
          NOTE: an unsecure <code>form</code> is used.</p>
      </td>
    </tr>
    <tr> 
      <td>User Name:</td>
      <td> 
        <input type="text" size="40" name="dbUserName" value="">
      </td>
    </tr>
    <tr> 
      <td>Password:</td>
      <td> 
        <input type="password" size="40" name="dbPassword" value="">
      </td>
    </tr>
  </table>
  <p> 
    <input type="submit" name="Submit" value="Submit">
  </p>
</form>
<hr>
<p>NOTE: You can access the tags directly here to look at the source. If you do 
  not provide a valid Driver and URL using the above form, the tags will NOT run properly.</p>
<%@ include file="links.html" %>
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

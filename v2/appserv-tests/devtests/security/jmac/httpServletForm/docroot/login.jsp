<%@ page language="java"%> 
<html>
<head><title>Login Page</title></head>
<h2> Welcome </h2>
<BR>
Please login
<BR>
<HR>
<FORM ACTION="j_security_check" METHOD=POST>
<table border=0>
<tr><td align="right">UserName:<td><INPUT TYPE="text" NAME="j_username" VALUE=""> <BR>
<tr><td align="right">Password:<td><INPUT TYPE="password" NAME="j_password" VALUE=""> <BR>
</table>
<BR>
<INPUT TYPE="submit" value="Login"> <INPUT TYPE="reset" value="Clear">

</FORM>
</html>

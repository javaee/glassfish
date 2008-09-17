<html>
<head><title>A JSP Login Page</title></head>
<h2> Login Page </h2>
<BR>
<%
out.println("Please Login \n");
%>
<BR>
<HR>
<FORM ACTION="j_security_check" METHOD=POST>
UserName: <INPUT TYPE="text" NAME="j_username" VALUE=""> <BR>
Password: <INPUT TYPE="password" NAME="j_password" VALUE=""> <BR>
<BR>
<INPUT TYPE="submit" value="Login"> <INPUT TYPE="reset" value="Clear">
</FORM>
</html>

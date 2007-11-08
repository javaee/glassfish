<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
   <title>Grizzly Comet Chat</title>
   <script language="JavaScript">
   function openWindow(url)
       {popupWin = window.open(url, 'remote',
       'menubar=no, toolbar=no, location=no, directories=no, status=no, scrollbars=no, resizable=yes, dependent, width=400, height=400, left=50, top=50')
       }
   </script>
</head>

<body bgcolor="#FFFFFF">
<form method="POST" action='chat' name="postForm">
<input type="hidden" name="action" value="post"/>
Message: <input type="text" name="message"/>
</form>

<br>
<br>

<a href="javascript:openWindow('http://<%=request.getLocalAddr()%>:<%=request.getLocalPort()%>/examples/chat?action=openchat_admin', 640, 480 ,0 ,0 ,0 ,0 ,0 ,1 ,10 ,10 )">Open Moderator Chat Window</a>
</body>
</html>

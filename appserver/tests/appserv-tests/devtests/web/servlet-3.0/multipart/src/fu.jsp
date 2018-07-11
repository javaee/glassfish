<html>
<head>
  <title>File Upload Example</title>
</head>

<body>
  <form action="upload.jsp" method="post"
        enctype="multipart/form-data">
    Choose a file to upload.
    <input name="myFile" type="file"/>
    <input name="myFile2" type="file"/>
    <input type="submit"/>
    <input type="reset"/>
  </form>
</body>
</html>

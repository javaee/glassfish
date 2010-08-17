<%
  String message = (String)request.getAttribute("message");
%>
        <html>
          <head>
            <title>Simple Bank Application</title>
          </head>
          <body>
            <b><%=message%></b>
            <table border="1">
            <form method="post" action="/subclassing/servlet/SimpleBankServlet">
            <tr>
              <td>Social Security Number</td>
              <td><input type="text" name="SSN"></td>
            </tr>
            <tr>
              <td>Last Name</td>
              <td><input type="text" name="lastName"></td>
            </tr>
            <tr>
              <td>First Name</td>
              <td><input type="text" name="firstName"></td>
            </tr>
            <tr>
              <td>Address1</td>
              <td><input type="text" name="address1"></td>
            </tr>
            <tr>
              <td>Address2</td>
              <td><input type="text" name="address2"></td>
            </tr>
            <tr>
              <td>City</td>
              <td><input type="text" name="city"></td>
            </tr>
            <tr>
              <td>State</td>
              <td><input type="text" name="state" maxlength="2"></td>
            </tr>
            <tr>
              <td>Zip Code</td>
              <td><input type="text" name="zipCode"></td>
            </tr>
            <tr>
              <td colspan="2">
                <input type="submit" name="action" value="<%=message%>">
              </td>
            </tr>
          </form>
          </table>
          <a href="/subclassing/index.html">Return to Main Page</a>
          </body>
        </html>

                
         

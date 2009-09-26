<%
  String SSN = (String)request.getAttribute("SSN");
  String lastName = (String)request.getAttribute("lastName");
  String firstName = (String)request.getAttribute("firstName");
  String address1 = (String)request.getAttribute("address1");
  String address2 = (String)request.getAttribute("address2");
  String city = (String)request.getAttribute("city");
  String state = (String)request.getAttribute("state");
  String zipCode = (String)request.getAttribute("zipCode");
  String currentSavingsBalance = (String)request.getAttribute("currentSavingsBalance");
  String currentCheckingBalance = (String)request.getAttribute("currentCheckingBalance");
%>
        <html>
          <head>
            <title>Simple Bank Application</title>
          </head>
          <body>
            <table border="1">
            <form method="post" action="/installed_libraries_embedded/servlet/SimpleBankServlet">
            <tr>
              <td>Social Security Number</td>
              <td>Name</td>
              <td>Address</td>
            </tr>
            <tr>
              <td>
                <%=SSN%>
              </td>
              <td>
                <%=firstName%> <%=lastName%>
              </td>
              <td>
                <%=address1%>, <%=address2%>, <%=city%>, <%=state%>, <%=zipCode%>
              </td>
            </tr>
            <tr>
              <td>
                Savings Account<br>
                <input type=radio checked name=operationSavings value=credit>Credit
                <input type=radio name=operationSavings value=debit>Debit<br>
                $<input type="text" name="amountSavings" value="0">
              </td>
              <td>
                Checking Account<br>
                <input type=radio checked name=operationChecking value=credit>Credit
                <input type=radio name=operationChecking value=debit>Debit<br>
                $<input type="text" name="amountChecking" value="0">
              </td>
              <td>
                <input type=submit name="action" value="Update">
              </td>
            </tr>
            <tr>
              <td colspan=3>
                Savings Balance :
                <%=currentSavingsBalance%>
                &nbsp;
                Checking Balance :
                <%=currentCheckingBalance%>
              </td>
            </tr>
            <input type="hidden" name="SSN" value="<%=SSN%>">
          </form>
          </table>
          <a href="/installed_libraries_embedded/index.html">Lookup another customer</a>
          </body>
        </html>

                
         

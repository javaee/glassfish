<%--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.

    The contents of this file are subject to the terms of either the GNU
    General Public License Version 2 only ("GPL") or the Common Development
    and Distribution License("CDDL") (collectively, the "License").  You
    may not use this file except in compliance with the License.  You can
    obtain a copy of the License at
    https://oss.oracle.com/licenses/CDDL+GPL-1.1
    or LICENSE.txt.  See the License for the specific
    language governing permissions and limitations under the License.

    When distributing the software, include this License Header Notice in each
    file and include the License file at LICENSE.txt.

    GPL Classpath Exception:
    Oracle designates this particular file as subject to the "Classpath"
    exception as provided by Oracle in the GPL Version 2 section of the License
    file that accompanied this code.

    Modifications:
    If applicable, add the following below the License Header, with the fields
    enclosed by brackets [] replaced by your own identifying information:
    "Portions Copyright [year] [name of copyright owner]"

    Contributor(s):
    If you wish your version of this file to be governed by only the CDDL or
    only the GPL Version 2, indicate your decision by adding "[Contributor]
    elects to include this software in this distribution under the [CDDL or GPL
    Version 2] license."  If you don't indicate a single choice of license, a
    recipient has the option to distribute your version of this file under
    either the CDDL, the GPL Version 2 or to extend the choice of license to
    its licensees as provided above.  However, if you add GPL Version 2 code
    and therefore, elected the GPL Version 2 license, then the option applies
    only if the new code is made subject to such option by the copyright
    holder.

--%>

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
            <form method="post" action="/annotation-subclassing/servlet/SimpleBankServlet">
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
          <a href="/annotation-subclassing/index.html">Lookup another customer</a>
          </body>
        </html>

                
         

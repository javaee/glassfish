<!--
 The contents of this file are subject to the terms
 of the Common Development and Distribution License
 (the License). You may not use this file except in
 compliance with the License.
 
 You can obtain a copy of the License at
 https://javaserverfaces.dev.java.net/CDDL.html or
 legal/CDDLv1.0.txt. 
 See the License for the specific language governing
 permission and limitations under the License.
 
 When distributing Covered Code, include this CDDL
 Header Notice in each file and include the License file
 at legal/CDDLv1.0.txt.    
 If applicable, add the following below the CDDL Header,
 with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"
 
 [Name of File] [ver.__] [Date]
 
 Copyright 2005 Sun Microsystems Inc. All Rights Reserved
-->

<HTML>
    <HEAD> <TITLE> JSF Basic Components Test Page </TITLE> </HEAD>

    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

    <H3> JavaServer Faces custom components test page </H3>
    <hr>

       <f:view>
        <h:form id="demotestForm" >

            <table> 
            <tr> 
              <td> <h:outputText id="testLabel" value="JavaServer Faces custom components test page..." /> </td>
            </tr>

          </TR>
           <td> <a href='<%= request.getContextPath() + "/menu.faces" %>'>Back</a> </td>
          </TR>

          </table>


        </h:form>
     </f:view>
</HTML>

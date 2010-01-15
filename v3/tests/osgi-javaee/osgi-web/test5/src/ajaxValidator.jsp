<!--
 Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

   - Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.

   - Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.

   - Neither the name of Sun Microsystems nor the names of its
     contributors may be used to endorse or promote products derived
     from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
-->

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/blueprints/ee5/components/ui" prefix="d" %>

<f:view>

<html>
<head>

<title>AJAX validator</title>
</head>
<body>

      <h:form>
      
        <p>Call a server side validator using AJAX.  Display the
        result when the onblur event happens.</p>
        
        <p>Enter a string greater than three characters.  Validation is
        fired on the "onkeypress" event.</p>
      
        <p><d:ajaxValidator messageId="input1" eventHook="onkeypress">
          <h:inputText>
            <f:validateLength minimum="3" />
          </h:inputText>
        </d:ajaxValidator></p>
        
        <span id="input1" style="color:red"></span>
        
        <p>Enter a date in all numeric format, like this: 12/12/1995.
        Validation is fired on the "onblur" event.</p>
        
        <p><d:ajaxValidator messageId="input2">
          <h:inputText>
            <f:convertDateTime dateStyle="short"/>
          </h:inputText>
        </d:ajaxValidator></p>
        
        <span id="input2" style="color:blue"></span>
        
        <p><h:commandButton value="reload"/></p>

<p><a href='<%= request.getContextPath() + "/index.jsp" %>'>Back</a> to home page.
</p>
        
      </h:form>
</f:view>

</body>
</html>

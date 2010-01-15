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
<title>Welcome to JavaServer Faces</title>
</head>
<body>

  <f:loadBundle basename="demo.model.Resources" var="mapBundle"/>
  <h:form>
  <table>

    <tr><td>
      <h:outputText  id="welcomeLabel" 
                                    value="#{mapBundle.welcomeLabel}" />
    </td></tr>

    <tr><td>

      <h:graphicImage id="mapImage"
                      url="/images/world.gif"
                   usemap="#worldMap"/>

      <d:map           id="worldMap"
        actionListener="#{imageMap.processAreaSelected}"
                immediate="true"
                  current="NAmericas">

        <d:area        id="NAmericas"
                 value="#{NA}"
              onmouseover="/images/world_namer.gif"
               onmouseout="/images/world.gif"
               targetImage="mapImage"/>

        <d:area        id="SAmericas"
                 value="#{SA}"
              onmouseover="/images/world_samer.gif"
               onmouseout="/images/world.gif"
               targetImage="mapImage"/>

        <d:area        id="Finland"
                 value="#{finA}"
              onmouseover="/images/world_finland.gif"
               onmouseout="/images/world.gif"
               targetImage="mapImage"/>

        <d:area        id="Germany"
                 value="#{gerA}"
              onmouseover="/images/world_germany.gif"
               onmouseout="/images/world.gif"
               targetImage="mapImage"/>

        <d:area        id="France"
                 value="#{fraA}"
              onmouseover="/images/world_france.gif"
               onmouseout="/images/world.gif"
               targetImage="mapImage"/>

      </d:map>

    </td></tr>

  </table>

<hr>


<a href='<%= request.getContextPath() + "/index.jsp" %>'>Back</a> to home page.

<h1>How to Use this Component</h1>

<p>This component renders a clickable image map of the world and regions
can be selected to change the locale.</p>

<p>You can mouse over and click on some parts of the world that speak
U.S. English, French, German, Finnish, and Latin American Spanish.  This
will cause the appropriate Locale to be set into the application,
causing the proper ResourceBundle lookup.</p>

<h1>Custom Tags / Components</h1>

<p>The <code>MapComponent</code> component is driven by the <code>map</code>
tag, and it keeps track of the the selected area on the map.  It determines
the selected area from the incoming request, and fires an
<code>AreaSelectedEvent</code> whenever the selected area is changed.  <code>AreaSelectedEvent</code>
is an <code>ActionEvent</code>.  A method binding reference expression tag attribute is 
used to reference a method in the backing file bean (imagemap), and that 
method listens for action events.  The listener method <code>processAreaSelected</code>,
receives the <code>ActionEvent</code> and sets the locale accordingly.

<p>The <code>AreaComponent</code> component is driven by the <code>area</code>
tag. It uses Javascript events to visually show the selected area, and it sends
the identifier of the selected area as part of the request.  This tag must be
nested within an <code>&lt;d:map&gt;</code> tag.</p>

<h2>JSP Attributes</h2>

<p>Attributes described below apply to map tag.
Attributes can represent values directly or point to them via value binding
expressions except for actionListener attribute that points to a
method reference. 

<table border="1">

<tr>
<th>JSP Attribute Name</th>
<th>What it Does</th>
</tr>

<tr>
<td><code>actionListener</code></td>
<td>Method binding reference that refers to a method that sets the locale
    from information contained in the <code>AreaSelectedEvent</code>.
</td>
</tr>

<tr>
<td><code>immediate</code>
</td>
<td>A flag indicating that the default ActionListener should execute
      immediately (that is, during the Apply Request Values phase of the
      request processing lifecycle, instead of waiting for Invoke
      Application phase). The default value of this property must be false.
</td>
</tr>

<tr>
<td><code>current</code></td>
<td>This refers to the current selected area on the map.
</td>
</tr>

</table>

<hr>

</body>
</html>
</h:form>
</f:view>

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

<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<%@ taglib uri="http://java.sun.com/blueprints/ee5/components/ui" prefix="d" %>

<f:view>
<html>
<head>
<title>Demonstration Components - Menu</title>
</head>
<body bgcolor="white">

<h:form >

<d:stylesheet path="/tree-control-test.css"/>

Render graph as a menu bar (graph retrieved from model):<br>
<d:graph_menubar id="menu2" value="#{GraphBean.menuGraph}" 
    selectedClass="tree-control-selected"
    unselectedClass="tree-control-unselected" immediate="true"/>

<hr>
Render graph as a menu bar (graph specified via JSP):<br>
<d:graph_menubar id="menu3" selectedClass="tree-control-selected"
      unselectedClass="tree-control-unselected" immediate="true">
    <d:graph_menunode  name="Menu" label="Menu 3" >
        <d:graph_menunode  name="File" label="File 3" expanded="true">
            <d:graph_menunode  name="File-New" label="New 3" action="demo-test.faces" />
            <d:graph_menunode  name="File-Open" label="Open 3" action="demo-test.faces" />
            <d:graph_menunode  name="File-Close" label="Close 3" enabled="false" />
            <d:graph_menunode  name="File-Exit" label="Exit 3" action="demo-test.faces" />
        </d:graph_menunode>

       <d:graph_menunode  name="Edit" label="Edit 3" >
           <d:graph_menunode  name="Edit-Cut" label="Cut 3" action="demo-test.faces"/>
           <d:graph_menunode  name="Edit-Copy" label="Copy 3" action="demo-test.faces" />
           <d:graph_menunode  name="Edit-Paste" label="Paste 3" enabled="false" />
       </d:graph_menunode>
   </d:graph_menunode>
</d:graph_menubar>

<hr>
Render graph as a tree control (graph retrieved from model):<br>
<d:graph_menutree id="menu4" value="#{GraphBean.treeGraph}" styleClass="tree-control"
     selectedClass="tree-control-selected" 
     unselectedClass="tree-control-unselected" immediate="true"/>
<hr>
Render graph as a tree control (graph specified via JSP):<br>
<d:graph_menutree id="menu5" selectedClass="tree-control-selected"
    unselectedClass="tree-control-unselected" styleClass="tree-control" 
    immediate="true">
    <d:graph_treenode  name="Menu" label="Menu 5" enabled="false" 
         expanded="true">

        <d:graph_treenode  name="File" label="File 5"
             icon="folder_16_pad.gif" enabled="false">

            <d:graph_treenode  name="File-New" label="New 5"
                icon="folder_16_pad.gif" action="demo-test.faces"/>
            <d:graph_treenode  name="File-Open" label="Open 5"
                icon="folder_16_pad.gif" action="/demo-test.faces" />
            <d:graph_treenode  name="File-Close" label="Close 5" enabled="false"
                icon="folder_16_pad.gif" />
            <d:graph_treenode  name="File-Exit" label="Exit 5"
                icon="folder_16_pad.gif" action="demo-test.faces" />
       </d:graph_treenode>

       <d:graph_treenode  name="Edit" label="Edit 5" 
           icon="folder_16_pad.gif" expanded="true" enabled="false"> 

           <d:graph_treenode  name="Edit-Cut" label="Cut 5" 
               icon="folder_16_pad.gif" action="demo-test.faces"/>
           <d:graph_treenode  name="Edit-Copy" label="Copy 5"
               icon="folder_16_pad.gif" action="demo-test.faces" />
           <d:graph_treenode  name="Edit-Paste" label="Paste 5" enabled="false" 
               icon="folder_16_pad.gif" />
       </d:graph_treenode>
   </d:graph_treenode>
</d:graph_menutree>

<hr>
</h:form>
<a href='<%= request.getContextPath() + "/index.jsp" %>'>Back</a> to home page.

<h1>How to Use this Component</h1>

<p>This component renders a <code>Graph</code> as either a menu bar or a tree control.  
The <code>Graph</code> can be specified as model data, or it can be specified in 
<code>JSP</code>.</p>

<h2>JSP Attributes</h2>

<p>Attributes described below apply to graph_menubar tag as well as graph_tree tag 
since they are just two different visual representations of the Graph Component.
Attributes can represent values directly or point to them via value binding
expressions except for actionListener attribute that points to a 
method reference. This component allows the user to define CSS classes via JSP 
attributes that are output in the rendered markup.  This makes it possible to 
produce highly customizable output. You can compare the rendered source of this 
page, using the "View Source" feature of your browser, with 
<a href="ShowSource.jsp?filename=/menu.jsp">the JSP source</A> for this page.</p>

<table border="1">

<tr>
<th>JSP Attribute Name</th>
<th>What it Does</th>
</tr>

<tr>
<td><code>selectedClass</code></td>
<td>A style sheet class which controls the display attributes of the selected 
menu bar or tree element.  This is used to distinguish the selected portion from 
the other unselected portions.</td>
</tr>
<tr>

<td><code>unselectedClass</code></td>
<td>A style sheet class which controls the display attributes of an unselected menu bar or tree element.  This is used to distinguish an unselected portion from a selected portion.</
<td>
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
<td><code>styleClass</code></td>
<td>The CSS style <code>class</code> to be applied to the entire menu/tree.
</td>
</tr>

<tr>
<td><code>value</code></td>
<td>Value Binding reference expression that points to a Graph in scoped namespace.
</td>
</tr>

<tr>
<td><code>actionListener</code></td>
<td>Method binding reference to handle tree expansion and contraction events.
</td>
</tr>

</table>

<h2>Menu Bar</h2>

<p>The menu bar can be described from a <code>Graph</code> specified in the model or it can be described from <code>JSP</code> tags.</p>

<h3>Described From JSP</h3>

<p>The <code>graph_menubar</code> tag consists of multiple <code>graph_menunode</code> tags.  Each <code>graph_menunode</code> tag corresponds to an item on the menu bar, and you can nest <code>graph_menunode</code> tags within each other.  The <code>graph_menunode</code> tag has attributes that control the visual aspects of the node, and it has an <code>action</code> attribute that can be used to specify a context-relative URL for when the node is selected.  Refer to the tag library descriptor <code>tld</code> file for a complete list of attributes.</p>

<h3>Described From Model</h3>

<p>The <code>graph_menubar</code> tag refers to a <code>Graph</code> model component through the <code>valueRef</code> attribute.  The <code>Graph</code> model component consists of multiple <code>Node</code> components.  Each <code>Node</code> component describes an item on the menu bar.</p>

<h2>Tree Control</h2>

<p>The tree control can be described from a <code>Graph</code> specified in the model or it can be described from <code>JSP</code> tags.</p>

<h3>Described From JSP</h3>

<p>The <code>graph_menutree</code> tag consists of multiple <code>graph_treenode</code> tags.  Each <code>graph_treenode</code> tag corresponds to a node in the tree, and you can nest <code>graph_treenode</code> tags within each other.  The <code>graph_treenode</code> tag has attributes that control the visual aspects of the node, and it has an <code>action</code> attribute that can be used to specify a context-relative URL for when the node is selected.  Refer to the tag library descriptor <code>tld</code> file for a complete list of attributes.</p>

<h3>Described From Model</h3>

<p>The <code>graph_menutree</code> tag refers to a <code>Graph</code> model component through the <code>valueRef</code> attribute.  The <code>Graph</code> model component consists of multiple <code>Node</code> components.  Each <code>Node</code> component describes an item in the tree.</p>

 
<hr>

<a href='<%= request.getContextPath() + "/index.jsp" %>'>Back</a> to home page.

</body>
</html>
</f:view>

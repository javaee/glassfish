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

<html>
<head>
<title>Demonstration Components Home Page</title>
<style type="text/css" media="screen">
TD { text-align: center }
</style>
</head>
<body bgcolor="white">

<%
  pageContext.removeAttribute("graph", PageContext.SESSION_SCOPE);
  pageContext.removeAttribute("list", PageContext.SESSION_SCOPE);
%>

<p>Here is a small gallery of custom components built from JavaServer
Faces technology.</p>


<table border="1">

<tr>

<th>Component Content</th> 

<th>View JSP Source</th> 

<th>View Java Source</th> 

<th>Execute JSP</th></tr>

<tr>
<td>Image Map
</td>

<td><a href="ShowSource.jsp?filename=/imagemap.jsp"><img src="images/code.gif" width="24" height="24" border="0"></a>
</td>

<td>
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/components/AreaComponent.java">com/sun/javaee/blueprints/components/ui/components/AreaComponent.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/components/AreaSelectedEvent.java">com/sun/javaee/blueprints/components/ui/components/AreaSelectedEvent.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/components/AreaSelectedListener.java">com/sun/javaee/blueprints/components/ui/components/AreaSelectedListener.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/components/MapComponent.java">com/sun/javaee/blueprints/components/ui/components/MapComponent.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/model/ImageArea.java">com/sun/javaee/blueprints/components/ui/model/ImageArea.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/renderkit/AreaRenderer.java">com/sun/javaee/blueprints/components/ui/renderkit/AreaRenderer.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/renderkit/MapRenderer.java">com/sun/javaee/blueprints/components/ui/renderkit/MapRenderer.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/AreaTag.java">com/sun/javaee/blueprints/components/ui/taglib/AreaTag.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/MapTag.java">com/sun/javaee/blueprints/components/ui/taglib/MapTag.java</a><br />



</td>

<td><a href="imagemap.faces"><img src="images/execute.gif" width="24" height="24" border="0"></a>
</td>

</tr>


<tr>

<td>Menu or Tree
</td>

<td><a href="ShowSource.jsp?filename=/menu.jsp"><img src="images/code.gif" width="24" height="24" border="0"></a></td>

<td>

<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/GraphMenuBarTag.java">com/sun/javaee/blueprints/components/ui/taglib/GraphMenuBarTag.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/GraphMenuNodeTag.java">com/sun/javaee/blueprints/components/ui/taglib/GraphMenuNodeTag.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/GraphMenuTreeTag.java">com/sun/javaee/blueprints/components/ui/taglib/GraphMenuTreeTag.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/GraphTreeNodeTag.java">com/sun/javaee/blueprints/components/ui/taglib/GraphTreeNodeTag.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/components/GraphComponent.java">com/sun/javaee/blueprints/components/ui/components/GraphComponent.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/model/Graph.java">com/sun/javaee/blueprints/components/ui/model/Graph.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/model/Node.java">com/sun/javaee/blueprints/components/ui/model/Node.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/renderkit/MenuBarRenderer.java">com/sun/javaee/blueprints/components/ui/renderkit/MenuBarRenderer.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/renderkit/MenuTreeRenderer.java">com/sun/javaee/blueprints/components/ui/renderkit/MenuTreeRenderer.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/demo/model/GraphBean.java">demo/model/GraphBean.java</a>
</td>

<td><a href="menu.faces"><img src="images/execute.gif" width="24" height="24" border="0"></a>
</td>

</tr>

<tr>

<td>Repeater
</td>

<td><a href="ShowSource.jsp?filename=/repeater.jsp"><img src="images/code.gif" width="24" height="24" border="0"></a>
</td>

<td>
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/DataRepeaterTag.java">com/sun/javaee/blueprints/components/ui/taglib/DataRepeaterTag.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/renderkit/RepeaterRenderer.java">com/sun/javaee/blueprints/components/ui/renderkit/RepeaterRenderer.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/demo/model/RepeaterBean.java">demo/model/RepeaterBean.java</a><br />
</td>

<td><a href="repeater.faces"><img src="images/execute.gif" width="24" height="24" border="0"></a>
</td>

</tr>

<tr>

<td>Scroller
</td>

<td><a href="ShowSource.jsp?filename=/result-set.jsp"><img src="images/code.gif" width="24" height="24" border="0"></a>
</td>

<td>
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/ScrollerTag.java">com/sun/javaee/blueprints/components/ui/taglib/ScrollerTag.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/components/ScrollerComponent.java">com/sun/javaee/blueprints/components/ui/components/ScrollerComponent.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/demo/model/ResultSetBean.java">demo/model/ResultSetBean.java</a>
</td>

<td><a href="result-set.faces"><img src="images/execute.gif" width="24" height="24" border="0"></a>
</td>

</tr>

<tr>

<td>Tabbed Pane
</td>

<td><a href="ShowSource.jsp?filename=/tabbedpanes.jsp"><img src="images/code.gif" width="24" height="24" border="0"></a>
</td>

<td>
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/PaneTabTag.java">com/sun/javaee/blueprints/components/ui/taglib/PaneTabTag.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/PaneTabLabelTag.java">com/sun/javaee/blueprints/components/ui/taglib/PaneTabLabelTag.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/PaneTabbedTag.java">com/sun/javaee/blueprints/components/ui/taglib/PaneTabbedTag.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/components/PaneComponent.java">com/sun/javaee/blueprints/components/ui/components/PaneComponent.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/components/PaneSelectedEvent.java">com/sun/javaee/blueprints/components/ui/components/PaneSelectedEvent.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/renderkit/TabLabelRenderer.java">com/sun/javaee/blueprints/components/ui/renderkit/TabLabelRenderer.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/renderkit/TabRenderer.java">com/sun/javaee/blueprints/components/ui/renderkit/TabRenderer.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/renderkit/TabbedRenderer.java">com/sun/javaee/blueprints/components/ui/renderkit/TabbedRenderer.java</a><br />
</td>

<td><a href="tabbedpanes.faces"><img src="images/execute.gif" width="24" height="24" border="0"></a>
</td>

</tr>

<tr>

<td>Chart
</td>

<td><a href="ShowSource.jsp?filename=/chart.jsp"><img src="images/code.gif" width="24" height="24" border="0"></a></td>

<td>

<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/ChartTag.java">com/sun/javaee/blueprints/components/ui/taglib/ChartTag.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/ChartItemTag.java">com/sun/javaee/blueprints/components/ui/taglib/ChartItemTag.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/components/ChartComponent.java">com/sun/javaee/blueprints/components/ui/components/ChartComponent.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/components/ChartItemComponent.java">com/sun/javaee/blueprints/components/ui/components/ChartItemComponent.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/renderkit/ChartServlet.java">com/sun/javaee/blueprints/components/ui/renderkit/ChartServlet.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/model/ChartItem.java">com/sun/javaee/blueprints/components/ui/model/ChartItem.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/demo/model/ChartBean.java">demo/model/ChartBean.java</a>
</td>

<td><a href="chart.faces"><img src="images/execute.gif" width="24" height="24" border="0"></a>
</td>

</tr>

<tr>

<td>AJAX Progress Bar
</td>

<td><a href="ShowSource.jsp?filename=/progressbar.jsp"><img src="images/code.gif" width="24" height="24" border="0"></a></td>

<td>

<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/renderkit/AjaxPhaseListener.java">com/sun/javaee/blueprints/components/ui/renderkit/AjaxPhaseListener.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/components/ProgressBarComponent.java">com/sun/javaee/blueprints/components/ui/components/ProgressBarComponent.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/renderkit/ProgressBarRenderer.java">com/sun/javaee/blueprints/components/ui/renderkit/ProgressBarRenderer.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/ProgressBarTag.java">com/sun/javaee/blueprints/components/ui/taglib/ProgressBarTag.java</a><br />
<a href="ShowSource.jsp?filename=/wait.jsp">wait.jsp</a><br />
<a href="ShowSource.jsp?filename=/complete.jsp">complete.jsp</a><br />
</td>

<td><a href="progressbar.faces"><img src="images/execute.gif" width="24" height="24" border="0"></a>
</td>

</tr>

<tr>

<td>AJAX Validator
</td>

<td><a href="ShowSource.jsp?filename=/ajaxValidator.jsp"><img src="images/code.gif" width="24" height="24" border="0"></a></td>

<td>

<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/components/AjaxValidatorComponent.java">com/sun/javaee/blueprints/components/ui/components/AjaxValidatorComponent.java
</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/components/AjaxValidatorPhaseListener.java">com/sun/javaee/blueprints/components/ui/components/AjaxValidatorPhaseListener.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/taglib/AjaxValidatorTag.java">com/sun/javaee/blueprints/components/ui/taglib/AjaxValidatorTag.java</a><br />
<a href="ShowSource.jsp?filename=/src/java/components/java/com/sun/javaee/blueprints/components/ui/renderkit/AjaxPhaseListener.java">com/sun/javaee/blueprints/components/ui/renderkit/AjaxPhaseListener.java</a><br />
</td>

<td><a href="ajaxValidator.faces"><img src="images/execute.gif" width="24" height="24" border="0"></a>
</td>

</tr>


</table>

</body>
</head>

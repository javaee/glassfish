<%--
 Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 Use is subject to license terms.
--%>

<!--
 Copyright 2004 Sun Microsystems, Inc. All Rights Reserved.
 
 Redistribution and use in source and binary forms, with or
 without modification, are permitted provided that the following
 conditions are met:
 
 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
 
 - Redistribution in binary form must reproduce the above
   copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials
   provided with the distribution.
    
 Neither the name of Sun Microsystems, Inc. or the names of
 contributors may be used to endorse or promote products derived
 from this software without specific prior written permission.
  
 This software is provided "AS IS," without a warranty of any
 kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  
 You acknowledge that this software is not designed, licensed or
 intended for use in the design, construction, operation or
 maintenance of any nuclear facility.
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
<a href="ShowSource.jsp?filename=/src/java/components/components/AreaComponent.java">components/components/AreaComponent.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/components/AreaSelectedEvent.java">components/components/AreaSelectedEvent.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/components/AreaSelectedListener.java">components/components/AreaSelectedListener.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/components/MapComponent.java">components/components/MapComponent.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/model/ImageArea.java">components/model/ImageArea.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/renderkit/AreaRenderer.java">components/renderkit/AreaRenderer.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/renderkit/MapRenderer.java">components/renderkit/MapRenderer.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/taglib/AreaTag.java">components/taglib/AreaTag.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/taglib/MapTag.java">components/taglib/MapTag.java</a><br>



</td>

<td><a href="imagemap.faces"><img src="images/execute.gif" width="24" height="24" border="0"></a>
</td>

</tr>


<tr>

<td>Menu or Tree
</td>

<td><a href="ShowSource.jsp?filename=/menu.jsp"><img src="images/code.gif" width="24" height="24" border="0"></a></td>

<td>

<a href="ShowSource.jsp?filename=/src/java/components/taglib/GraphMenuBarTag.java">components/taglib/GraphMenuBarTag.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/taglib/GraphMenuNodeTag.java">components/taglib/GraphMenuNodeTag.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/taglib/GraphMenuTreeTag.java">components/taglib/GraphMenuTreeTag.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/taglib/GraphTreeNodeTag.java">components/taglib/GraphTreeNodeTag.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/components/GraphComponent.java">components/components/GraphComponent.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/model/Graph.java">components/model/Graph.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/model/Node.java">components/model/Node.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/renderkit/MenuBarRenderer.java">components/renderkit/MenuBarRenderer.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/renderkit/MenuTreeRenderer.java">components/renderkit/MenuTreeRenderer.java</a><br>
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
<a href="ShowSource.jsp?filename=/src/java/components/taglib/DataRepeaterTag.java">components/taglib/DataRepeaterTag.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/renderkit/RepeaterRenderer.java">components/renderkit/RepeaterRenderer.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/demo/model/RepeaterBean.java">demo/model/RepeaterBean.java</a><br>
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
<a href="ShowSource.jsp?filename=/src/java/components/taglib/ScrollerTag.java">components/taglib/ScrollerTag.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/components/ScrollerComponent.java">components/components/ScrollerComponent.java</a><br>
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
<a href="ShowSource.jsp?filename=/src/java/components/taglib/PaneTabTag.java">components/taglib/PaneTabTag.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/taglib/PaneTabLabelTag.java">components/taglib/PaneTabLabelTag.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/taglib/PaneTabbedTag.java">components/taglib/PaneTabbedTag.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/components/PaneComponent.java">components/components/PaneComponent.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/components/PaneSelectedEvent.java">components/components/PaneSelectedEvent.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/renderkit/TabLabelRenderer.java">components/renderkit/TabLabelRenderer.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/renderkit/TabRenderer.java">components/renderkit/TabRenderer.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/renderkit/TabbedRenderer.java">components/renderkit/TabbedRenderer.java</a><br>
</td>

<td><a href="tabbedpanes.faces"><img src="images/execute.gif" width="24" height="24" border="0"></a>
</td>

</tr>

<tr>

<td>Chart
</td>

<td><a href="ShowSource.jsp?filename=/chart.jsp"><img src="images/code.gif" width="24" height="24" border="0"></a></td>

<td>

<a href="ShowSource.jsp?filename=/src/java/components/taglib/ChartTag.java">components/taglib/ChartTag.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/taglib/ChartItemTag.java">components/taglib/ChartItemTag.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/components/ChartComponent.java">components/components/ChartComponent.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/components/ChartItemComponent.java">components/components/ChartItemComponent.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/renderkit/ChartServlet.java">components/renderkit/ChartServlet.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/components/model/ChartItem.java">components/model/ChartItem.java</a><br>
<a href="ShowSource.jsp?filename=/src/java/demo/model/ChartBean.java">demo/model/ChartBean.java</a>
</td>

<td><a href="chart.faces"><img src="images/execute.gif" width="24" height="24" border="0"></a>
</td>

</tr>

<tr>

</table>

</body>
</head>

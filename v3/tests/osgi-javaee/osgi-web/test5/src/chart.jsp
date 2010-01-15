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

<%-- $Id: chart.jsp,v 1.8 2005/10/26 21:37:12 edburns Exp $ --%>



<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
<%@ taglib uri="http://java.sun.com/blueprints/ee5/components/ui" prefix="d" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<f:view>
<html>
<head>
  <title>Chart Example</title>
</head>
<body bgcolor="white">

<h:form>
       
     <hr>
     <table> 
      <tr>
         <th align="left"><h:outputText value="Vertical Bar Chart with data specifed via JSP" /></th>
      </tr> 
      <tr> 
      <td>
      <d:chart width="300" height="300" title="Employee Number By Department" xlabel="Departments" ylabel="Employees">
         <d:chartItem itemLabel="Eng" itemValue="200" itemColor="red" />
         <d:chartItem itemLabel="Mktg" itemValue="400" itemColor="green" />
         <d:chartItem itemLabel="Sales" itemValue="250" itemColor="blue" />
         <d:chartItem itemLabel="R&D" itemValue="350" itemColor="orange" />
         <d:chartItem itemLabel="HR" itemValue="450" itemColor="cyan" />
      </d:chart> 
      </td>
      <td>
      <table>
      <tr>
         <th align="left"><h:outputText value="Horizontal Bar Chart with data specifed via JSP" /></th>
      </tr>
      <tr>
      <td>
      <d:chart width="300" height="300" type="bar" orientation="horizontal" 
         title="Employee Number By Department" xlabel="Employees" ylabel="Departments">
         <d:chartItem itemLabel="Eng" itemValue="200" itemColor="red" />
         <d:chartItem itemLabel="Mktg" itemValue="400" itemColor="green" />
         <d:chartItem itemLabel="Sales" itemValue="250" itemColor="blue" />
         <d:chartItem itemLabel="R&D" itemValue="350" itemColor="orange" />
         <d:chartItem itemLabel="HR" itemValue="450" itemColor="cyan" />
      </d:chart>
      </td>
      </tr>
      <tr>
         <th align="left"><h:outputText value="Pie Chart with data specifed via JSP" /></th>

      </tr>
      <tr>
     <td>
     <d:chart width="400" height="200" type="pie" 
         title="Employee Number By Department">
         <d:chartItem itemLabel="Eng" itemValue="200" itemColor="red" />
         <d:chartItem itemLabel="Mktg" itemValue="400" itemColor="green" />
         <d:chartItem itemLabel="Sales" itemValue="600" itemColor="blue" />
         <d:chartItem itemLabel="R&D" itemValue="700" itemColor="orange" />
         <d:chartItem itemLabel="HR" itemValue="800" itemColor="cyan" />
     </d:chart> </td>
     </tr>
      </table>
      <td>
      </tr> 
    </table>

</h:form>
</f:view>

  <hr>
  <a href='<%= request.getContextPath() + "/index.jsp" %>'>Back</a> to home page.

<h1>How to Use this Component</h1>

<p>This component generates different types of charts like Bar and Pie.
</p>

<h2>JSP Attributes</h2>

<table border="1">

<tr>
<th>JSP Attribute Name
</th>
<th>What it Does
</th>
</tr>

<tr>

<td><code>width</code>
</td>
<td>
A value binding expression or a literal value corresponding to the width of the chart.
</td>
</tr>

<tr>
<td><code>height</code>
</td>
<td>A value binding expression or a literal value corresponding to the height of the chart.
</td>
</tr>

<tr>
<td><code>orientation</code></td>
<td>A value binding expression or a literal value corresponding to the orientation of the chart.
This attribute applies to bar charts only. Valid values are "horizontal" and "vertical".
Default orientation is "vertical".
</td>
</tr>

<tr>
<td><code>title</code></td>
<td>A value binding expression or a literal value corresponding to the title of the chart.
</td>
</tr>

<tr>
<td><code>xlabel</code></td>
<td>A value binding expression or a literal value that represents the label for x-axis.
</td>
</tr>

<tr>
<td><code>ylabel</code></td>
<td>A value binding expression or a literal value that represents the label for y-axis.
</td>
</tr>

<tr>
<td><code>type</code></td>
<td>A value binding expression or a literal value that specifies what type of chart to generate.
Supported types are "bar" and "pie". Default type is "bar".
</td>
</tr>

</table>

<h2>How to specify data for chart</h2>

<p>Chart can be described using an array of <code>ChartItem</code> specified in the model or it can be described using <code>JSP</code> tags.</p>

<h3>Described From JSP</h3>

<p>The <code>chart</code> tag consists of multiple <code>chartItem</code> tags.  
Each <code>chartItem</code> tag corresponds to a single column of the chart. 
The <code>chartItem</code> tag supports attributes like itemLabel, itemColor and itemValue
which can be literal value or a valueBinding expression that points to a ChartItem.</p>

<h3>Described From Model</h3>

<p>The <code>value</code> attribute specified on a chart tag refers to an array of <code>ChartItem</code>. 
Each element of the array represents a single column of the chart.</p>

<hr>
<a href='<%= request.getContextPath() + "/index.jsp" %>'>Back</a> to home page.
</body>
</html>


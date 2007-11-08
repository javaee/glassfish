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

<%-- $Id: result-set.jsp,v 1.2 2004/11/14 07:33:19 tcfujii Exp $ --%>



<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/demo/components" prefix="d" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<f:view>
<html>
<head>
  <title>Result Set Example</title>
  <link rel="stylesheet" type="text/css"
       href='<%= request.getContextPath() + "/result-set.css" %>'>
</head>
<body bgcolor="white">

<h:form>  

Rendered via Faces components:

  <h:dataTable columnClasses="list-column-center,list-column-center,
                               list-column-center, list-column-center"
                  headerClass="list-header"
                   rowClasses="list-row-even,list-row-odd"
                   styleClass="list-background"
                           id="table"
                         rows="20"
                      binding="#{ResultSetBean.data}"
                        value="#{ResultSetBean.list}"
                          var="customer">

    <h:column>
      <f:facet           name="header">
        <h:outputText  value="Account Id"/>
      </f:facet>
      <h:outputText        id="accountId"
                     value="#{customer.accountId}"/>
    </h:column>

    <h:column>
      <f:facet           name="header">
        <h:outputText  value="Customer Name"/>
      </f:facet>
      <h:outputText        id="name" value="#{customer.name}"/>
    </h:column>

    <h:column>
      <f:facet           name="header">
        <h:outputText  value="Symbol"/>
      </f:facet>
      <h:outputText        id="symbol"
                     value="#{customer.symbol}"/>
    </h:column>

    <h:column>
      <f:facet           name="header">
        <h:outputText  value="Total Sales"/>
      </f:facet>
      <h:outputText       id="totalSales"
                     value="#{customer.totalSales}"/>
    </h:column>

  </h:dataTable>

  <d:scroller navFacetOrientation="NORTH" for="table" 
          actionListener="#{ResultSetBean.processScrollEvent}">
      <f:facet name="header">
        <h:panelGroup>
          <h:outputText value="Account Id"/>
          <h:outputText value="Customer Name"/>
          <h:outputText value="Symbol"/>
          <h:outputText value="Total Sales"/>
        </h:panelGroup>
      </f:facet>

      <f:facet name="next">
        <h:panelGroup>
          <h:outputText value="Next"/>
          <h:graphicImage url="/images/arrow-right.gif" />
        </h:panelGroup>
      </f:facet>

      <f:facet name="previous">
        <h:panelGroup>
          <h:outputText value="Previous"/>
          <h:graphicImage url="/images/arrow-left.gif" />
        </h:panelGroup>
      </f:facet>

      <f:facet name="number">
         <!-- You can put a panel here if you like -->
      </f:facet>

      <f:facet name="current">
        <h:panelGroup>
          <h:graphicImage url="/images/duke.gif" />
        </h:panelGroup>
      </f:facet>
  </d:scroller>
</h:form>

  <hr>
  <a href='<%= request.getContextPath() + "/index.jsp" %>'>Back</a> to home page.

<h1>How to Use this Component</h1>

<p>This component produces a search engine style scroller that facilitates
   easy navigation over results that span across several pages.
</p>

<h2>JSP Attributes</h2>

<p>This component relies on the presence of a data grid to display the results
   in the form of a table. You can compare the
rendered source of this page, using the "View Source" feature of your
browser, with <a href="ShowSource.jsp?filename=/result-set.jsp">the JSP
source</A> for this page.</p>

<table border="1">

<tr>
<th>JSP Attribute Name
</th>
<th>What it Does
</th>
</tr>

<tr>

<td><code>navFacetOrientation</code>
</td>

<td>"NORTH", "SOUTH", "EAST", or "WEST".  This attribute tells where to
put the number that means "skip to page N in the result set" in relation
the facet.
</td>
</tr>

<tr>
<td><code>forValue</code>
</td>
<td>The data grid component for which this acts as a scroller.
</td>
</tr>

<tr>
<td><code>actionListener</code></td>
<td>Method binding reference to handle an action event generated as a result of 
    clicking on a link that points a particular page in the result-set.
</td>
</tr>
</table>

<h2>Facets</h2>

<p>You can define Facets for each of the following elements of the
result set component.</p>

<table border="1">
<tr>
<th>Facet Name
</th>
<th>What it Does
</th>
</tr>

<tr>
<td><code>next</code>
</td>

<td>If present, this facet is output as the "Next" widget.  If absent,
the word "Next" is used.
</td>
</tr>

<tr>
<td><code>previous</code>
</td>
<td>If present, this facet is output as the "Previous" widget.  If absent,
the word "Previous" is used.
</td>
</tr>


<tr>
<td><code>number</code>
</td>

<td>If present, this facet is output, leveraging the
<code>navFacetOrientation</code> attribute, to represent "skip to page N
in the result set".
</td>

</tr>

<tr>
<td><code>current</code>
</td>

<td>If present, this facet is output, leveraging the
<code>navFacetOrientation</code> attribute, to represent the "current
page" in the result set.
</td>

</tr>
</table>

<hr>
<a href='<%= request.getContextPath() + "/index.jsp" %>'>Back</a> to home page.
</body>
</html>
</f:view>

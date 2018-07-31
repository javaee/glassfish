<%--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.

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

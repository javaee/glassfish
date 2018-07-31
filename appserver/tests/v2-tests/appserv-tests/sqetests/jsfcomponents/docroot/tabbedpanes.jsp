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

<%@ page import="java.util.Date" %>
<%@ taglib uri="http://java.sun.com/jsf/core"   prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"   prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/demo/components" prefix="d" %>

<%

    // Construct a preconfigured Date in session scope
    Date date = (Date)
      pageContext.getAttribute("date", PageContext.SESSION_SCOPE);
    if (date == null) {
      date = new Date();
      pageContext.setAttribute("date", date,
                               PageContext.SESSION_SCOPE);
    }

%>


<f:view>
<html>
<head>
  <title>Demonstration Components - Tabbed Panes</title>
</head>
<body bgcolor="white">

<h:form>
     <d:stylesheet path="/stylesheet.css"/>
Powered by Faces components:

<d:pane_tabbed id="tabcontrol"
        paneClass="tabbed-pane"
     contentClass="tabbed-content"
    selectedClass="tabbed-selected"
  unselectedClass="tabbed-unselected">

  <d:pane_tab id="first">

    <f:facet name="label">
      <d:pane_tablabel label="T a b 1" commandName="first" />
    </f:facet>

    <h:panelGroup>
      <h:outputText value="This is the first pane with the date set to: "/>
      <h:outputText value="#{sessionScope.date}">
          <f:convertDateTime dateStyle="medium"/>
      </h:outputText>
    </h:panelGroup>

  </d:pane_tab>

  <d:pane_tab id="second">

    <f:facet name="label">
      <d:pane_tablabel image="images/duke.gif" commandName="second"/>
    </f:facet>

    <h:panelGroup>
      <h:outputText value="Hi folks!  My name is 'Duke'.  Here's a sample of some of the components you can build:"/>
    </h:panelGroup>
    <h:panelGroup>
      <h:commandButton value="button"/>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
      <h:selectBooleanCheckbox value="true"/>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
      <h:selectOneRadio layout="pageDirection" border="1" value="nextMonth">
        <f:selectItem itemValue="nextDay" itemLabel="Next Day"/>
        <f:selectItem itemValue="nextWeek" itemLabel="Next Week"  />
        <f:selectItem itemValue="nextMonth" itemLabel="Next Month" />
      </h:selectOneRadio>
      <h:selectOneListbox id="appleQuantity" title="Select Quantity"
        tabindex="20" value="4" >
        <f:selectItem  itemDisabled="true" itemValue="0" itemLabel="0"/>
        <f:selectItem  itemValue="1" itemLabel="One" />
        <f:selectItem  itemValue="2" itemLabel="Two" />
        <f:selectItem  itemValue="3" itemLabel="Three" />
        <f:selectItem  itemValue="4" itemLabel="Four" />
      </h:selectOneListbox>
    </h:panelGroup>

  </d:pane_tab>

  <d:pane_tab id="third">

    <f:facet name="label">
      <d:pane_tablabel label="T a b 3" commandName="third"/>
    </f:facet>

 
    <jsp:include page="tabbedpanes3.jsp"/>

  </d:pane_tab>

</d:pane_tabbed>

<hr>
</h:form>
<a href='<%= request.getContextPath() + "/index.jsp" %>'>Back</a> to home page.

<h1>How to Use this Component</h1>

<p>This component produces a tabbed pane user interface.</p>

<h2>JSP Attributes</h2>

<p>This component allows the user to define CSS classes via JSP attributes that are output in the rendered markup.  This makes it possible to produce highly customizable output.  You can compare the rendered source of this page, using the "View Source" feature of your browser, with <a href="ShowSource.jsp?filename=/tabbedpanes.jsp">the JSP source</A> for this page.</p>

<table border="1">

<tr>
<th>JSP Attribute Name</th>
<th>What it Does</th>
</tr>

<tr>

<td><code>paneClass</code></td>

<td>A style sheet class which controls the display attributes of the outer border and tabs of the control.</td>

</tr>

<tr>

<td><code>contentClass</code></td>

<td>A style sheet class which controls the display attributes of the selected child pane contents.</td>

</tr>

<tr>

<td><code>selectedClass</code></td>

<td>A style sheet class which controls the display attributes of the select tab label.  This is used to distinguish the selected tab from the other unselected tabs.</td>

</tr>

<tr>

<td><code>unselectedClass</code></td>

<td>A style sheet class which controls the display attributes of an unselected tab label.  This is used to distinguish an unselected tab from a selected tab.</td>

</tr>

</table>

<h2>Tab Controls</h2>

<p><p>The pane control consists of multiple <code>pane_tab</code> tags, and each one corresponds to the individual tabbed panes of the control.  You can optionally indicate that a tab is initially selected with the <code>selected</code> attribute of this tag.  You must specify a unique <code>id</code> attribute for each <code>pane_tab</code> tag.

<h2>Facets</h2>

<p>Each <code>pane_tab</code> tag contains the label for the tabbed pane, as well as the content. You can define Facets for the tab labels for each of the panes.</p>

<table border="1">

<tr>
<th>Facet Name</th>
<th>What it Does</th>
</tr>

<tr>

<td><code>label</code>
</td>

<td>This should be a <code>pane_tablabel</code> tag which has either a <code>label</code> or <code>image</code> attribute and a <code>commandName</code> attribute.  This element is rendered as a button, so <code>commandName</code> is required. The <code>image</code> attribute references an image that will appear on the face of the button.  The <code>label</code> attribute is the label for the button.  This facet should be nested within a <code>pane_tab</code> tag.</td>

</tr>

</table>

<hr>

<a href='<%= request.getContextPath() + "/index.jsp" %>'>Back</a> to home page.

</body>
</html>

</f:view>

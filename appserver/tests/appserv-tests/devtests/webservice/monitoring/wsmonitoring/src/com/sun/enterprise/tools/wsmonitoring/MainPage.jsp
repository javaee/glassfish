<%--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.

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

<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <html lang="en-US" xml:lang="en-US">
            <head>
                <meta content="no-cache" http-equiv="Cache-Control"/>
                <meta content="no-cache" http-equiv="Pragma"/>
                <f:loadBundle basename="com.sun.enterprise.tools.wsmonitoring.LocalStrings" var="messages"/>                
                <link href="stylesheet.css" rel="stylesheet" type="text/css"/>
            </head>
            <body style="-rave-layout: grid">
                <h:form binding="#{Page1.form1}" id="form1">
                    <h1>
                        <h:outputText binding="#{Page1.mainTitle}" id="mainTitle" style="height: 28px; left: 208px; top: 0px; position: absolute; width: 408px" value="#{messages.mainpage_pageHeader}"/>
                    </h1>
                    <h:outputText binding="#{Page1.chooseTitle}" id="chooseTitle" style="height: 24px; left: 24px; top: 48px; position: absolute; width: 314px" value="#{messages.mainpage_chooseEndpoint}"/>
                    <h:outputText binding="#{Page1.selectedEndpoint}" id="selectedEndpoint"
                        style="height: 26px; left: 24px; top: 240px; position: absolute; width: 600px" value="#{SessionBean1.endpointInfo}"/>
                    <h:outputLink binding="#{Page1.hyperlink1}" id="hyperlink1" style="height: 26px; left: 144px; top: 216px; position: absolute; width: 48px" value="#{SessionBean1.wsdlURL}">
                        <h:outputText binding="#{Page1.wsdlLink}" id="wsdlLink" value="#{messages.mainpage_wsdlLink}"/>
                    </h:outputLink>
                    <h:outputText binding="#{Page1.wsdlTitle}" id="wsdlTitle" style="height: 26px; left: 24px; top: 216px; position: absolute; width: 120px" value="#{messages.mainpage_wsdlTitle}"/>
                    <h:commandButton actionListener="#{Page1.refreshButton_processAction}" binding="#{Page1.refreshButton}" id="refreshButton"
                        style="height: 24px; left: 624px; top: 168px; position: absolute; width: 240px" value="#{messages.mainpage_refreshMessageList}"/>
                    <h:selectOneListbox binding="#{Page1.messageList}" id="messageList" immediate="true" onchange="this.form.submit();" size="5"
                        style="height: 96px; left: 624px; top: 72px; position: absolute; width: 240px" valueChangeListener="#{Page1.messageList_processValueChange}">
                        <f:selectItems binding="#{Page1.messageListItems}" id="messageListItems" value="#{SessionBean1.messageListItems}"/>
                    </h:selectOneListbox>
                    <h:outputText binding="#{Page1.soapRequest}" id="soapRequest" style="height: 198px; left: 24px; top: 360px; position: absolute; width: 888px"/>
                    <h:outputText binding="#{Page1.soapResponse}" id="soapResponse" style="height: 198px; left: 24px; top: 576px; position: absolute; width: 888px"/>
                    <h:outputText binding="#{Page1.processingTime}" id="processingTime" style="height: 26px; left: 24px; top: 264px; position: absolute; width: 264px"/>
                    <h:selectBooleanCheckbox binding="#{Page1.displayEnvCheckbox}" id="displayEnvCheckbox" immediate="true" onchange="this.form.submit();"
                        onclick="this.form.submit();" style="left: 24px; top: 312px; position: absolute" valueChangeListener="#{Page1.displayEnvCheckbox_processValueChange}"/>
                    <h:outputText binding="#{Page1.displayEnvText}" id="displayEnvText"
                        style="height: 21px; left: 48px; top: 312px; position: absolute; width: 240px" value="#{messages.mainpage_displayEnv}"/>
                    <h:selectOneListbox binding="#{Page1.endpointsList}" id="endpointsList" immediate="true" onchange="this.form.submit();" size="5"
                        style="height: 96px; left: 24px; top: 72px; position: absolute; width: 576px" valueChangeListener="#{Page1.endpointsList_processValueChange}">
                        <f:selectItems binding="#{Page1.listbox1SelectItems}" id="listbox1SelectItems" value="#{SessionBean1.endpointsListItems}"/>
                    </h:selectOneListbox>
                    <h:commandButton actionListener="#{Page1.refreshEndpointList_processAction}" binding="#{Page1.refreshEndpointList}" id="refreshEndpointList"
                        style="height: 24px; left: 24px; top: 168px; position: absolute; width: 576px" value="#{messages.mainpage_refreshEndpointList}"/>
                </h:form>
            </body>
        </html>
    </f:view>
</jsp:root>

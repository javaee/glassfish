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

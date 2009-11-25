<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 2 only ("GPL") or the Common Development
 and Distribution License("CDDL") (collectively, the "License").  You
 may not use this file except in compliance with the License. You can obtain
 a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 language governing permissions and limitations under the License.

 When distributing the software, include this License Header Notice in each
 file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 Sun designates this particular file as subject to the "Classpath" exception
 as provided by Sun in the GPL Version 2 section of the License file that
 accompanied this code.  If applicable, add the following below the License
 Header, with the fields enclosed by brackets [] replaced by your own
 identifying information: "Portions Copyrighted [year]
 [name of copyright owner]"

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
-->

<!-- Page Title -->
<sun:title id="propertyContentPage" title="#{pageTitle}" helpText="#{pageTitleHelp}">
    <sun:button id="def" rendered="#{edit}" primary="#{false}"  text="$resource{i18n.button.LoadDefaults}" >
        <!command
            getDefaultProxyAttrs( parentObjectNameStr="#{pageSession.parentObjectNameStr}" childType="#{pageSession.childType}"  orig="#{pageSession.valueMap}" valueMap="#{pageSession.valueMap}")
        />
    </sun:button>
    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}') && checkPSW()) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    saveBeanAttributes(objectNameStr="#{pageSession.objectNameStr}", attrs="#{pageSession.valueMap}");
                    prepareSuccessfulMsg();
                    gf.redirect(page="jmsHosts.jsf?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}&configName=#{configName}")
                />
            </sun:button>

            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}" 
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}') && checkPSW()) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    createProxy(parentObjectNameStr="#{pageSession.parentObjectNameStr}" childType="#{pageSession.childType}"
                        attrs="#{pageSession.valueMap}", convertToFalse="#{pageSession.convertToFalseList}");
                    prepareSuccessfulMsg();
                    gf.redirect(page="jmsHosts.jsf?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}&configName=#{configName}")
                    />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}">
                <!command
                    gf.redirect(page="jmsHosts.jsf?configName=#{configName}");
                    />
            </sun:button>

        </sun:panelGroup>
    </facet>
</sun:title>
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">			 
    <sun:propertySheetSection id="propertSectionTextField">
        <sun:property id="JmsHostStaticProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.edJmsHost.jmsHostNameLabel}" >
            <sun:staticText escape="true" id="JmsHostStatic" text="#{pageSession.valueMap['Name']}"/>
        </sun:property>

        <sun:property id="JmsHostTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.edJmsHost.jmsHostNameLabel}">
            <sun:textField id="JmsHostText" text="#{pageSession.valueMap['Name']}" columns="$int{55}" styleClass="required" required="#{true}" />
        </sun:property>

        <sun:property id="HostProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.edJmsHost.hostLabel}" helpText="$resource{i18njms.edJmsHost.hostHelp}" >
            <sun:textField id="Host" columns="$int{55}"  text="#{pageSession.valueMap['Host']}" />
        </sun:property>
        <sun:property id="PortProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.edJmsHost.portLabel}" helpText="$resource{i18njms.edJmsHost.portHelp}" >
            <sun:textField id="Port" columns="$int{25}" styleClass="port"text="#{pageSession.valueMap['Port']}" />
        </sun:property>
        <sun:property id="AdminUserProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.edJmsHost.adminUserLabel}" helpText="$resource{i18njms.edJmsHost.adminUserHelp}" >
            <sun:textField id="AdminUser" columns="$int{25}" text="#{pageSession.valueMap['AdminUserName']}" styleClass="required"  required="#{true}" />
        </sun:property>
        <sun:property id="newPasswordProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njms.edJmsHost.adminPwdLabel}" helpText="$resource{i18njms.edJmsHost.adminPwdHelp}">
        <sun:passwordField id="NewPassword" columns="$int{25}" text="#{pageSession.valueMap['AdminPassword']}" styleClass="required"  required="#{true}">
             <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{ps1});
            />
        </sun:passwordField>
        </sun:property>
        <sun:property id="confirmPasswordProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.server.ConfirmNewPassword}">
            <sun:passwordField id="ConfirmPassword"  columns="$int{25}" text="#{pageSession.valueMap['AdminPassword']}" styleClass="required" required="#{true}"  >
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{ps2});
                />
            </sun:passwordField>
        </sun:property>

        "<br /><br />
    </sun:propertySheetSection>
</sun:propertySheet>    


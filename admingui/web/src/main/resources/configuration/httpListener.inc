<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 
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
<!-- configuration/httpListener.inc -->    
<!-- Page Title -->
<sun:title id="propertyContentPage" title="#{pageTitle}" helpText="#{pageTitleHelp}">
    <!facet pageButtonsTop>
    <sun:panelGroup id="topButtons">
        <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}" 
        onClick="return guiValidateWithDropDown('#{reqMsg}','#{reqInt}' ,'#{reqPort}', '#{reqMsgSelect}');" >   
            <!command
            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
            convertRowsToProperties(NewList="#{newList}", AddProps=>$attribute{newProps});
            prepareSuccessfulMsg();   
            saveHttpListenerValues(
                ConfigName="#{configName}"
                HttpName="#{tabName}"  
                Edit="#{edit}"
                NetwkAddr="#{netwkAddr}" 
                ListenerPort="#{listenerPort}"
                DefaultVirtServer="#{defaultVirtServer}"
                ServerName="#{requestScope.serverName}"
                Listener="#{listener}"
                security="#{security}"
                RedirectPort="#{redirectPort}"
                Acceptor="#{acceptor}"
                PoweredBy="#{poweredBy}"
                Blocking="#{blocking}"
                newProps="#{newProps}"
            );
           redirect(page="#{editPage}?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}&configName=#{configName}&name=#{tabName}");
            />
        </sun:button>
        <sun:button id="nextButton" rendered="#{!edit}" text="$resource{i18n.button.Next}" 
                onClick="return guiValidateWithDropDown('#{reqMsg}','#{reqInt}' ,'#{reqPort}', '#{reqMsgSelect}');" > 
            <!command
            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
            convertRowsToProperties(NewList="#{newList}", AddProps=>$attribute{newProps});
            saveHttpListenerValues(
                ConfigName="#{configName}"
                HttpName="#{tabName}"  
                Edit="#{edit}"
                NetwkAddr="#{netwkAddr}" 
                ListenerPort="#{listenerPort}"
                DefaultVirtServer="#{defaultVirtServer}"
                ServerName="#{requestScope.serverName}"
                Listener="#{listener}"
                security="#{security}"
                RedirectPort="#{redirectPort}"
                Acceptor="#{acceptor}"
                PoweredBy="#{poweredBy}"
                Blocking="#{blocking}"
                newProps="#{newProps}"
            );
<!--
            setAttribute(key="configName", value="#{configName}");
            setAttribute(key="tabName", value="#{tabName}");
            setAttribute(key="type", value="http");
            setAttribute(key="sslPageTitle", value="#{step2PageTitle}");
            setAttribute(key="prevPage", value="#{request.contextPath}/web/configuration/httpListenerNew.jsf");
            setAttribute(key="finalPage", value="#{request.contextPath}/web/configuration/httpListeners.jsf");
            setAttribute(key="ssl2", value="#{true}");
-->
            redirect(page="#{request.contextPath}/web/configuration/listenersSslNew.jsf?configName=#{configName}&tabName=#{tabName}&type=http&sslPageTitle=#{step2PageTitle}&prevPage=/web/configuration/httpListenerNew.jsf&finalPage=httpListeners.jsf&ssl2=true");
            />
        </sun:button>
        <sun:button id="cancelButton"  immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}">
            <!command
            setSessionAttribute(key="httpProps" value="#{null}")
            setSessionAttribute(key="sslProps" value="#{null}")
            redirect(page="#{request.contextPath}/web/configuration/httpListeners.jsf?configName=#{configName}");
            />
        </sun:button>
        
    </sun:panelGroup>
    </facet>   
</sun:title>
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet" requiredFields="true">			 
        <event>
            <!beforeCreate
            getDefaultVirtualServers(ConfigName="#{configName}", DefaultVirtualServers=>$attribute{defaultVirtServers}); 
            getHttpListenerValues(Edit="#{edit}", FromStep2="#{fromStep2}", ConfigName="#{configName}", 
            HttpName="#{tabName}", Listener=>$attribute{listener}, security=>$attribute{security}, 
            NetwkAddr=>$attribute{netwkAddr}, ListenerPort=>$attribute{listenerPort}, 
            DefaultVirtServer=>$attribute{defaultVirtServer}, ServerName=>$attribute{serverName}
            RedirectPort=>$attribute{redirectPort}, Acceptor=>$attribute{acceptor}, 
            PoweredBy=>$attribute{poweredBy}, Blocking=>$attribute{blocking}, Properties=>$page{props});
            getTableList(Properties="#{props}", TableList=>$attribute{tableList});
            />
        </event>

        <!-- General Settings section -->   
        <sun:propertySheetSection id="generalSettingsSetion" label="$resource{i18n.common.GeneralSettings}">
            <sun:property id="HttpNameProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" >
                <sun:staticText id="HttpNameName" text="#{tabName}" /> 
            </sun:property> 
            <sun:property id="HttpNameTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}">
                <sun:textField id="HttpNameText" styleClass="required" text="#{tabName}" columns="$int{55}" required="#{true}" /> 
            </sun:property> 
            <sun:property id="ListenerProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.iiopListener.listenerLabel}" >
                <sun:checkbox label="$resource{i18n.desc.Enabled}" selected="#{requestScope.listener}" />   
            </sun:property>

            <sun:property id="SecurityProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edJmxConnector.securityLabel}" >
                <sun:checkbox id="Security" label="$resource{i18n.desc.Enabled}" selected="#{requestScope.security}" />
            </sun:property>

            <sun:property id="NetwkAddrProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.iiopListener.netwkAddrLabel}" helpText="$resource{i18n.httpListeners.netwkAddrHelp}" >
                <sun:textField id="NetwkAddr" styleClass="require" columns="$int{20}" text="#{requestScope.netwkAddr}" required="#{true}" />      
            </sun:property> 
            <sun:property id="ListenerPortProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.iiopListener.listPortLabel}" helpText="$resource{i18n.httpListeners.listPortHelp}" >
                <sun:textField id="ListenerPort" styleClass="require port" columns="$int{20}" text="#{requestScope.listenerPort}" required="#{true}" />      
            </sun:property> 
            <sun:property id="DefaultVirtServersProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.httpListeners.defVirtualServerLabel}" helpText="$resource{i18n.httpListeners.defVirtualServerHelp}">
                <sun:dropDown id="DefaultVirtServers" selected="#{requestScope.defaultVirtServer}" values="$attribute{defaultVirtServers}"  labels="$attribute{defaultVirtServers}"  required="#{true}"/>
            </sun:property>                          
            <sun:property id="ServerNameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.httpListeners.serverNameLabel}" helpText="$resource{i18n.httpListeners.serverNameHelp}" >
                <sun:textField id="ServerName" columns="$int{20}" text="#{requestScope.serverName}" />      
            </sun:property> 
        </sun:propertySheetSection>                            
        <!-- Advanced Settings section -->   
        <sun:propertySheetSection id="advancedSettingsSetion" label="$resource{i18n.common.Advanced}">
            <sun:property id="RedirectPortProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.httpListeners.redirectPortLabel}" helpText="$resource{i18n.httpListeners.redirectPortHelp}" >
                <sun:textField id="RedirectPort" styleClass="port" columns="$int{20}" text="#{requestScope.redirectPort}" /> 
            </sun:property> 
            <sun:property id="AcceptorProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.httpListeners.acceptorLabel}" helpText="$resource{i18n.httpListeners.acceptorHelp}" >
                <sun:textField id="Acceptor" styleClass="integer" columns="$int{20}" text="#{requestScope.acceptor}" />      
            </sun:property> 
            <sun:property id="PoweredByProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.httpListeners.poweredByLabel}" helpText="$resource{i18n.httpListeners.poweredByHelp}" >
                <sun:checkbox label="$resource{i18n.desc.Enabled}" selected="#{requestScope.poweredBy}" />   
            </sun:property>
            <sun:property id="BlockingProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.httpListeners.blockingLabel}" helpText="$resource{i18n.httpListeners.blockingHelp}" >
                <sun:checkbox label="$resource{i18n.desc.Enabled}" selected="#{requestScope.blocking}" />   
            </sun:property>
        </sun:propertySheetSection>                            
</sun:propertySheet>    


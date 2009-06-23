<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 
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
<!-- configuration/iiopListener.inc -->    
<!-- Page Title -->
<sun:title id="propertyContentPage" title="#{pageTitle}" helpText="#{pageTitleHelp}">
    <sun:button id="loadDefaults" rendered="#{edit}" style="margin-left: 8pt" primary="#{false}" text="$resource{i18n.button.LoadDefaults}" >    
        <!command
            getDefaultProxyAttrs( parentObjectNameStr="#{pageSession.parentObjectNameStr}" childType="#{pageSession.childType}"  valueMap="#{pageSession.valueMap}")
        />
    </sun:button>

    <!facet pageButtonsTop>
    <sun:panelGroup id="topButtons">
        <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}" 
                onClick="return guiValidate('#{reqMsg}','#{reqInt}', '#{reqPort}'); " >  
            <!command
            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
            convertListToArrayList(TableList="#{newList}", NameList=>$attribute{nameList});
            getAddRemoveProps(NewList="#{newList}", OldList="#{props}", NameList="#{nameList}", AddProps=>$attribute{addProps}, RemoveProps=>$attribute{removeProps});
            prepareSuccessfulMsg();
            saveIiopListenerSettings(
            ConfigName="#{configName}"
            IiopName="#{tabName}"  
            Edit="#{edit}"
            NetwkAddr="#{netwkAddr}" 
            ListenerPort="#{listenerPort}"
            Listener="#{listener}"
            security="#{security}"
            AddProps="#{addProps}"
            RemoveProps="#{removeProps}"
            );  
            />    
        </sun:button>

    <sun:button id="nextButton" rendered="#{!edit}" text="$resource{i18n.button.Next}" 
                onClick="return guiValidate('#{reqMsg}','#{reqInt}', '#{reqPort}'); " > 
        <!command
            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
            convertRowsToProperties(NewList="#{newList}", AddProps=>$attribute{addProps});
            saveIiopListenerSettings(
            ConfigName="#{configName}"
            IiopName="#{tabName}"  
            Edit="#{edit}"
            NetwkAddr="#{netwkAddr}" 
            ListenerPort="#{listenerPort}"
            Listener="#{listener}"
            security="#{security}"
            AddProps="#{addProps}"
            RemoveProps="#{removeProps}"
            );           
            setAttribute(key="configName", value="#{configName}");
            setAttribute(key="tabName", value="#{tabName}");
            setAttribute(key="type", value="iiop");
            setAttribute(key="sslPageTitle", value="#{step2PageTitle}");
            setAttribute(key="prevPage", value="configuration/iiopListenerNew.jsf");
            setAttribute(key="finalPage", value="iiopListeners.jsf");
            setAttribute(key="ssl2", value="#{false}");
            navigate(page="configuration/listenersSslNew.jsf");
        />
    </sun:button>
    <sun:button id="cancelButton" rendered="#{!edit}" immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}">
        <!command
            setSessionAttribute(key="iiopProps" value="#{null}")
            setSessionAttribute(key="sslProps" value="#{null}")
            redirect(page="iiopListeners.jsf?configName=#{configName}");
        />
  </sun:button>

</sun:panelGroup>
</facet>
</sun:title>
  
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">			 
        <event>
            <!beforeCreate
		getIiopListenerSettings(Edit="#{edit}", FromStep2="#{fromStep2}", ConfigName="#{configName}", IiopName="#{tabName}",  
		    NetwkAddr=>$attribute{netwkAddr}, ListenerPort=>$attribute{listenerPort}, 
		    Listener=>$attribute{listener}, security=>$attribute{security},
		    Properties=>$page{props}); 
		    getTableList(Properties="#{props}", TableList=>$attribute{tableList});
		if ("!#{requestScope.security}") {
		    setAttribute(key="security", value="$boolean{false}");
		}
            />
        </event>

        <!-- General Settings section -->   
        <sun:propertySheetSection id="generalSettingsSetion" label="$resource{i18n.common.GeneralSettings}">
            <sun:property id="IiopNameProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" >
                <sun:staticText id="IiopName" text="#{tabName}" /> 
            </sun:property> 
            <sun:property id="IiopNameTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}">
                <sun:textField id="IiopNameText" styleClass="required" text="#{tabName}" columns="$int{55}" required="#{true}" /> 
            </sun:property> 
            <sun:property id="NetwkAddrProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.iiopListener.netwkAddrLabel}" helpText="$resource{i18n.iiopListener.netwkAddrHelp}" >
                <sun:textField id="NetwkAddr" styleClass="required"  columns="$int{20}" text="#{requestScope.netwkAddr}" required="#{true}" />      
            </sun:property> 
            <sun:property id="ListenerPortProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.iiopListener.listPortLabel}" helpText="$resource{i18n.iiopListener.listPortHelp}" >
                <sun:textField id="ListenerPort" styleClass="required port" columns="$int{20}" text="#{requestScope.listenerPort}" required="#{true}" />      
            </sun:property> 
            <sun:property id="ListenerProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.iiopListener.listenerLabel}" helpText="$resource{i18n.iiopListener.listenerHelp}" >
                <sun:checkbox label="$resource{i18n.desc.Enabled}" selected="#{requestScope.listener}" />   
            </sun:property>
            <sun:property id="SecurityProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edJmxConnector.securityLabel}" >
                <sun:checkbox id="Security" label="$resource{i18n.desc.Enabled}" selected="#{requestScope.security}" />
            </sun:property>
        </sun:propertySheetSection>                            
</sun:propertySheet>    


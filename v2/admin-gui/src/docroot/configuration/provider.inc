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
<!-- configuration/provider.inc -->    
<!-- Page Title -->
<sun:title id="propertyContentPage" title="#{pageTitle}" helpText="#{pageTitleHelp}">
    <!facet pageButtonsTop>
    <sun:panelGroup id="topButtons">
        <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " > 
            <!command
            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
            convertListToArrayList(TableList="#{newList}", NameList=>$attribute{nameList});
            getAddRemoveProps(NewList="#{newList}", OldList="#{props}", NameList="#{nameList}", AddProps=>$attribute{addProps}, RemoveProps=>$attribute{removeProps});
            prepareSuccessfulMsg();
            saveProviderValues(
            Edit="#{edit}"
            ConfigName="#{configName}"
            MsgSecurityName="#{msgSecurityName}"
            ProviderId="#{providerId}"
            DefaultProvider="#{defaultProvider}"
            ProviderType="#{providerType}"  
            ClassName="#{className}"
            AuthSourceRequest="#{authSourceRequest}" 
            AuthRecpRequest="#{authRecpRequest}"
            AuthSourceResponse="#{authSourceResponse}"
            AuthRecpResponse="#{authRecpResponse}"
            AddProps="#{addProps}"
            RemoveProps="#{removeProps}"
            );  
            />    
        </sun:button>
    
        <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}" 
                onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " > 
            <!command
            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
            convertRowsToProperties(NewList="#{newList}", AddProps=>$attribute{addProps});
            saveProviderValues(
            Edit="#{edit}"
            ConfigName="#{configName}"
            MsgSecurityName="#{msgSecurityName}"
            ProviderId="#{providerId}"
            DefaultProvider="#{defaultProvider}"
            ProviderType="#{providerType}"  
            ClassName="#{className}"
            AuthSourceRequest="#{authSourceRequest}" 
            AuthRecpRequest="#{authRecpRequest}"
            AuthSourceResponse="#{authSourceResponse}"
            AuthRecpResponse="#{authRecpResponse}"
            AddProps="#{addProps}"
            RemoveProps="#{removeProps}"
            );             
            setAttribute(key="configName", value="#{configName}");
            setAttribute(key="msgSecurityName", value="#{msgSecurityName}");
            redirect(page="providers.jsf?configName=#{configName}&msgSecurityName=#{msgSecurityName}"); 
            />
        </sun:button>
    
        <sun:button id="cancelButton" rendered="#{!edit}" immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}">
            <!command
                redirect(page="providers.jsf?configName=#{configName}&msgSecurityName=#{msgSecurityName}");
            />
        </sun:button>
        
    </sun:panelGroup>
    </facet>
    <!facet pageButtonsBottom>
    <sun:panelGroup id="bottomButtons">
        <sun:button id="saveButton2" rendered="#{edit}" text="$resource{i18n.button.Save}"
                onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " > 
            <!command
            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
            convertListToArrayList(TableList="#{newList}", NameList=>$attribute{nameList});
            getAddRemoveProps(NewList="#{newList}", OldList="#{props}", NameList="#{nameList}", AddProps=>$attribute{addProps}, RemoveProps=>$attribute{removeProps});
            prepareSuccessfulMsg();
            saveProviderValues(
            Edit="#{edit}"
            ConfigName="#{configName}"
            MsgSecurityName="#{msgSecurityName}"
            ProviderId="#{providerId}"
            DefaultProvider="#{defaultProvider}"
            ProviderType="#{providerType}"  
            ClassName="#{className}"
            AuthSourceRequest="#{authSourceRequest}" 
            AuthRecpRequest="#{authRecpRequest}"
            AuthSourceResponse="#{authSourceResponse}"
            AuthRecpResponse="#{authRecpResponse}"
            AddProps="#{addProps}"
            RemoveProps="#{removeProps}"
            );  
            />    
        </sun:button>
    
        <sun:button id="newButton2" rendered="#{!edit}" text="$resource{i18n.button.OK}" 
                onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " > 
            <!command
            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
            convertRowsToProperties(NewList="#{newList}", AddProps=>$attribute{addProps});
            saveProviderValues(
            Edit="#{edit}"
            ConfigName="#{configName}"
            MsgSecurityName="#{msgSecurityName}"
            ProviderId="#{providerId}"
            DefaultProvider="#{defaultProvider}"
            ProviderType="#{providerType}"  
            ClassName="#{className}"
            AuthSourceRequest="#{authSourceRequest}" 
            AuthRecpRequest="#{authRecpRequest}"
            AuthSourceResponse="#{authSourceResponse}"
            AuthRecpResponse="#{authRecpResponse}"
            AddProps="#{addProps}"
            RemoveProps="#{removeProps}"
            );
            redirect(page="providers.jsf?configName=#{configName}&msgSecurityName=#{msgSecurityName}"); 
            />
        </sun:button>
    
        <sun:button id="cancelButton2" rendered="#{!edit}" immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}">
            <!command
                redirect(page="providers.jsf?configName=#{configName}&msgSecurityName=#{msgSecurityName}");
            />
        </sun:button>
        
    </sun:panelGroup>
    </facet>   
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">			 
    <event>
        <!beforeCreate
            getListsforProvider(ProviderTypes=>$attribute{providerTypes}, 
            AuthSources=>$attribute{authSources}, AuthRecepients=>$attribute{authRecepients});
            getProviderValues(Edit="#{edit}", ConfigName="#{configName}", 
            MsgSecurityName="#{msgSecurityName}", ProviderId="#{providerId}"
            DefaultProvider=>$attribute{defaultProvider}, ProviderType=>$attribute{providerType}, 
            ClassName=>$attribute{className}, AuthSourceRequest=>$attribute{authSourceRequest}, 
            AuthRecpRequest=>$attribute{authRecpRequest}, AuthSourceResponse=>$attribute{authSourceResponse}, 
            AuthRecpResponse=>$attribute{authRecpResponse}, Properties=>$page{props} ); 
            getTableList(Properties="#{props}", TableList=>$attribute{tableList});
        />
    </event>
#include "configuration/providerConfig.inc"                           
    <!-- Request Policy section -->               
    <sun:propertySheetSection id="requestPolicySection" label="$resource{i18n.msgSecProvider.requestPolicyLabel}">
        <sun:property id="AuthSourceProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.msgSecProvider.authSourceLabel}" helpText="$resource{i18n.msgSecProvider.authSourceRequestHelp}">
            <sun:dropDown id="AuthSource" selected="#{requestScope.authSourceRequest}" items="$attribute{authSources}" />
        </sun:property>                          
        <sun:property id="AuthRecipientProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.msgSecProvider.authRecepientLabel}" helpText="$resource{i18n.msgSecProvider.authRecepientHelp}">
            <sun:dropDown id="AuthRecipient" selected="#{requestScope.authRecpRequest}" items="$attribute{authRecepients}" />
        </sun:property>                          
    </sun:propertySheetSection>                            
    <!-- Response Policy section -->               
    <sun:propertySheetSection id="responsePolicySection" label="$resource{i18n.msgSecProvider.responsePolicyLabel}">
        <sun:property id="AuthSourceProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.msgSecProvider.authSourceLabel}" helpText="$resource{i18n.msgSecProvider.authSourceResponseHelp}">
            <sun:dropDown id="AuthSource" selected="#{requestScope.authSourceResponse}" items="$attribute{authSources}" />
        </sun:property>                          
        <sun:property id="AuthRecipientProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.msgSecProvider.authRecepientLabel}" helpText="$resource{i18n.msgSecProvider.authRecepientHelp}">
            <sun:dropDown id="AuthRecipient" selected="#{requestScope.authRecpResponse}" items="$attribute{authRecepients}" />
        </sun:property>                          
    </sun:propertySheetSection>                            
</sun:propertySheet>    


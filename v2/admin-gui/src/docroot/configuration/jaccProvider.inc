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
            saveJaccProviderSettings(
            ConfigName="#{configName}",
            Name="#{name}",
            Edit="#{edit}",                        
            AddProps="${addProps}",,
            RemoveProps="${removeProps}",
            PolicyConfig="#{config}",
            PolicyProvider="#{policy}"
            );            
            />    
        </sun:button>

    <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}" 
                onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " >   
        <!command
            getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
            convertRowsToProperties(NewList="#{newList}", AddProps=>$attribute{addProps});
            saveJaccProviderSettings(
            ConfigName="#{configName}",
            Name="#{name}",
            Edit="#{edit}",                        
            AddProps="${addProps}",
            PolicyConfig="#{config}",
            PolicyProvider="#{policy}"
            );            
            redirect(page="jaccProviders.jsf?configName=#{configName}")
        />
    </sun:button>
    <sun:button id="cancelButton" rendered="#{!edit}" immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}">
        <!command
            redirect(page="jaccProviders.jsf?configName=#{configName}");
        />
  </sun:button>

</sun:panelGroup>
</facet>   
</sun:title>
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">			 
        <event>
        <!beforeCreate
        getJaccProviderSettings(
        Edit="#{edit}",
        ConfigName="#{configName}", 
        Name="#{name}",
        PolicyConfig=>$attribute{config},
        Properties=>$page{props}, 
        PolicyProvider=>$attribute{policy});   
       getTableList(Properties="#{props}", TableList=>$attribute{tableList});      
        />
        </event>

        <sun:propertySheetSection id="propertSectionTextField">
            <sun:property id="IdStaticProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" >
                <sun:staticText id="IdStatic" text="#{name}"/> 
            </sun:property> 

            <sun:property id="IdTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" helpText="$resource{i18n.jacc.NameHelp}">
                <sun:textField id="IdText" text="#{name}" columns="$int{55}" styleClass="required" required="#{true}" /> 
            </sun:property> 

            <sun:property id="policyConfigProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jacc.PolicyConfig}" helpText="$resource{i18n.jacc.PolicyConfigHelp}">
                <sun:textField id="PolicyConfig" columns="$int{75}" text="#{config}" styleClass="required" required="#{true}"/> 
            </sun:property>      
            <sun:property id="policyProviderProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jacc.PolicyProvider}" helpText="$resource{i18n.jacc.PolicyProviderHelp}">
                <sun:textField id="PolicyProvider" columns="$int{75}" text="#{policy}" styleClass="required" required="#{true}"/> 
            </sun:property>      
        </sun:propertySheetSection>
</sun:propertySheet>    


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
<!-- resourceNode/jndiResource.inc -->


<!-- Page Title -->
<sun:title id="propertyContentPage" title="#{pageTitle} "helpText="#{pageTitleHelp}">
<!-- Buttons  -->                    
<!facet pageButtonsTop>
<sun:panelGroup id="topButtons">
    <sun:button id="saveButton" rendered="#{edit}" text="#{saveButtonLabel}" 
        onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " > 
       <!command
         getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
         getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
         convertListToArrayList(TableList="#{newList}", NameList=>$attribute{nameList});
         getAddRemoveProps(NewList="#{newList}", OldList="#{props}", NameList="#{nameList}", AddProps=>$attribute{addProps}, RemoveProps=>$attribute{removeProps});
         setPageSessionAttribute(key="props", value="${addProps}");               
         prepareSuccessfulMsg();
         saveJndiResource(
            type="#{jndiType}"
            jndiName="#{jndiName}"
            edit="#{edit}"
            resType="#{resType}"
            factoryClass="#{factoryClass}"
            jndiLookupName="#{jndiLookupName}"
            description="#{description}"
            enabled="#{enabled}"
            AddProps="${addProps}",
            RemoveProps="${removeProps}"
        );
        />    
    </sun:button>
    <sun:button id="newButton"  rendered="#{!edit}" text="#{saveButtonLabel}" 
            onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " > 
       <!command
        getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
        getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
        convertRowsToProperties(NewList="#{newList}", AddProps=>$attribute{addProps});
        saveJndiResource(
            type="#{jndiType}"
            jndiName="#{jndiName}"
            edit="#{edit}"
            resType="#{resType}"
            factoryClass="#{factoryClass}"
            jndiLookupName="#{jndiLookupName}"
            description="#{description}"
            enabled="#{enabled}"
            AddProps="${addProps}",
            targets="#{selectedTargets}"
        );
        redirect(page="#{cancelPage}");
        />    
    </sun:button>
    <sun:button id="cancelButton" rendered="#{!edit}" immediate="#{true}" primary="#{false}" text="$resource{i18n.button.Cancel}" >
    <!command
        redirect(page="#{cancelPage}");
    />
  </sun:button>
</sun:panelGroup>
</facet>
<!facet pageButtonsBottom>
<sun:panelGroup id="bottomButtons">
    <sun:button id="saveButton2" rendered="#{edit}" text="#{saveButtonLabel}" 
        onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " > 
       <!command
         getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
         getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
         convertListToArrayList(TableList="#{newList}", NameList=>$attribute{nameList});
         getAddRemoveProps(NewList="#{newList}", OldList="#{props}", NameList="#{nameList}", AddProps=>$attribute{addProps}, RemoveProps=>$attribute{removeProps});
         setPageSessionAttribute(key="props", value="${addProps}");               
         prepareSuccessfulMsg();
         saveJndiResource(
            type="#{jndiType}"
            jndiName="#{jndiName}"
            edit="#{edit}"
            resType="#{resType}"
            factoryClass="#{factoryClass}"
            jndiLookupName="#{jndiLookupName}"
            description="#{description}"
            enabled="#{enabled}"
            AddProps="${addProps}",
            RemoveProps="${removeProps}"
        );
        />    
    </sun:button>
    <sun:button id="newButton2"  rendered="#{!edit}" text="#{saveButtonLabel}" 
            onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " > 
       <!command
        getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
        getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
        convertRowsToProperties(NewList="#{newList}", AddProps=>$attribute{addProps});
        saveJndiResource(
            type="#{jndiType}"
            jndiName="#{jndiName}"
            edit="#{edit}"
            resType="#{resType}"
            factoryClass="#{factoryClass}"
            jndiLookupName="#{jndiLookupName}"
            description="#{description}"
            enabled="#{enabled}"
            AddProps="${addProps}",
            targets="#{selectedTargets}"
        );
        redirect(page="#{cancelPage}");
        />    
    </sun:button>
    <sun:button id="cancelButton2" rendered="#{!edit}" immediate="#{true}" primary="#{false}" text="$resource{i18n.button.Cancel}" >
    <!command
        redirect(page="#{cancelPage}");
    />
  </sun:button>
</sun:panelGroup>
</facet> 


<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">			 
    <sun:propertySheetSection id="propertSectionTextField">
        <event>
        <!beforeCreate
            getRequestValue(key="name" value=>$page{jndiName});
            getJndiResourceInfo(jndiName="#{jndiName}"
                    type="#{jndiType}"
                    edit="#{edit}"
                    resType=>$attribute{resType}
                    factoryClass=>$attribute{factoryClass}
                    jndiLookupName=>$attribute{jndiLookupName}
                    description=>$attribute{description}
                    enabled=>$attribute{enabled} enabledString=>$attribute{enabledString}
                    Properties=>$page{props});
            getTableList(Properties="#{props}", TableList=>$attribute{tableList});
        />
        </event>
        <sun:property id="jndiProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}">
            <sun:staticText id="Jndi" rendered="#{edit}" text="#{jndiName}"/>
            <sun:textField id="JndiNew" styleClass="required" rendered="#{!edit}" columns="$int{30}" text="#{jndiName}" required="#{true}" />
        </sun:property>   

        <sun:property id="resTypeProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resType}" helpText="$resource{i18n.jndiResource.resTypeHelp}">
            <sun:textField id="resType" styleClass="required"  columns="$int{55}" text="#{requestScope.resType}" required="#{true}" />
        </sun:property>

        <sun:property id="factoryClassProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jndiResource.factoryClass}" helpText="$resource{i18n.jndiResource.factoryClassHelp}">
            <sun:textField id="factoryClass" styleClass="required" columns="$int{55}" text="#{requestScope.factoryClass}" required="#{true}" /> 
        </sun:property>

        <sun:property id="jndiLookupProp"  rendered="#{showJndiLookup}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jndiResource.jndiLookup}" helpText="$resource{i18n.jndiResource.jndiLookupHelp}" >
            <sun:textField id="jndiLookup" styleClass="required" columns="$int{55}" text="#{jndiLookupName}" required="#{true}" />      
        </sun:property>

        <sun:property id="descProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}" >helpText="$resource{i18n.jdbc.descHelp}">
            <sun:textField id="desc" columns="$int{55}" text="#{requestScope.description}" />      
        </sun:property>

        <sun:property id="statusProp" rendered="#{useCheckBox}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
            <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{requestScope.enabled}" />  
       </sun:property>

       <sun:property id="statusProp2" rendered="#{useString}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" 
            label="$resource{i18n.common.status}" helpText="$resource{i18n.application.EnableTargetHelp}">
            <sun:staticText  text="#{requestScope.enabledString}" />  
       </sun:property> 
        "<br /><br />
    </sun:propertySheetSection>
</sun:propertySheet>  
#include "propertyTable.inc"  


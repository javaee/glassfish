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
<!-- resourceNode/adminObjectEdit.inc -->

<!-- Page Title -->
<sun:title id="propertyContentPage" title="#{pageTitle}" 
    helpText="#{pageTitleHelp}">
    <!-- Buttons  -->                    
    <!facet pageButtonsTop>
    <sun:panelGroup id="topButtons">
        <sun:button id="saveButton" text="$resource{i18n.button.Save}" 
                onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " > 
           <!command
             getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
             getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
             convertListToArrayList(TableList="#{newList}", NameList=>$attribute{nameList});
             getAddRemoveProps(NewList="#{newList}", OldList="#{props}", NameList="#{nameList}", AddProps=>$attribute{addProps}, RemoveProps=>$attribute{removeProps});
             prepareSuccessfulMsg();
             saveAdminObjectResource(
                jndiName="#{jndiName}"
                resType="#{resType}"
                resAdapter="#{resAdapter}"
                description="#{description}"
                enabled="#{enabled}"
                AddProps="${addProps}",
                RemoveProps="${removeProps}"
             );
            />    
        </sun:button>
    </sun:panelGroup>
    </facet>  
</sun:title>                   
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">			 
    <!-- Text Field section -->               
    <sun:propertySheetSection id="propertSectionTextField">
        <event>
        <!beforeCreate
            getRequestValue(key="name" value=>$page{jndiName} );
            getResourceAdapter(forAdminObject="#{true}",resAdapters=>$attribute{resAdapters});
            getAdminObjectResourceInfo(jndiName="#{jndiName}" edit="#{true}" 
                resType=>$attribute{resType} resAdapter=>$attribute{resAdapter}
                description=>$attribute{description} enabled=>$attribute{enabled}
                enabledString=>$attribute{enabledString}
                properties => $page{props}
            );
            getTableList(Properties="$pageSession{props}", TableList=>$attribute{tableList});
        />
        </event>
        <sun:property id="jndiProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}">
            <sun:staticText id="Jndi" text="#{jndiName}"/> 
        </sun:property>   

        <sun:property id="resTypeProp" rendered="#{showTypeAsDropdown}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resType}"  >
            <sun:dropDown id="resType" selected="#{resType}" labels={"" "javax.jms.Topic"  "javax.jms.Queue"}  required="#{true}"/>
        </sun:property>

        <sun:property id="resType1Prop" rendered="#{showTypeAsText}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resType}" helpText="$resource{i18n.adminObjectResource.resTypeHelp}" >
            <sun:textField id="resType" styleClass="required"  columns="$int{55}" text="#{resType}" required="#{true}"/>      
        </sun:property>

        <sun:property id="resAdatperProp"  visible="#{showAdapter}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resourceAdapter}"  helpText="$resource{i18n.adminObjectResource.resourceAdapterHelp}">
            <sun:dropDown id="resAdapter" selected="#{resAdapter}" labels="$attribute{resAdapters}" required="#{true}"/>
        </sun:property>   

        <sun:property id="descProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
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



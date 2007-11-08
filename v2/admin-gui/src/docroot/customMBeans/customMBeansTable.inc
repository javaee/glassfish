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
<!-- customMBeans/customMBeansTable.inc -->
<sun:table id="customMBeansTable" title="$resource{i18n.customMBeans.TableTitle}" sortPanelToggleButton="#{false}" 
    deselectMultipleButton="$boolean{true}" deselectMultipleButtonOnClick="setTimeout('changeThreeTableButtons()', 0)"
    selectMultipleButton="$boolean{true}" selectMultipleButtonOnClick="setTimeout('changeThreeTableButtons()', 0)" >
    <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{tableId});
    />
    <!facet actionsTop>
    <sun:panelGroup id="topActionsGroup1">
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{topActionGroup});
        />
        <sun:button id="deployButton" text="$resource{i18n.button.Deploy}" >
            <!command redirect(page="customMBeansDeploy1.jsf"); />
        </sun:button> 
        
        <sun:button id="button1" text="$resource{i18n.button.Undeploy}" disabled="#{true}"  primary="#{false}">
            <!command 
                getUIComponent(clientId="$pageSession{customMBeansTableRowGroupId}", component=>$attribute{tableRowGroup});      
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                undeployCustomMBean(selectedRows="${selectedRows}");
                navigate(page="customMBeans/customMBeans.jsf");
            />            
        </sun:button>

        <sun:button id="button2" text="$resource{i18n.button.Enable}" disabled="#{true}" primary="#{false}"
            onClick="javascript: submitAndDisable(this, '$resource{i18n.button.Processing}');" >
            <!command 
                getUIComponent(clientId="$pageSession{customMBeansTableRowGroupId}", component=>$attribute{tableRowGroup});      
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                changeAppStatus(selectedRows="${selectedRows}" appType="mbean" enabled="#{true}")
                navigate(page="customMBeans/customMBeans.jsf");
            />
        </sun:button>

        <sun:button id="button3" text="$resource{i18n.button.Disable}" disabled="#{true}" primary="#{false}" >
            <!command 
                getUIComponent(clientId="$pageSession{customMBeansTableRowGroupId}", component=>$attribute{tableRowGroup});      
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                changeAppStatus(selectedRows="${selectedRows}" appType="mbean" enabled="#{false}")
                navigate(page="customMBeans/customMBeans.jsf");
            />
        </sun:button>
        <!if  #{supportCluster}>
        <sun:dropDown id="customMBeansFilterDropDown"
            disabled="#{false}" 
            forgetValue="#{true}"
            navigateToValue="#{true}"
            label="View: " 
            labels={"$resource{i18n.common.Summary}", "$resource{i18n.common.Detailed}"}
            rendered='#{true}'
            submitForm="#{true}"
            value="#{sessionScope.customMBeansFilterSelectedValue}"
            values={"customMBeans.jsf", "customMBeansDetailed.jsf"}
            selected="#{customMBeansFilterSelectedValue}"
            >
            <!command
                getUIComponent(clientId="propertyForm:customMBeansTable:topActionsGroup1:customMBeansFilterDropDown", component=>$attribute{customMBeansFilterDropDown});	
                getUIComponentProperty(component="$attribute{customMBeansFilterDropDown}", name="value", value=>$page{customMBeansFilterDropDownValue});	
                redirect(page="#{customMBeansFilterDropDownValue}"); 
            />
        </sun:dropDown>      
        </!if>
    </sun:panelGroup>
    </facet>
    
    <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
        <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{customMBeansTableRowGroupId});
       />
 
        <sun:tableColumn selectId="select" rowHeader="$boolean{false}" id="col0">
            <sun:checkbox id="select" selected="#{td.value.selected}" selectedValue="$boolean{true}" 
                onClick="setTimeout('initAllRows(); changeThreeTableButtons()', 0)" />
        </sun:tableColumn>    
        
        <sun:tableColumn headerText="$resource{i18n.customMBeans.colMBeanName}" rowHeader="$boolean{true}" 
            id="col1">
            <sun:hyperlink url="../customMBeans/customMBeanEdit.jsf?appName=#{td.value.name}" text="#{td.value.name}" />
        </sun:tableColumn>   
        <sun:tableColumn 
            headerText="$resource{i18n.customMBeans.colImplClassName}" rowHeader="$boolean{true}" 
            id="col2">
            <staticText id="colHostName" value="#{td.value.classname}" />
        </sun:tableColumn>
        <sun:tableColumn 
            headerText="$resource{i18n.common.Enabled}" rendered="#{!supportCluster}" 
            rowHeader="$boolean{true}" id="col3">
            <staticText id="colEnabled" value="#{td.value.enable}" />
        </sun:tableColumn>
        <sun:tableColumn 
            headerText="$resource{i18n.common.Status}" rendered="#{supportCluster}" 
            rowHeader="$boolean{true}" id="col4">
            <sun:hyperlink url="../customMBeans/customMBeansTargets.jsf?appName=#{td.value.name}" text="#{td.value.status}" />
        </sun:tableColumn> 
        <sun:tableColumn 
            headerText="$resource{i18n.configuration.colTarget}" rendered="#{!isSummary}" 
            rowHeader="$boolean{true}" id="col5">
            <staticText id="colTarget" value="#{td.value.targets}" />
        </sun:tableColumn>
    </sun:tableRowGroup>   
</sun:table>    

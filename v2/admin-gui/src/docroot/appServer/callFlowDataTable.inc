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
<!-- appServer/callFlowDataTable.inc -->
    <sun:propertySheetSection id="section2" label="$resource{i18n.callFlow.DataSection}" >
    <sun:property id="tableProp" >
                           
        <sun:table id="callFlowTable" title="$resource{i18n.callFlow.requests}" sortPanelToggleButton="#{true}" 
            deselectMultipleButton="$boolean{true}"
            deselectMultipleButtonOnClick="setTimeout('changeOneTableButton()', 0)"
            selectMultipleButton="$boolean{true}"
            selectMultipleButtonOnClick="setTimeout('changeOneTableButton()', 0)" >

             <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{tableId});
            />
        >

    <!facet actionsTop>
         <sun:panelGroup id="topActionsGroup1">
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{topActionGroup});
            />
            <sun:button id="button1" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}"
                    onClick="if ( getConfirm(this, '$resource{i18n.msg.JS.confirmDeleteCallFlowData}') )
                                  { return submitAndDisable(this, '$resource{i18n.button.Processing}');} 
                                    else {return false;} " >
            <!command 
                getUIComponent(clientId="$pageSession{callFlowTableRowGroupId}", component=>$attribute{tableRowGroup});   
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                deleteCallFlowRequest(selectedRows="${selectedRows}" instanceName="#{instanceName}");
                redirect(page="#{actualLink}?instanceName=#{instanceName}&filterValue=#{filterValue}");
            />
            </sun:button>
            
            <sun:dropDown id="filter" selected="#{filterValue}" label="$resource{i18n.common.filterDropDown}" submitForm="#{true}" immediate="#{true}" labels="$attribute{filterListLabel}" values="$attribute{filterListValue}" >
                <!command
                    setAttribute(key="click" value="$this{component}");
                    setAttribute(key="filterValue" value="#{click.selected}")
                    redirect(page="#{actualLink}?instanceName=#{instanceName}&filterValue=#{filterValue}");
                />
            </sun:dropDown>
         </sun:panelGroup>
     </facet>

   <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
    <!afterCreate
         getClientId(component="$this{component}" clientId=>$page{callFlowTableRowGroupId});
    />
   <sun:tableColumn selectId="select" rowHeader="$boolean{false}" id="col0">
            <sun:checkbox id="select" 
                selected="#{td.value.selected}" 
                selectedValue="$boolean{true}" 
                onClick="setTimeout('initAllRows(); changeOneTableButton()', 0)" 
            />
   </sun:tableColumn>    

    <sun:tableColumn headerText="$resource{i18n.common.TimeStamp}"  sort="timeStamp" rowHeader="$boolean{true}" extrahtml="nowrap" id="col1">
        <sun:hyperlink id="link" text="#{td.value.timeStampFormatted}" url="/appServer/callFlowDetail.jsf?requestId=#{td.value.requestId}&instanceName=#{instanceName}&user=#{td.value.user}&responseTime=#{td.value.responseTime}&backLink=#{actualLink}" />
    </sun:tableColumn>
    
    <sun:tableColumn headerText="$resource{i18n.common.ResponseTime}"  sort="responseTime" rowHeader="$boolean{true}" id="responseTime">
        <staticText id="responseTime" value="#{td.value.responseTime}" />
    </sun:tableColumn>
    
      <sun:tableColumn headerText="$resource{i18n.common.Response}"  sort="response" rowHeader="$boolean{true}" id="response">
        <staticText id="response" value="#{td.value.response}" />
    </sun:tableColumn>
    
    <sun:tableColumn headerText="$resource{i18n.common.User}"  sort="user" rowHeader="$boolean{true}" id="user">
        <staticText id="user" value="#{td.value.user}" />
    </sun:tableColumn>
   
    <sun:tableColumn headerText="$resource{i18n.callFlow.StartContainer}"  sort="startContainer" rowHeader="$boolean{true}" id="startContainer">
        <staticText id="startContainer" value="#{td.value.startContainer}" />
    </sun:tableColumn>
    
     <sun:tableColumn headerText="$resource{i18n.common.ClientHost}"  sort="clientHost" rowHeader="$boolean{true}" id="clientHost">
        <staticText id="clientHost" value="#{td.value.clientHost}" />
     </sun:tableColumn>
    
      <sun:tableColumn headerText="$resource{i18n.common.Application}"  sort="application" rowHeader="$boolean{true}" id="application">
        <staticText id="application" value="#{td.value.application}" />
      </sun:tableColumn>
    
   </sun:tableRowGroup>
  
        </sun:table>
    </sun:property> 
    </sun:propertySheetSection>


           

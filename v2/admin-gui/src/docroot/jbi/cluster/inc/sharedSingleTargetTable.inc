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
<!-- jbi/cluster/inc/sharedSingleTargetTable.inc -->

    <sun:form id="sharedSingleTargetTableForm" >
                           
        <sun:table id="singleTargetTable" title="#{'service-assembly'==sessionScope.jbiSharedSingleTargetTableType ? '$resource{i18n.jbi.single.target.table.title.deployments.text}' : ('shared-library'==sessionScope.jbiSharedSingleTargetTableType ?  '$resource{i18n.jbi.single.target.table.title.libraries.text}' : '$resource{i18n.jbi.single.target.table.title.components.text}')}"
            deselectMultipleButton="$boolean{true}"
            deselectMultipleButtonOnClick="setTimeout('changeThreeTableButtons()', 0)"
            selectMultipleButton="$boolean{true}"
            selectMultipleButtonOnClick="setTimeout('changeThreeTableButtons()', 0)"
            paginateButton="#{true}"
            paginationControls="#{true}"
            sortPanelToggleButton="#{false}" 
            >
            
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{tableId});
            />
            
            <!facet actionsTop>
                 <sun:panelGroup id="topActionsGroup1">
                 
                     <!afterCreate
                         getClientId(component="$this{component}" clientId=>$page{topActionGroup});
                     />
            
                     <sun:button id="newSharedSingleTargetTableButton" 
                         disabled="#{false}" 
                         primary="#{true}"
                         text="#{sessionScope.newButton}" 
                         >
                         <!command
navigate(page="jbi/cluster/#{sessionScope.newButtonUrl}");
                         />
                     </sun:button>

                     <sun:button id="button1"  
                         onClick="
                             if (! getConfirm(this, '#{sessionScope.jbiSharedTableButton1Confirmation}')) 
                                 return false;
                             return submitAndDisable(this, '$resource{i18n.button.Processing}');"  
                         disabled="#{true}" 
                         primary="#{false}"
                         text="$resource{i18n.button.Remove}" 
                         >
                         <!command
getUIComponent(clientId="sharedSingleTargetTableForm:singleTargetTable:tableRowGroup", component=>$attribute{tableRowGroup});  
jbiRemoveSelectedSingleTargetRows(tableRowGroup="$attribute{tableRowGroup}", targetName="#{instanceName}", isAlertNeeded=>$session{isJbiAlertNeeded}, alertSummary=>$session{jbiAlertSummary}, alertDetails=>$session{jbiAlertDetails});
jbiIncrementAlertCountIfNeeded(isAlertNeeded='$session{isJbiAlertNeeded}')
redirect(page="#{sessionScope.redirectOperationRefresh}#{instanceName}")
                         />
                     </sun:button>

                     <sun:button id="button2" 
                         disabled="#{true}" 
                         primary="#{false}"
	                 rendered="#{hasStatus}"
                         text="$resource{i18n.button.Enable}" 
                         >
                         <!command
getUIComponent(clientId="sharedSingleTargetTableForm:singleTargetTable:tableRowGroup", component=>$attribute{tableRowGroup});  
jbiOperateSelectedSingleTargetRows(tableRowGroup="$attribute{tableRowGroup}", targetName="#{instanceName}", operation="#{OperationBean.valueStart}", componentOrDeploymentType="#{sessionScope.jbiSharedSingleTargetTableType}", isAlertNeeded=>$session{isJbiAlertNeeded}, alertSummary=>$session{jbiAlertSummary}, alertDetails=>$session{jbiAlertDetails});
jbiIncrementAlertCountIfNeeded(isAlertNeeded='$session{isJbiAlertNeeded}')
redirect(page="#{sessionScope.redirectOperationRefresh}#{instanceName}")
                          />
                     </sun:button>

                     <sun:button id="button3" 
                         disabled="#{true}"
                         primary="#{false}"
	                 rendered="#{hasStatus}"
                         text="$resource{i18n.button.Disable}" 
                         >
                         <!command
getUIComponent(clientId="sharedSingleTargetTableForm:singleTargetTable:tableRowGroup", component=>$attribute{tableRowGroup});  
jbiOperateSelectedSingleTargetRows(tableRowGroup="$attribute{tableRowGroup}", targetName="#{instanceName}", operation="#{OperationBean.valueShutDown}", componentOrDeploymentType="#{sessionScope.jbiSharedSingleTargetTableType}", isAlertNeeded=>$session{isJbiAlertNeeded}, alertSummary=>$session{jbiAlertSummary}, alertDetails=>$session{jbiAlertDetails});
jbiIncrementAlertCountIfNeeded(isAlertNeeded='$session{isJbiAlertNeeded}')
redirect(page="#{sessionScope.redirectOperationRefresh}#{instanceName}")
                         />
                     </sun:button>            
                </sun:panelGroup>
            </facet>

            <sun:tableRowGroup id="tableRowGroup" 
                aboveColumnHeader="#{true}"
                rows="$int{5}"
                selected="#{sourceVarRow.value.selected}" 
                sourceData="#{sessionScope.singleTargetTableSourceData}"
                sourceVar="sourceVarRow"
                >
                <sun:tableColumn id="selectTableColumn"
                    rowHeader="$boolean{false}" 
                    selectId="selectCheckbox" 
                    >
                    <sun:checkbox id="selectCheckbox"   
                        selected="#{sourceVarRow.value.selected}"
                        selectedValue="$boolean{true}"
                        onClick="setTimeout('initAllRows(); changeThreeTableButtons()', 0)" 
                    />
                </sun:tableColumn>    

                <sun:tableColumn id="nameTableColumn"
                    headerText="Name"  
                    rendered="#{true}" 
                    sort="name" 
                    rowHeader="$boolean{true}" 
                    extrahtml="nowrap" id="col1"
                    >
                    <sun:hyperlink id="nameHyperlink"
			 text="#{sourceVarRow.value.name}"

			>
                     <!command
redirect(page="#{sessionScope.showNameLinkUrl}?type=#{sourceVarRow.value.type}&name=#{sourceVarRow.value.name}#show")
                     />
                    </sun:hyperlink>
                </sun:tableColumn>
    
                <sun:tableColumn id="enabledTableColumn"
                    headerText="$resource{i18n.common.Enabled}"  
                    rendered="#{sessionScope.hasStatus}"
                    rowHeader="$boolean{true}" 
                    sort="enabled" 
                    >
                    <staticText id="enabled" value="#{sourceVarRow.value.enabled}" />
                </sun:tableColumn>

                <sun:tableColumn id="typeTableColumn"
                    headerText="$resource{i18n.common.Type}"  
                    rendered="#{sessionScope.hasType}"
                    rowHeader="$boolean{true}" 
                    sort="type"  
                    >

		    <sun:staticText id="typeBindingText"
	                rendered="#{'binding-component' == sourceVarRow.value.type}" 
                        value="$resource{i18n.jbi.list.type.binding-component}"
                        />

                    <sun:staticText id="typeEngineText"
	                rendered="#{'service-engine' == sourceVarRow.value.type}" 
                        value="$resource{i18n.jbi.list.type.service-engine}"
                        />

                </sun:tableColumn>
    
            </sun:tableRowGroup>
  
         </sun:table>

    </sun:form>

           

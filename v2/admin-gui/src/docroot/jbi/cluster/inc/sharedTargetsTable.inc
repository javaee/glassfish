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
<!-- jbi/cluster/inc/sharedTargetsTable.inc -->

    <!beforeCreate
getClusterList(clusterList => $attribute{clusterTargetsList});
getStandaloneServerList(serverList => $attribute{standaloneServerTargetsList});
jbiSetFilteredTargetsList(
    pageName = '#{sessionScope["sharedTableType"]}'
    clustersList = "$attribute{clusterTargetsList}"
    serversList = "$attribute{standaloneServerTargetsList}");
    />

    <sun:form id="sharedTargetsTableForm" >
                <sun:table id="sharedTargetsTable" title='$resource{i18n.common.TargetTableTitle}'
                    deselectMultipleButton='#{sessionScope["hasSelectionColumn"]}'
                    deselectMultipleButtonOnClick="setTimeout('changeThreeTableButtons2()', 0)"
                    selectMultipleButton='#{sessionScope["hasSelectionColumn"]}'
                    selectMultipleButtonOnClick="setTimeout('changeThreeTableButtons2()', 0)"
                    paginateButton="#{true}"
                    paginationControls="#{true}"
                    lite="#{false}"
		    >  
                    
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{tableId2});
                    />
                    
                    <sun:tableRowGroup id="tableRowGroup"
                         aboveColumnHeader="#{true}"
                         rows="$int{5}"
                         selected="#{sourceVarRow.value.selected}"
                         sourceData="#{ShowBean.sharedTargetsTableData}"
                         sourceVar="sourceVarRow" 
                         >
                         
                         <sun:tableColumn id="selectedTableColumn" 
 		              rendered='#{sessionScope["hasSelectionColumn"]}'
                              selectId="selectCheckbox"
                              >

                              <sun:checkbox id="selectCheckbox" 
			        selected="#{sourceVarRow.value.selected}"
				selectedValue="$boolean{true}"
                                onClick="setTimeout('initAllRows2(); changeThreeTableButtons2()', 0)" 
                              />
                         </sun:tableColumn>
                         
                         <sun:tableColumn id="nameTableColumn" 
                              headerText="$resource{i18n.jbi.shared.targets.table.column.name.text}"
                              rowHeader="#{true}"
                              sort="name" 
                              >
                              <sun:hyperlink id="nameHyperlink"
                                    rendered="#{true}" 
                                    style="font-weight:normal"
                                    toolTip="$resource{i18n.jbi.shared.targets.table.name.link.tooltip}" 
                                    value="#{sourceVarRow.value.name}"
                                    > 
	                            <!command
jbiGetWormholeLinkForName(clusterOrServerName='#{sourceVarRow.value.name}', wormholeLink=>$attribute{wormholeLink});
redirect(page="../../$attribute{wormholeLink}");
                                    />
	                      </sun:hyperlink>

                         </sun:tableColumn>

                         <sun:tableColumn id="statusTableColumn" 
                              headerText="$resource{i18n.jbi.shared.table.column.status.text}"
 		              rendered='#{sessionScope["hasStateColumn"]}'
                              rowHeader="#{true}"
                              sort="status" 
                              >
                              <sun:staticText id="sharedLifeCycleStateText" 
                                   value="#{sourceVarRow.value.status}"
                                     />
                         </sun:tableColumn>

                         <sun:tableColumn id="typeTableColumn" 
                              headerText="$resource{i18n.jbi.shared.table.column.type.text}"
                              rowHeader="#{true}"
                              sort="type" 
                              >
                              <sun:staticText id="typeStaticText" 
                                   value="#{sourceVarRow.value.type}"
                                     />
                         </sun:tableColumn>

                    </sun:tableRowGroup>

                    <!facet actionsTop>

                         <sun:panelGroup id="topActionsGroup1">

                               <!afterCreate
                                   getClientId(component="$this{component}" clientId=>$page{topActionGroup2});
                               />

                               <sun:button id="sharedTargetsTableManageTargetsButton"
                                   disabled="#{false}"
                                   styleClass="Btn1"
                                   value="$resource{i18n.button.ManageTarget}"
                                   > 
                                   <!command
                                       navigate(page="jbi/cluster/manageJBITargets.jsf");
                                   />
                               </sun:button>

                               <sun:image id="actionTopSeparator1"
                                   align="top"
                                   height="$int{18}"
                                   url="/resource/images/jbi/actions_separator.gif"
                                   width="$int{18}"
                                   />
                                   
                               <sun:button id="button1"
                                   disabled="#{true}"
                                   primary="$boolean{false}"
                                   rendered='#{sessionScope["hasLifeCycleStateColumn"]}'
                                   value="$resource{i18n.button.Start}"
                                   >
                                        <!command
getUIComponent(clientId="sharedTargetsTableForm:sharedTargetsTable:tableRowGroup", component=>$attribute{tableRowGroup});  
jbiOperateSelectedTargetRows(tableRowGroup="$attribute{tableRowGroup}", componentOrDeploymentType="#{ShowBean.type}", componentOrDeploymentName="#{sessionScope.sharedShowName}", operation="#{OperationBean.valueStart}", isAlertNeeded=>$session{isJbiAlertNeeded}, alertSummary=>$session{jbiAlertSummary}, alertDetails=>$session{jbiAlertDetails});
jbiIncrementAlertCountIfNeeded(isAlertNeeded='$session{isJbiAlertNeeded}')
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="showTargets.jsf?type=#{sessionScope.sharedShowType}&name=#{sessionScope.sharedShowName}")

                                        />

                               </sun:button>

                               <sun:button id="button2"
                                   disabled="#{true}"
                                   primary="$boolean{false}"
                                   rendered='#{sessionScope["hasLifeCycleStateColumn"]}'
                                   value="$resource{i18n.button.Stop}"
                                   >
                                        <!command
getUIComponent(clientId="sharedTargetsTableForm:sharedTargetsTable:tableRowGroup", component=>$attribute{tableRowGroup});  
jbiOperateSelectedTargetRows(tableRowGroup="$attribute{tableRowGroup}", componentOrDeploymentType="#{ShowBean.type}", componentOrDeploymentName="#{sessionScope.sharedShowName}", operation="#{OperationBean.valueStop}");
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="showTargets.jsf?type=#{sessionScope.sharedShowType}&name=#{sessionScope.sharedShowName}")
                                        />
                               </sun:button>

                               <sun:button id="button3"
                                   disabled="#{true}"
                                   primary="$boolean{false}"
                                   rendered='#{sessionScope["hasStateColumn"]}'
                                   value="$resource{i18n.jbi.operations.shutdown}"
                                   >
                                        <!command
getUIComponent(clientId="sharedTargetsTableForm:sharedTargetsTable:tableRowGroup", component=>$attribute{tableRowGroup});  
jbiOperateSelectedTargetRows(tableRowGroup="$attribute{tableRowGroup}", componentOrDeploymentType="#{ShowBean.type}", componentOrDeploymentName="#{sessionScope.sharedShowName}", operation="#{OperationBean.valueShutDown}");
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="showTargets.jsf?type=#{sessionScope.sharedShowType}&name=#{sessionScope.sharedShowName}")
                                        />
                               </sun:button>

                               <sun:image id="actionTopSeparator2"
                                   align="top"
                                   height="$int{18}"
                                   rendered='#{sessionScope["hasStateFilter"]}'
                                   url="/resource/images/jbi/actions_separator.gif"
                                   width="$int{18}"
                                  />

                               <sun:dropDown id="filterTargetByStateDropDown"
                                   disabled    = "#{false}" 
                                   forgetValue = "#{true}"
                                   label       = "$resource{i18n.jbi.filter.state.label}"
                                   labels      = {"$resource{i18n.jbi.filter.state.all}" "$resource{i18n.jbi.filter.state.enabled}" "$resource{i18n.jbi.filter.state.disabled}"}
                                   submitForm  = "#{true}"
                                   value       = "#{ShowBean.filterTargetComponentState}"
                                   values      = {"Show All" "Enabled" "Disabled"}
                                   rendered    = '#{sessionScope["hasTargetStateComponentFilter"]}'
                               >
                               <!-- values are not I18n, labels are -->
                                   <!command
getUIComponent (clientId="sharedTargetsTableForm:sharedTargetsTable:topActionsGroup1:filterTargetByStateDropDown", component=>$attribute{filterTargetByStateDropDown})
getUIComponentProperty (component="$attribute{filterTargetByStateDropDown}", name="value", value=>$attribute{dropDownValue})   
getUIComponentProperty (component="$attribute{filterTargetByStateDropDown}", name="values", value=>$attribute{dropDownValues})	
jbiFilterTargetTableComponentState (value="$attribute{dropDownValue}",values="$attribute{dropDownValues}")
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="showTargets.jsf?type=#{sessionScope.sharedShowType}&name=#{sessionScope.sharedShowName}")
                                   />
                               </sun:dropDown>
                                
                               <sun:dropDown id="filterTargetByStateDropDown2"
                                   disabled    = "#{false}" 
                                   forgetValue = "#{true}"
                                   label       = "$resource{i18n.jbi.filter.state.label}"
                                   labels      = {"$resource{i18n.jbi.filter.state.all}" "$resource{i18n.jbi.filter.state.enabled}" "$resource{i18n.jbi.filter.state.disabled}"}
                                   submitForm  = "#{true}"
                                   value       = "#{ShowBean.filterTargetAssemblyState}"
                                   values      = {"Show All" "Enabled" "Disabled"}
                                   rendered    = '#{sessionScope["hasTargetStateAssemblyFilter"]}'
                               >
                                <!-- values are not I18n, labels are -->
                                   <!command
getUIComponent (clientId="sharedTargetsTableForm:sharedTargetsTable:topActionsGroup1:filterTargetByStateDropDown2", component=>$attribute{filterTargetByStateDropDown2})
getUIComponentProperty (component="$attribute{filterTargetByStateDropDown2}", name="value", value=>$attribute{dropDownValue})  
getUIComponentProperty (component="$attribute{filterTargetByStateDropDown2}", name="values", value=>$attribute{dropDownValues})	
jbiFilterTargetTableAssemblyState (value="$attribute{dropDownValue}",values="$attribute{dropDownValues}")
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="showTargets.jsf?type=#{sessionScope.sharedShowType}&name=#{sessionScope.sharedShowName}")
                                   />
                               </sun:dropDown>
                                   

                         </sun:panelGroup>

                    </facet>

                </sun:table>

</sun:form>

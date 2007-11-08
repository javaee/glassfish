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
<!-- jbi/pe/inc/sharedTable.inc -->
<!beforeCreate
testExists(attr="$requestParameter{filterType}" defined=>$attribute{hasFilterTypeKey});
if (${hasFilterTypeKey}) { // root pages set filter type via handler; tree passes typeFilter=Show%20All (workaround)
    getRequestValue(key="filterType" default="Show All" value=>$page{filterType});
    filterTableType(operation="$pageSession{filterType}")
}
setSharedTableData(tableType="#{sessionScope.sharedTableType}")
/>

<sun:form id="sharedTableForm" >
	        <sun:table id="sharedTable" title='#{sessionScope["sharedTableTitle"]}'
                    deselectMultipleButton="$boolean{true}"
                    deselectMultipleButtonOnClick="setTimeout('changeButtonAndDropDown()', 0)"
                    selectMultipleButton="$boolean{true}"
                    selectMultipleButtonOnClick="setTimeout('changeButtonAndDropDown()', 0)" 
                    paginateButton="#{true}"
                    paginationControls="#{true}"
                    >
 		    
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{tableId});
                    />
                    
                    <sun:tableRowGroup id="sharedTableRowGroup"
                         aboveColumnHeader="#{true}"
                         rows="$int{5}"
		         selected="#{sourceVarRow.value.selected}"
			 sourceData="#{ListBean.sharedTableData}"
			 sourceVar="sourceVarRow" 
                         >
                         <sun:tableColumn id="sharedSelectedTableColumn" 
                              selectId="select"
                              >
                              <sun:checkbox id="select" 
			        selected="#{sourceVarRow.value.selected}"
				selectedValue="$boolean{true}"
                                onClick="setTimeout('initAllRows(); changeButtonAndDropDown()', 0)" 
                              />
                         </sun:tableColumn>

                         <sun:tableColumn id="sharedNamesTableColumn" 
                              headerText="$resource{i18n.jbi.shared.table.column.name.text}"
                              rowHeader="#{true}"
                              sort="#{sourceVarRow.value.name}"                             
                              >
                              <sun:image id="sharedTableNameSelectedImage" 
                                    align="middle"
                                    height="$int{15}"
                                    rendered="#{(ShowBean.name == sourceVarRow.value.name) && (sessionScope['renderShow']) }"
                                    url="/resource/images/jbi/bullet-15-15.gif"
                                    width="$int{15}"
                                    />

                              <sun:staticText id="sharedTableNameText"
                                  rendered="#{(ShowBean.name == sourceVarRow.value.name) && (sessionScope['renderShow']) }"
                                  style="font-weight:bold"
                                  value="#{sourceVarRow.value.name}"
                                  />
                              
                              <sun:hyperlink id="sharedTableNameHyperlink"
                                    rendered="#{!((ShowBean.name == sourceVarRow.value.name) && (sessionScope['renderShow'])) }"
                                    style="font-weight:normal"
                                    toolTip="$resource{i18n.jbi.shared.table.name.link.tooltip}" 
                                    value="#{sourceVarRow.value.name}"
			            url="#{sessionScope.showNameLinkUrl}?type=#{sourceVarRow.value.type}&name=#{sourceVarRow.value.name}#show"
                                    />   

                         </sun:tableColumn>

                         <sun:tableColumn id="sharedStateTableColumn" 
                              headerText="$resource{i18n.jbi.shared.table.column.state.text}"
 		              rendered='#{sessionScope["hasStateColumn"]}'
                              rowHeader="#{true}"
                              sort="#{sourceVarRow.value.summaryStatus}"                             
                              >
                              <sun:staticText id="sharedstateStarted"
	                          value="#{sourceVarRow.value.summaryStatus}"
                              />
                         </sun:tableColumn>

                         <sun:tableColumn id="sharedTypeTableColumn" 
                              headerText="$resource{i18n.jbi.shared.table.column.type.text}"
 		              rendered='#{sessionScope["hasTypeColumn"]}'
                              rowHeader="#{true}"
                              sort="#{sourceVarRow.value.type}"                             
                              >

                              <sun:staticText id="sharedTypeBindingText"
	                          rendered="#{'binding-component' == sourceVarRow.value.type}" 
                                  value="$resource{i18n.jbi.list.type.binding-component}"
                                  />

                              <sun:staticText id="sharedTypeEngineText"
	                          rendered="#{'service-engine' == sourceVarRow.value.type}" 
                                  value="$resource{i18n.jbi.list.type.service-engine}"
                                  />

                         </sun:tableColumn>

                         <sun:tableColumn id="sharedDescriptionTableColumn" 
                              headerText="$resource{i18n.jbi.shared.table.column.description.text}"
                              rowHeader="#{true}"
                              sort="#{sourceVarRow.value.description}"                             
                              >
                              <sun:staticText id="sharedDescriptionText" 
                                  value="#{sourceVarRow.value.description}"
                                  />
                         </sun:tableColumn>
                         
                         <sun:tableColumn id="componentActionsColumn" 
                              headerText="$resource{i18n.jbi.shared.table.column.action.text}"
                              rendered='false'
                              rowHeader="#{true}"
                              >

                              <sun:hyperlink id="sharedTableNameHyperlink"
                                    style="font-weight:normal"
                                    toolTip="$resource{i18n.jbi.shared.table.update.link.tooltip}" 
                                    value="$resource{i18n.jbi.shared.table.update.link.value}" 
                                    url="#{sessionScope.jbiSharedTableUpdateRedirectPage}?type=#{sourceVarRow.value.type}&name=#{sourceVarRow.value.name}"
                                    />   

                         </sun:tableColumn>

                    </sun:tableRowGroup>

                    <!facet actionsTop>

                         <sun:panelGroup id="topActionsGroup1">

                               <!afterCreate
                                   getClientId(component="$this{component}" clientId=>$page{topActionGroup});
                               />

                               <sun:button id="newSharedTableButton"
                                   primary="$boolean{true}"
                                   value='#{sessionScope.newButton}'
                                   >
				<!command
   		                        navigate(page="jbi/pe/#{sessionScope.newButtonUrl}");
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
                                   primary="#{false}"
                                   value="#{sessionScope.jbiDeleteButton}"
                                   onClick="
                                       if (! getConfirm(this, '#{sessionScope.jbiSharedTableButton1Confirmation}')) 
                                           return false;
                                       return submitAndDisable(this, '$resource{i18n.button.Processing}');"  
                                   >
				<!command
getUIComponent(clientId="sharedTableForm:sharedTable:sharedTableRowGroup", component=>$attribute{tableRowGroup})
jbiDeleteSelectedRows(tableRowGroup="$attribute{tableRowGroup}", tableType="#{sessionScope.sharedTableType}", isAlertNeeded=>$session{isJbiAlertNeeded}, alertSummary=>$session{jbiAlertSummary}, alertDetails=>$session{jbiAlertDetails})
jbiIncrementAlertCountIfNeeded(isAlertNeeded='$session{isJbiAlertNeeded}')
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="#{sessionScope.redirectOperationRefresh}?type=#{ShowBean.type}&name=#{ShowBean.name}#$pageSession{anchor}")
				/>
			       </sun:button>
				
                               <sun:dropDown id="dropdown1"
                                   disabled="#{true}" 
                                   forgetValue="#{true}"
                                   labels={"$resource{i18n.jbi.operations.label}" "$resource{i18n.jbi.operations.start}" "$resource{i18n.jbi.operations.stop}" "$resource{i18n.jbi.operations.shutdown}"}
                                   rendered='#{sessionScope["hasOperationsDropDown"]}'
                                   submitForm="#{true}"
                                   value="$resource{i18n.jbi.operations.label}"
                                   values={"ignored", "Start", "Stop", "Shut Down"}
                               >
                                <!-- values are not I18n, labels are -->
				<!command
getUIComponent(clientId="sharedTableForm:sharedTable:sharedTableRowGroup", component=>$attribute{tableRowGroup});	
getUIComponent(clientId="sharedTableForm:sharedTable:topActionsGroup1:dropdown1", component=>$attribute{dropdown1});	
getUIComponentProperty(component="$attribute{dropdown1}", name="value", value=>$attribute{dropDownValue});	
jbiOperateSelectedComponentOrDeploymentRows(tableRowGroup="$attribute{tableRowGroup}", tableType="#{sessionScope.sharedTableType}", operation="$attribute{dropDownValue}", isAlertNeeded=>$session{isJbiAlertNeeded}, alertSummary=>$session{jbiAlertSummary}, alertDetails=>$session{jbiAlertDetails});
jbiIncrementAlertCountIfNeeded(isAlertNeeded='$session{isJbiAlertNeeded}')
setSharedTableData(tableType="#{sessionScope.sharedTableType}")
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="#{sessionScope.redirectOperationRefresh}?type=#{ShowBean.type}&name=#{ShowBean.name}#$pageSession{anchor}");
				/>

                               </sun:dropDown>
       
                               <sun:image id="actionTopSeparator2"
                                   align="top"
                                   height="$int{18}"
                                   rendered='#{sessionScope["hasStateFilter"]}'
                                   url="/resource/images/jbi/actions_separator.gif"
                                   width="$int{18}"
                                  />
                                   
                               <sun:dropDown id="filterByAssemblyStateDropDown"
                                   disabled    = "#{false}" 
                                   forgetValue = "#{true}"
                                   label       = "$resource{i18n.jbi.filter.state.label}"
                                   labels      = {"$resource{i18n.jbi.filter.state.all}" "$resource{i18n.jbi.filter.state.started}" "$resource{i18n.jbi.filter.state.stopped}" "$resource{i18n.jbi.filter.state.shutdown}"}
                                   submitForm  = "#{true}"
                                   value       = "#{ListBean.filterAssemblyState}"
                                   values      = {"Show All" "Started" "Stopped" "Shutdown"}
                                   rendered    = '#{sessionScope["hasAssemblyStateFilter"]}'
                               >
                                   <!command
getUIComponent (clientId="sharedTableForm:sharedTable:topActionsGroup1:filterByAssemblyStateDropDown", component=>$attribute{filterByAssemblyStateDropDown})
getUIComponentProperty (component="$attribute{filterByAssemblyStateDropDown}", name="value", value=>$attribute{dropDownValue})	
<!-- filterTableAssemblyState (operation="$attribute{dropDownValue}") -->
getUIComponentProperty (component="$attribute{filterByAssemblyStateDropDown}", name="values", value=>$attribute{dropDownValues})	
jbiFilterTableAssemblyState (value="$attribute{dropDownValue}",values="$attribute{dropDownValues}")
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="#{sessionScope.redirectOperationRefresh}?type=#{ShowBean.type}&name=#{ShowBean.name}#$pageSession{anchor}");
                                   />
                               </sun:dropDown>


                               <sun:dropDown id="filterByComponentStateDropDown"
                                   disabled    = "#{false}" 
                                   forgetValue = "#{true}"
                                   label       = "$resource{i18n.jbi.filter.state.label}"
                                   labels      = {"$resource{i18n.jbi.filter.state.all}" "$resource{i18n.jbi.filter.state.started}" "$resource{i18n.jbi.filter.state.stopped}" "$resource{i18n.jbi.filter.state.shutdown}"}
                                   submitForm  = "#{true}"
                                   value       = "#{ListBean.filterComponentState}"
                                   values      = {"Show All" "Started" "Stopped" "Shutdown"}
                                   rendered    = '#{sessionScope["hasComponentStateFilter"]}'
                               >
                                   <!command
getUIComponent (clientId="sharedTableForm:sharedTable:topActionsGroup1:filterByComponentStateDropDown", component=>$attribute{filterByComponentStateDropDown})
getUIComponentProperty (component="$attribute{filterByComponentStateDropDown}", name="value", value=>$attribute{dropDownValue})	
getUIComponentProperty (component="$attribute{filterByComponentStateDropDown}", name="values", value=>$attribute{dropDownValues})	
<!-- filterTableComponentState (operation="$attribute{dropDownValue}") -->
jbiFilterTableComponentState (value="$attribute{dropDownValue}",values="$attribute{dropDownValues}")
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="#{sessionScope.redirectOperationRefresh}?type=#{ShowBean.type}&name=#{ShowBean.name}#$pageSession{anchor}");
                                   />
                               </sun:dropDown>

sharedTableForm:sharedTable:topActionsGroup1:filterByComponentStateDropDown
                               <sun:image id="actionTopSeparator3"
                                    align="top"
                                    height="$int{18}"
                                    rendered='#{sessionScope["hasTypeFilter"]}'
                                    url="/resource/images/jbi/actions_separator.gif"
                                    width="$int{18}"
                                   />

                               <sun:dropDown id="filterTypeDropDown"
                                   disabled    = "#{false}" 
                                   forgetValue = "#{true}"
                                   label       = "$resource{i18n.jbi.filter.type.label}" 
                                   labels      = {"$resource{i18n.jbi.filter.type.all}" "$resource{i18n.jbi.filter.type.binding}" "$resource{i18n.jbi.filter.type.engine}"}
                                   rendered    = '#{sessionScope["hasOperationsDropDown"]}'
                                   submitForm  = "#{true}"
                                   value       = "#{ListBean.filterType}"
                                   values      = {"Show All" "Binding" "Engine"}
                                   rendered    = '#{sessionScope["hasTypeFilter"]}'
                               >
                                <!-- values are not I18n, labels are -->
                                   <!command
getUIComponent (clientId="sharedTableForm:sharedTable:topActionsGroup1:filterTypeDropDown", component=>$attribute{filterTypeDropDown})
getUIComponentProperty (component="$attribute{filterTypeDropDown}", name="value", value=>$attribute{dropDownValue})	
filterTableType (operation="$attribute{dropDownValue}")
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="#{sessionScope.redirectOperationRefresh}?type=#{ShowBean.type}&name=#{ShowBean.name}#$pageSession{anchor}");
                                   />
                               </sun:dropDown>
                               
                         </sun:panelGroup>

                    </facet>

                </sun:table>

</sun:form>

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
<!-- jbi/cluster/cluster/inc/sharedTableEE.inc -->
    <!beforeCreate
testExists(attr="$requestParameter{filterType}" defined=>$attribute{hasFilterTypeKey});
if (${hasFilterTypeKey}) { // root pages set filter type via handler; tree passes typeFilter=Show%20All (workaround)
    getRequestValue(key="filterType" default="Show All" value=>$page{filterType});
    filterTableType(operation="$pageSession{filterType}")
}
setSharedTableData(tableType="#{sessionScope.sharedTableType}")
    />

<sun:form id="sharedTableEEForm" >
	        <sun:table id="sharedTableEE" title="#{sessionScope.sharedTableTitle}"
                    deselectMultipleButton="$boolean{true}"
                    deselectMultipleButtonOnClick="setTimeout('changeThreeTableButtons()', 0)"
                    selectMultipleButton="$boolean{true}"
                    selectMultipleButtonOnClick="setTimeout('changeThreeTableButtons()', 0)" 
                    paginateButton="#{true}"
                    paginationControls="#{true}"
		    >  
                       
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{tableId});
                    />
 	       
 		    <sun:tableRowGroup id="sharedTableEERowGroup"
                         aboveColumnHeader="#{true}"
                         rows="$int{5}"
		         selected="#{sourceVarRow.value.selected}"
			 sourceData="#{ListBean.sharedTableData}"
		         sourceVar="sourceVarRow"
                         >
                         
                         <sun:tableColumn id="sharedSelectedTableColumn" 
                              selectId="select"
                            <!--  sort="#{sourceVarRow.value.selectedState}"  -->                          
                              >

                              <sun:checkbox id="select" 
			        selected="#{sourceVarRow.value.selected}"
				selectedValue="$boolean{true}"
                                onClick="setTimeout('initAllRows(); changeThreeTableButtons()', 0)" 
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
                                    url="../../images/jbi/bullet-15-15.gif"
                                    width="$int{15}"
                                    />
                              
                              <sun:staticText id="sharedTableNameText"
                                  rendered="#{(ShowBean.name == sourceVarRow.value.name) && (sessionScope['renderShow']) }"
                                  style="font-weight:bold"
                                  value="#{sourceVarRow.value.name}"
                                  />
                              
                              <sun:hyperlink id="sharedTableEENameHyperlink"
                                    rendered="#{(ShowBean.name != sourceVarRow.value.name) || (!sessionScope['renderShow']) }"
                                    style="font-weight:normal"
                                    toolTip="$resource{i18n.jbi.shared.table.name.link.tooltip}" 
                                    value="#{sourceVarRow.value.name}"
			            url="#{sessionScope.showNameLinkUrl}?type=#{sourceVarRow.value.type}&name=#{sourceVarRow.value.name}"
                                    />

                         </sun:tableColumn>

                         <sun:tableColumn id="sharedStatusTableColumn" 
                              headerText="$resource{i18n.common.Status}"
 		              rendered='#{sessionScope["hasStateColumn"]}'
                              rowHeader="#{true}"
                              sort="#{sourceVarRow.value.summaryStatus}"                             
                              >
                              <sun:staticText id="sharedStateText" 
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

                    </sun:tableRowGroup>

                    <!facet actionsTop>

                         <sun:panelGroup id="topActionsGroup1">

                               <!afterCreate
                                   getClientId(component="$this{component}" clientId=>$page{topActionGroup});
                               />

                               <sun:button id="newSharedTableEEButton"
                                   primary="#{true}"
                                   styleClass="Btn1"
                                   url='#{sessionScope["newButtonUrl"]}' 
                                   value='#{sessionScope["newButton"]}'
                                   >
				   <!command
navigate(page="jbi/cluster/#{sessionScope.newButtonUrl}");
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
                                   value="$resource{i18n.button.Delete}"
                                   onClick="
                                       if (! getConfirm(this, '#{sessionScope.jbiSharedTableButton1Confirmation}')) 
                                           return false;
                                       return submitAndDisable(this, '$resource{i18n.button.Processing}');"  
                                   >
				<!command
getUIComponent(clientId="sharedTableEEForm:sharedTableEE:sharedTableEERowGroup", component=>$attribute{tableRowGroup});	
jbiDeleteSelectedRows(tableRowGroup="$attribute{tableRowGroup}", tableType="#{sessionScope.sharedTableType}", isAlertNeeded=>$session{isJbiAlertNeeded}, alertSummary=>$session{jbiAlertSummary}, alertDetails=>$session{jbiAlertDetails});
jbiIncrementAlertCountIfNeeded(isAlertNeeded='$session{isJbiAlertNeeded}')
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="#{sessionScope.redirectOperationRefresh}?type=#{ShowBean.type}&name=#{ShowBean.name}#$pageSession{anchor}")
				/>				
			       </sun:button>

                               <sun:button id="button2"
				   disabled="#{true}"
                                   primary="#{false}"
                                   rendered='#{sessionScope["hasStateColumn"]}'
                                   styleClass="Btn1_sun4"
                                   value="$resource{i18n.button.Enable}"
                                   >
				<!command
getUIComponent(clientId="sharedTableEEForm:sharedTableEE:sharedTableEERowGroup", component=>$attribute{tableRowGroup});	
jbiSetEnablementForSelectedRows(tableRowGroup="$attribute{tableRowGroup}", tableType="#{sessionScope.sharedTableType}", isEnabled="$boolean{true}", isAlertNeeded=>$session{isJbiAlertNeeded}, alertSummary=>$session{jbiAlertSummary}, alertDetails=>$session{jbiAlertDetails});
jbiIncrementAlertCountIfNeeded(isAlertNeeded='$session{isJbiAlertNeeded}')
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="#{sessionScope.redirectOperationRefresh}?type=#{ShowBean.type}&name=#{ShowBean.name}#$pageSession{anchor}")
				/>
			       </sun:button>

                               <sun:button id="button3"
				   disabled="#{true}"
                                   primary="#{false}"
                                   rendered='#{sessionScope["hasStateColumn"]}'
                                   styleClass="Btn1_sun4"
                                   value="$resource{i18n.button.Disable}"
                                   >
				<!command
getUIComponent(clientId="sharedTableEEForm:sharedTableEE:sharedTableEERowGroup", component=>$attribute{tableRowGroup});	
jbiSetEnablementForSelectedRows(tableRowGroup="$attribute{tableRowGroup}", tableType="#{sessionScope.sharedTableType}", isEnabled="$boolean{false}", isAlertNeeded=>$session{isJbiAlertNeeded}, alertSummary=>$session{jbiAlertSummary}, alertDetails=>$session{jbiAlertDetails});
jbiIncrementAlertCountIfNeeded(isAlertNeeded='$session{isJbiAlertNeeded}')
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="#{sessionScope.redirectOperationRefresh}?type=#{ShowBean.type}&name=#{ShowBean.name}#$pageSession{anchor}")
				/>
			       </sun:button>

                               <sun:image id="actionTopSeparator3"
                                    align="top"
                                    height="$int{18}"
                                    rendered='#{sessionScope["hasStateFilter"]}'
                                    url="/resource/images/jbi/actions_separator.gif"
                                    width="$int{18}"
                                   />

                               <sun:dropDown id="filterStateDropDown"
                                   disabled    = "#{false}" 
                                   forgetValue = "#{true}"
                                   label       = "$resource{i18n.jbi.filter.state.label}" 
                                   labels      = {"$resource{i18n.jbi.filter.state.all}" "$resource{i18n.jbi.filter.state.enabled}" "$resource{i18n.jbi.filter.state.disabled}" "$resource{i18n.jbi.filter.state.no.targets}"}
                                   submitForm  = "#{true}"
                                   value       = "#{ListBean.filterComponentState}"
                                   values      = {"Show All" "Enabled" "Disabled" "No Targets"}
                                   rendered    = '#{sessionScope["hasComponentStateFilter"]}'
                               >
                                   <!command
getUIComponent (clientId="sharedTableEEForm:sharedTableEE:topActionsGroup1:filterStateDropDown", component=>$attribute{filterStateDropDown})
getUIComponentProperty (component="$attribute{filterStateDropDown}", name="value", value=>$attribute{dropDownValue})	
getUIComponentProperty (component="$attribute{filterStateDropDown}", name="values", value=>$attribute{dropDownValues})	
jbiFilterTableComponentState (value="$attribute{dropDownValue}", values="$attribute{dropDownValues}")
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="#{sessionScope.redirectOperationRefresh}?type=#{ShowBean.type}&name=#{ShowBean.name}#$pageSession{anchor}")
                                   />
                               </sun:dropDown>


                               <sun:dropDown id="filterAssemblyStateDropDown"
                                   disabled    = "#{false}" 
                                   forgetValue = "#{true}"
                                   label       = "$resource{i18n.jbi.filter.state.label}" 
                                   labels      = {"$resource{i18n.jbi.filter.state.all}" "$resource{i18n.jbi.filter.state.enabled}" "$resource{i18n.jbi.filter.state.disabled}" "$resource{i18n.jbi.filter.state.no.targets}"}
                                   submitForm  = "#{true}"
                                   value       = "#{ListBean.filterAssemblyState}"
                                   values      = {"Show All" "Enabled" "Disabled" "No Targets"}
                                   rendered    = '#{sessionScope["hasAssemblyStateFilter"]}'
                               >
                               <!-- values are not I18n, labels are -->
                                   <!command
getUIComponent (clientId="sharedTableEEForm:sharedTableEE:topActionsGroup1:filterAssemblyStateDropDown", component=>$attribute{filterStateDropDown})
getUIComponentProperty (component="$attribute{filterStateDropDown}", name="value", value=>$attribute{dropDownValue})	
getUIComponentProperty (component="$attribute{filterStateDropDown}", name="values", value=>$attribute{dropDownValues})	
jbiFilterTableAssemblyState (value="$attribute{dropDownValue}",values="$attribute{dropDownValues}")
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="#{sessionScope.redirectOperationRefresh}?type=#{ShowBean.type}&name=#{ShowBean.name}#$pageSession{anchor}")
                                   />
                               </sun:dropDown>


                               <sun:image id="actionTopSeparator4"
                                    align="top"
                                    height="$int{18}"
                                    rendered='#{sessionScope["hasTypeFilter"]}'
                                    url="/resource/images/jbi/actions_separator.gif"
                                    width="$int{18}"
                                   />


                               <sun:dropDown id="filterActionDropDown"
                                   disabled    = "#{false}" 
                                   forgetValue = "#{true}"
                                   label       = "$resource{i18n.jbi.filter.type.label}" 
                                   labels      = {"$resource{i18n.jbi.filter.type.all}" "$resource{i18n.jbi.filter.type.binding}" "$resource{i18n.jbi.filter.type.engine}"}
                                   submitForm  = "#{true}"
                                   value       = "#{ListBean.filterType}"
                                   values      = {"Show All" "Binding" "Engine"}
                                   rendered    = '#{sessionScope["hasTypeFilter"]}'
                               >
                                   <!command
getUIComponent (clientId="sharedTableEEForm:sharedTableEE:topActionsGroup1:filterActionDropDown", component=>$attribute{filterActionDropDown})
getUIComponentProperty (component="$attribute{filterActionDropDown}", name="value", value=>$attribute{dropDownValue})	
filterTableType (operation="$attribute{dropDownValue}")
setPageSessionAttribute(key="anchor", value="#{sessionScope.isJbiAlertNeeded ? 'list'  : 'show' }")
redirect(page="#{sessionScope.redirectOperationRefresh}?type=#{ShowBean.type}&name=#{ShowBean.name}#$pageSession{anchor}")
                                   />
                               </sun:dropDown>

                         </sun:panelGroup>

                    </facet>

                </sun:table>

</sun:form>

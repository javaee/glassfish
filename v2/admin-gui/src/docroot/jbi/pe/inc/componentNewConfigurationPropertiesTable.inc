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
<!-- jbi/pe/inc/componentNewConfigurationPropertiesTable.inc.inc -->

    <!-- sun:form id="componentNewConfigurationPropertiesTableForm" -->
                <sun:table id="componentConfigurationTable" title="$resource{i18n.jbi.install.AdditionalProperties}" 
                    deselectMultipleButton="$boolean{true}"
                    deselectMultipleButtonOnClick="setTimeout('changeThreeTableButtons2()', 0)"
                    selectMultipleButton="$boolean{true}"
                    selectMultipleButtonOnClick="setTimeout('changeThreeTableButtons2()', 0)"
                    paginateButton="#{false}"
                    paginationControls="#{false}"
                    lite="#{false}" >  
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableId});
        />
                    
      <!facet actionsTop>
                    <sun:panelGroup id="topActionsGroup1">
                        <!afterCreate
                                getClientId(component="$this{component}" clientId=>$page{topActionGroup});
                            />
                        <sun:button id="addJBIComponentPropertyButton" disabled="#{false}" text="$resource{i18n.jbi.button.AddProperty}" >   
                            <!command
                                getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                                addRowToJBIComponentPropertiesTable(TableRowGroup="$attribute{tableRowGroup}", NameList={"name", "value"});
                            />                                 
                        </sun:button>
                        <sun:button id="button1" text="$resource{i18n.jbi.button.DeleteProperties}" disabled="#{true}" primary="#{false}">
                            <!command
                                getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                                jbiGetSelectedTableRowKeys(tableRowGroup="${tableRowGroup}" rowKeys=>$attribute{rowKeys});
                                jbiDeleteTableRows(tableRowGroup="${tableRowGroup}" rowKeys="${rowKeys}");
                                jbiCommitTableRowGroup(tableRowGroup="${tableRowGroup}");  
                            />                                
                        </sun:button>
                    </sun:panelGroup>
                    </facet>       
                  
                    <sun:tableRowGroup id="tableRowGroup"
                         aboveColumnHeader="#{true}"
                         rows="$int{5}"
                         data={"$attribute{tableList}"}
		         selected="#{componentConfig.value.selected}"
                         sourceVar="componentConfig" 
                         >
				         <!afterCreate
				           getClientId(component="$this{component}" clientId=>$page{propertyTableRowGroupId});
				         />
                        <sun:tableColumn selectId="select" rowHeader="$boolean{false}" id="col1">
                            <sun:checkbox id="select" 
                                rendered="#{componentConfig.value.rendered}" 
			        selected="#{componentConfig.value.selected}"
				selectedValue="$boolean{true}"
                                onClick="setTimeout('initAllRows(); changeOneTableButton()', 0)" 
                            />
                        </sun:tableColumn>                    
                        <sun:tableColumn headerText="$resource{i18n.jbi.install.PropertyName}" 
                                         sort="name" rowHeader="$boolean{false}" id="col2">
                            <sun:textField columns="$int{30}" id="col1St" disabled="#{componentConfig.value.disabled}"  
                                    value="#{componentConfig.value.name}" />
                        </sun:tableColumn>
                        <sun:tableColumn headerText="$resource{i18n.jbi.install.PropertyValue}" 
                                         sort="value" rowHeader="$boolean{false}" id="col3">
                            <sun:textField columns="$int{55}" id="col1St" 
                                     text="#{componentConfig.value.value}" />
                        </sun:tableColumn>
                         



                    </sun:tableRowGroup>

                </sun:table>

<!--/sun:form -->

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
<!-- shared/appSingleTargetTable.inc -->

    <sun:title id="title" title="$pageSession{pageTitle}"
        helpText="$pageSession{pageTitleHelp}">
        <!facet pageButtonsTop>
            <sun:panelGroup id="topButtons">
                <sun:button id="saveButton" text="$resource{i18n.button.Save}" rendered="#{hasLB}" >
                    <!command
                    getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});      
                    prepareSuccessfulMsg();
                    saveDisableTimeout(TableRowGroup="${tableRowGroup}");
                    setAttribute(key="selectedRows" value="${selectedRows}");
                    />
                </sun:button>   
            </sun:panelGroup>
        </facet>           
    </sun:title>
    "<br />
                           
<sun:table id="table1" title="$resource{i18n.appTable.applications}" sortPanelToggleButton="#{true}"  
            deselectMultipleButton="$boolean{true}"
            deselectMultipleButtonOnClick="setTimeout('changeButtonAndDropDown()', 0)"
            selectMultipleButton="$boolean{true}"
            selectMultipleButtonOnClick="setTimeout('changeButtonAndDropDown()', 0)" >
         <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableId});
        />
    <!facet actionsTop>
         <sun:panelGroup id="topActionsGroup1">
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{topActionGroup});
            />
            <sun:button id="deployButton" text="$resource{i18n.button.Deploy}">
                    <!command redirect(page="../applications/upload.jsf?appType=application&listPageLink=#{listPage}&target=#{target}&cancelPage=#{listPage}"); />
            </sun:button>

            <sun:button id="button1" text="$resource{i18n.button.Remove}" disabled="#{true}" primary="#{false}"
                    onClick="if (!getConfirm(this,'$resource{i18n.msg.JS.confirmRemoveTarget}')) 
                               return false;
                    return submitAndDisable(this, '$resource{i18n.button.Processing}');"  >
                <!command          
                    getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});   
                    getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                    removeAppTarget(selectedRows="${selectedRows}");
                    setAttribute(key="name" value="$pageSession{target}");
                    navigate(page="#{listPage}");
                />
             </sun:button>

            <sun:dropDown id="dropdown1" submitForm="#{true}"  disabled="#{true}" 
                labels="$attribute{actionLabels}" values="$attribute{actionValues}" >
                <!beforeCreate
                    if( $pageSession{hasLB}){
                        setAttribute(key="actionLabels" value={ "$resource{i18n.common.moreActionDropDown}"
                        "$resource{i18n.button.Enable}"  "$resource{i18n.button.Disable}" 
                        "$resource{i18n.loadBalancer.table.lbEnable}"  "$resource{i18n.loadBalancer.table.lbDisable}" });
                        setAttribute(key="actionValues" value={"" "enable" "disable" "lb-enable" "lb-disable" });
                    }
                    if( !$pageSession{hasLB}){
                        setAttribute(key="actionLabels" value={ "$resource{i18n.common.moreActionDropDown}"
                        "$resource{i18n.button.Enable}"  "$resource{i18n.button.Disable}" });
                        setAttribute(key="actionValues" value={"" "enable" "disable"});
                    }
                />

                <!command
                    getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});   
                    getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                    setAttribute(key="click" value="$this{component}");
                    setAttribute(key="actionValue" value="#{click.selected}")
                    if( ${actionValue}=enable){
                        changeAppEnableForTarget(selectedRows="${selectedRows}" enabled="$boolean{true}" LB="$boolean{false}" );
                    }
                    if( ${actionValue}=disable){
                        changeAppEnableForTarget(selectedRows="${selectedRows}" enabled="$boolean{false}" LB="$boolean{false}");
                    }
                    if( ${actionValue}=lb-enable){
                        changeAppEnableForTarget(selectedRows="${selectedRows}" enabled="$boolean{true}" LB="$boolean{true}");
                    }
                    if( ${actionValue}=lb-disable){
                        changeAppEnableForTarget(selectedRows="${selectedRows}" enabled="$boolean{false}" LB="$boolean{true}");
                    }
                    setAttribute(key="name" value="$pageSession{target}");
                    navigate(page="#{listPage}");
                />
            </sun:dropDown>

             <sun:image id="actionTopSeparator1"
               align="top"
               height="$int{18}"
               url="/theme/com/sun/webui/jsf/suntheme/images/table/actions_separator.gif"
               width="$int{18}"
               />

            <sun:dropDown id="filter" selected="#{filterValue}" label="$resource{i18n.common.filterDropDown}" submitForm="#{true}" immediate="#{true}" 
                    values="${values}"  labels="${labels}">
                <!beforeCreate
                    getAppFilterTypes(labels=>$attribute{labels} values=>$attribute{values});
                />
                <!command
                    setAttribute(key="click" value="$this{component}");
                    setAttribute(key="filterValue" value="#{click.selected}");
                    redirect(page="../$pageSession{listPage}?name=#{target}&filterValue=${filterValue}" );
                />
            </sun:dropDown>
         </sun:panelGroup>
     </facet>

   <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
    <!afterCreate
         getClientId(component="$this{component}" clientId=>$page{tableRowGroupId});
    />
   <sun:tableColumn selectId="select" rowHeader="$boolean{false}" id="col0">
        <sun:checkbox id="select" 
            selected="#{td.value.selected}" 
            selectedValue="$boolean{true}" 
            onClick="setTimeout('initAllRows(); changeButtonAndDropDown()', 0)" 
        />
   </sun:tableColumn>    

    <sun:tableColumn headerText="$resource{i18n.common.name.header}"  sort="name" rowHeader="$boolean{true}" extrahtml="nowrap" id="col1">
        <sun:hyperlink id="link" text="#{td.value.name}" url="#{td.value.link}" />
    </sun:tableColumn>
    
   <sun:tableColumn headerText="$resource{i18n.common.Enabled}"  sort="enabled" rowHeader="$boolean{true}" id="enabled">
        <staticText id="enabled" value="#{td.value.enabled}" />
    </sun:tableColumn>

    <sun:tableColumn rendered="#{hasLB}" headerText="$resource{i18n.loadBalancer.statusColName}"  sort="lbEnabled" rowHeader="$boolean{true}" id="lbEabled">
        <staticText id="enabled" value="#{td.value.lbEnabled}" />
    </sun:tableColumn>

    <sun:tableColumn rendered="#{hasLB}" headerText="$resource{i18n.loadBalancer.timeoutColName}" rowHeader="$boolean{true}" id="timeout">
        <sun:textField id="timeout" columns="$int{10}" text="#{td.value.timeout}" />      
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18n.common.Type}"  sort="appType" rowHeader="$boolean{true}" id="appType">
        <staticText id="appType" value="#{td.value.appType}" />
    </sun:tableColumn>
    
   </sun:tableRowGroup>
  
</sun:table>


           

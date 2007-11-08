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
<!-- shared/resourcesSingleTargetTable.inc -->

    <sun:title id="title" title="$pageSession{pageTitle}"
        helpText="$pageSession{pageTitleHelp}" />
    "<br />
                           
<sun:table id="table1" title="$resource{i18n.resourcesTable.resources}" sortPanelToggleButton="#{true}"   
            deselectMultipleButton="$boolean{true}"
            deselectMultipleButtonOnClick="setTimeout('changeTwoTableButtons()', 0)"
            selectMultipleButton="$boolean{true}"
            selectMultipleButtonOnClick="setTimeout('changeTwoTableButtons()', 0)" >
         <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableId});
        />
    <!facet actionsTop>
         <sun:panelGroup id="topActionsGroup1">
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{topActionGroup});
            />
            <sun:button id="button1" text="$resource{i18n.button.Enable}" disabled="#{true}" >
            <!command 
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});   
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                changeEnableForTarget(selectedRows="${selectedRows}" target="$pageSession{target}" 
                    isServer="$pageSession{isServer}", enabled="$boolean{true}");
                redirect(page="../#{origPage}");
            />
            </sun:button>

            <sun:button id="button2" text="$resource{i18n.button.Disable}" disabled="#{true}" primary="#{false}">
            <!command 
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});   
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                changeEnableForTarget(selectedRows="${selectedRows}" target="$pageSession{target}" 
                    isServer="$pageSession{isServer}", enabled="$boolean{false}");
                redirect(page="../#{origPage}");
            />
            </sun:button>
            
            <sun:dropDown id="actions" selected="#{actionValue}" submitForm="#{true}" immediate="#{true}"
                labels={"$resource{i18n.common.newDropDown}",
                        "$resource{i18n.tree.jdbcResources}",
                        "$resource{i18n.jmsResources.jmsConFact}",
                        "$resource{i18n.jmsResources.jmsDestRes}",
                        "$resource{i18n.tree.javaMailSessions}",
                        "$resource{i18n.tree.customResources}",
                        "$resource{i18n.tree.externalResources}",
                        "$resource{i18n.tree.connectorResources}",
                        "$resource{i18n.tree.adminObjectResources}"}

                values={"",  "jdbcResourceNew.jsf", "jmsConnectionNew.jsf", "jmsDestinationNew.jsf",
                         "javaMailSessionNew.jsf", "customResourceNew.jsf", "externalResourceNew.jsf",
                         "connectorResourceNew.jsf", "adminObjectNew1.jsf" } >
                <!command
                    setAttribute(key="click" value="$this{component}");
                    setAttribute(key="actionValue" value="#{click.selected}")
                    redirect(page="../resourceNode/#{click.selected}?target=#{target}&listPage=#{listPage}" );
                />
            </sun:dropDown>

            <sun:dropDown id="filter" selected="#{filterValue}" label="$resource{i18n.common.filterDropDown}" submitForm="#{true}" immediate="#{true}" 
                    values="${values}"  labels="${labels}">
                <!beforeCreate
                    getResourceFilterTypes(labels=>$attribute{labels} values=>$attribute{values});
                />
                <!command
                    setAttribute(key="click" value="$this{component}");
                    setAttribute(key="filterValue" value="#{click.selected}")
                    redirect(page="../#{origPage}&filterValue=${filterValue}" );
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
            onClick="setTimeout('initAllRows(); changeTwoTableButtons()', 0)" 
        />
   </sun:tableColumn>    

    <sun:tableColumn headerText="$resource{i18n.common.ResourceName}"  sort="name" rowHeader="$boolean{true}" extrahtml="nowrap" id="col1">
        <sun:hyperlink id="link" text="#{td.value.name}" url="#{td.value.link}" />
    </sun:tableColumn>
    
   <sun:tableColumn headerText="$resource{i18n.common.Enabled}"  sort="enabled" rowHeader="$boolean{true}" id="enabled">
        <staticText id="enabled" value="#{td.value.enabled}" />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18n.common.Type}"  sort="resType" rowHeader="$boolean{true}" id="resType">
        <staticText id="resType" value="#{td.value.resType}" />
    </sun:tableColumn>
    
   </sun:tableRowGroup>
  
</sun:table>


           

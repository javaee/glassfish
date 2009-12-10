<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 
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
<!-- applications/lifecyclesTable.inc -->

<sun:table id="deployTable" sortPanelToggleButton="#{false}"  title="$resource{i18n.deployTable.lifeCycle}"
   deselectMultipleButton="$boolean{true}"
    deselectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)"
    selectMultipleButton="$boolean{true}"
    selectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)" >
     <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{tableId});
    />

<!facet actionsTop>
     <sun:panelGroup id="topActionsGroup1">
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{topActionGroup});
        />
        <sun:button id="deployButton" text="$resource{i18n.button.New}" primary="#{true}">
                <!command gf.redirect(page="#{pageSession.nextPageLink}") />
        </sun:button>
        <sun:button id="button1" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}"
                onClick="if ( getConfirm(this,'$pageSession{confirmDeleteMsg}') )
                    { return submitAndDisable(this, '$resource{i18n.button.Processing}');}
                    else {return false;} " >
            <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                deleteChildren(objectNameStr="#{pageSession.objectNameStr}", type="#{pageSession.childType}", selectedRows="${selectedRows}");
                deleteChildren(objectNameStr="#{pageSession.objectNameStr2}", type="#{pageSession.childType2}", selectedRows="${selectedRows}");
                gf.navigate(page="#{listPageLink}");
            />
         </sun:button>
        <sun:button id="button2" text="$resource{i18n.button.Enable}" disabled="#{true}" primary="#{false}"
                onClick="return submitAndDisable(this, '$resource{i18n.button.Processing}');" >
        <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                changeLifecycleStatus(selectedRows="${selectedRows}"  enabled="#{true}")
                gf.navigate(page="#{listPageLink}");
            />
            </sun:button>
        <sun:button id="button3" text="$resource{i18n.button.Disable}" disabled="#{true}" primary="#{false}"
            onClick="return submitAndDisable(this, '$resource{i18n.button.Processing}');" >
        <!command
                getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
                getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                changeLifecycleStatus(selectedRows="${selectedRows}"  enabled="#{false}")
                gf.navigate(page="#{listPageLink}");
            />
        </sun:button>
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
                onClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\'); admingui.table.initAllRows(\\\\\'#{pageSession.tableId}\\\\\');', 0)"
            />
    </sun:tableColumn>

    <sun:tableColumn headerText="$resource{i18n.deployTable.colName}" sort="Name" rowHeader="$boolean{true}" id="col1">
        <sun:hyperlink id="link" text="#{td.value.Name}" url="#{editLink}?appName=#{td.value.Name}" />
    </sun:tableColumn>

    <sun:tableColumn  headerText="$resource{i18n.deployTable.colEnabled}" sort="enabled" rowHeader="$boolean{true}" id="col2">
        <sun:image id="enCol" value="#{td.value.enableURL}" />
    </sun:tableColumn>


    <sun:tableColumn headerText="$resource{i18n.deployTable.colLoadOrder}"  sort="loadOrder"  rowHeader="$boolean{true}" id="col3">
        <staticText id="loadOrder" value="#{td.value.loadOrder}" />
    </sun:tableColumn>

    <sun:tableColumn  headerText="$resource{i18n.deployTable.classnameCol}" sort="className" rowHeader="$boolean{true}" id="col4">
        <sun:staticText escape="true" id="className" value="#{td.value.className}" />
    </sun:tableColumn>

<!--
    <sun:tableColumn rendered="$pageSession{showDetail}"
        headerText="$resource{i18n.common.TargetTab}"  
        rowHeader="$boolean{true}" id="colTarget">
        <event>
            <!beforeEncode
                getTargetStatus(appName="#{td.value.name}", forApp="$boolean{true}", result=>$page{listOfTargets});
                />				    
        </event>
        <foreach key="target" list="#{listOfTargets}">
            <staticText id="image" value="#{target.image}" />"&nbsp; 
            <sun:hyperlink url="#{target.targetURL}" text="#{target.target}" />
            "<br />
        </foreach>
    </sun:tableColumn>
-->
  </sun:tableRowGroup>
</sun:table>


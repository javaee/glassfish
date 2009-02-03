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
<!-- webApp/deployTable.inc -->

<sun:table id="deployTable" title="#{tableTitle}" sortPanelToggleButton="#{false}" 
       deselectMultipleButton="$boolean{true}"
        deselectMultipleButtonOnClick="setTimeout('changeThreeTableButtons()', 0)"
        selectMultipleButton="$boolean{true}"
        selectMultipleButtonOnClick="setTimeout('changeThreeTableButtons()', 0)" >
         <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableId});
        />

    <!facet actionsTop>
         <sun:panelGroup id="topActionsGroup1">
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{topActionGroup});
            />
            <sun:button id="deployButton" text="$resource{i18n.button.Deploy}" primary="#{true}">
                    <!command redirect(page="#{request.contextPath}/web/webApp/upload.jsf?cancelPage=#{listPageLink}&appType=#{appType}"); />
            </sun:button>
            <sun:button id="button1" text="$resource{i18n.button.Undeploy}" disabled="#{true}" primary="#{false}"
                    onClick="javascript: return submitAndDisable(this, '$resource{i18n.button.Processing}');">
                <!command 
                    getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup}); 
                    getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                    undeploy(selectedRows="${selectedRows}" appType="#{appType}");
                    navigate(page="#{listPageLink}");
                />
             </sun:button>
            <sun:button id="button2" text="$resource{i18n.button.Enable}" disabled="#{true}" primary="#{false}"
                    onClick="javascript: submitAndDisable(this, '$resource{i18n.button.Processing}');" >
            <!command 
                    getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});      
                    getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                    changeAppStatus(selectedRows="${selectedRows}" appType="#{appType}" enabled="#{true}")
                    navigate(page="#{listPageLink}");
                />
                </sun:button>
            <sun:button id="button3" text="$resource{i18n.button.Disable}" disabled="#{true}" primary="#{false}"
                onClick="javascript: submitAndDisable(this, '$resource{i18n.button.Processing}');" >
            <!command 
                    getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});      
                    getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                    changeAppStatus(selectedRows="${selectedRows}" appType="#{appType}" enabled="#{false}")
                    navigate(page="#{listPageLink}");
                />
            </sun:button>

            <sun:image id="actionTopSeparator1" rendered="$session{supportCluster}"
               align="top"
               height="$int{18}"
               url="/theme/com/sun/webui/jsf/suntheme/images/table/actions_separator.gif"
               width="$int{18}"
               />

            <!--
            <sun:dropDown rendered="$session{supportCluster}" id="view" selected="$pageSession{viewValue}" label="$resource{i18n.common.viewDropDown}" submitForm="#{true}" immediate="#{true}" 
                    labels={"$resource{i18n.common.Summary}", "$resource{i18n.common.Detailed}" }
                    values={"summary", "detail"} >
                <!command
                    setAttribute(key="click" value="$this{component}");
                    setAppViewKey(key="$pageSession{viewKey}" selectedValue="#{click.selected}");
                    navigate(page="#{listPageLink}");
                />
            </sun:dropDown>
            -->
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
                onClick="setTimeout('initAllRows(); changeThreeTableButtons()', 0)" 
            />
    </sun:tableColumn>    
        
    <sun:tableColumn headerText="$resource{i18n.deployTable.colName}" sort="name" rowHeader="$boolean{true}" id="col1">
        <sun:hyperlink id="link" text="#{td.value.name}" 
            url="#{editLink}?appName=#{td.value.name}" />
    </sun:tableColumn>

    <sun:tableColumn rendered="$session{supportCluster}" headerText="$resource{i18n.deployTable.colStatus}" sort="enabled" rowHeader="$boolean{true}" id="col2">
        <sun:hyperlink rendered="$session{supportCluster}" id="targetLink" text="#{td.value.enabled}" url="#{targetLink}?appName=#{td.value.name}" />
    </sun:tableColumn>

    <sun:tableColumn rendered="$session{notSupportCluster}" headerText="$resource{i18n.deployTable.colEnabled}" sort="enabled" rowHeader="$boolean{true}" id="col22">
        <staticText id="typeCol" value="#{td.value.enabled}" />
    </sun:tableColumn>
    
    <sun:tableColumn headerText="$resource{i18n.deployTable.colContextRoot}"  sort="contextRoot" rendered="#{hasContextRoot}" rowHeader="$boolean{true}" id="col3">
        <staticText id="typeContextRoot" value="#{td.value.contextRoot}" />
    </sun:tableColumn>

    <!-- TODO-V3
        <sun:tableColumn rendered="$pageSession{showDetail}"
            headerText="$resource{i18n.common.TargetTab}"  
            rowHeader="$boolean{true}" id="colTarget">
            <event>
                <!beforeEncode
                    getTargetStatus(appName="#{td.value.name}", forApp="$boolean{true}" ,result=>$page{listOfTargets});
                 />				    
            </event>
            <foreach key="target" list="#{listOfTargets}">
		<staticText id="image" value="#{target.image}" />"&nbsp; 
                <sun:hyperlink url="#{target.targetURL}" text="#{target.target}" />
                "<br />
            </foreach>
        </sun:tableColumn> 
        -->
   

    <sun:tableColumn headerText="$resource{i18n.ComponentTable.colAction}" embeddedActions="#{true}" rowHeader="$boolean{true}" id="col4">
        <!if $session{notSupportCluster}>
            <sun:hyperlink id="link" rendered="#{td.value.hasLaunch}" text="$resource{i18n.deployTable.launch}" 
                onClick="window.open('#{td.value.launchLink}','ViewerWindow','width='+.75*screen.width+',height='+.75*screen.height+',top='+.1*screen.height+',left='+.1*screen.width+',toolbar=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes,directories=yes,location=yes');document.Form.target='_self';" />
        </!if>
        <!if $session{supportCluster}>
            <sun:hyperlink id="clusterLink" rendered="#{td.value.hasLaunch}" text="$resource{i18n.deployTable.launch}" 
                onClick="var win=window.open('webApplicationLinks.jsf?appID=#{td.value.name}&contextRoot=#{td.value.contextRoot}','ViewerWindow','width='+.75*screen.width+',height='+.75*screen.height+',top='+.1*screen.height+',left='+.1*screen.width+',toolbar=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes,directories=yes,location=yes');win.focus(); return false;" />
        </!if>
        // TODO: These resources should probably be in a web plugin bundle and not the core
        <sun:hyperlink id="link2" text="$resource{i18n.deployTable.redeploy}" 
            url="#{request.contextPath}/web/webApp/redeploy.jsf?appName=#{td.value.name}&appType=$pageSession{appType}&nextPage=#{listPageLink}" />
        <sun:hyperlink id="restartLink" text="$resource{web.restart.restart}">
            <!command
                prepareSuccessfulMsg();
                restartApplication(appName="#{td.value.name}");
            />
        </sun:hyperlink>
        
    </sun:tableColumn>
    
  </sun:tableRowGroup>
  
  
</sun:table>

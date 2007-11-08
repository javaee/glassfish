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
<!-- cluster/clustersTable.inc -->
<sun:table id="clusterTable" title="$resource{i18n.cluster.TableTitle}" 
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
        <sun:button id="newButton" text="$resource{i18n.button.New}" >
            <!command redirect(page="clusterNew.jsf"); />
        </sun:button> 
        
        <sun:button id="button1" text="$resource{i18n.button.Delete}" disabled="#{true}" primary="#{false}"
                    onClick="return getConfirm(this,'$resource{i18n.msg.JS.confirmDeleteCluster}');">
            <!command 
            getUIComponent(clientId="$pageSession{clustersTableRowGroupId}", component=>$attribute{tableRowGroup});      
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
            deleteSelectedClusters(selectedRows="${selectedRows}");
            setAttribute(key="selectedRows" value="${selectedRows}");
            redirect(page="clusters.jsf");
            />            
        </sun:button>
        <sun:button id="button2" text="$resource{i18n.button.startCluster}" disabled="#{true}" primary="#{false}"
            onClick="if ( getConfirm(this,'$resource{i18n.msg.JS.confirmStartClusters}') )
                { return submitAndDisable(this, '$resource{i18n.button.Processing}');} 
                else {return false;} " >
            <!command 
            getUIComponent(clientId="$pageSession{clustersTableRowGroupId}", component=>$attribute{tableRowGroup});      
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
            selectedClustersAction(start="$boolean{true}" selectedRows="${selectedRows}");
            setAttribute(key="selectedRows" value="${selectedRows}");
            getUIComponent(clientId="$pageSession{filterId}", component=>$attribute{clustersFilterDropDown});	
            getUIComponentProperty(component="$attribute{clustersFilterDropDown}", name="value", value=>$page{clustersFilterDropDownValue});	
            redirect(page="#{clustersFilterDropDownValue}");
            />            
        </sun:button>
        <sun:button id="button3" text="$resource{i18n.button.stopCluster}" disabled="#{true}" primary="#{false}"
                onClick="if ( getConfirm(this,'$resource{i18n.msg.JS.confirmStopClusters}') )
                        { return submitAndDisable(this, '$resource{i18n.button.Processing}');} 
                        else {return false;} " >
            <!command 
            getUIComponent(clientId="$pageSession{clustersTableRowGroupId}", component=>$attribute{tableRowGroup});      
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
            selectedClustersAction(start="$boolean{false}" selectedRows="${selectedRows}");
            setAttribute(key="selectedRows" value="${selectedRows}");
            getUIComponent(clientId="$pageSession{filterId}", component=>$attribute{clustersFilterDropDown});	
            getUIComponentProperty(component="$attribute{clustersFilterDropDown}", name="value", value=>$page{clustersFilterDropDownValue});	
            redirect(page="#{clustersFilterDropDownValue}");
             />            
        </sun:button>

        <sun:image id="actionTopSeparator1"
               align="top"
               height="$int{18}"
               url="/theme/com/sun/webui/jsf/suntheme/images/table/actions_separator.gif"
               width="$int{18}"
               />

        <sun:dropDown id="clustersFilterDropDown"
                                  disabled="#{false}" 
                                  forgetValue="#{false}"
                                  label="$resource{i18n.common.viewDropDown}" 
                                  labels={"$resource{i18n.common.Detailed}", "$resource{i18n.common.Summary}"}
                                  rendered='#{true}'
                                  submitForm="#{true}"
                                  selected="#{clustersFilterSelectedValue}"
                                  values={"clustersDetailed.jsf", "clusters.jsf"}
                                    >
              <!command
                getUIComponent(clientId="propertyForm:clusterTable:topActionsGroup1:clustersFilterDropDown", component=>$attribute{clustersFilterDropDown});	
                getUIComponentProperty(component="$attribute{clustersFilterDropDown}", name="value", value=>$page{clustersFilterDropDownValue});	
                redirect(page="#{clustersFilterDropDownValue}");
              />
              <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{filterId});
               />
          </sun:dropDown>    
        
    </sun:panelGroup>
    </facet>
    
    <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
        <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{clustersTableRowGroupId});
       />
 
        <sun:tableColumn selectId="select" rowHeader="$boolean{false}" id="col0">
            <sun:checkbox id="select" 
                selected="#{td.value.selected}" 
                selectedValue="$boolean{true}" 
                onClick="setTimeout('initAllRows(); changeThreeTableButtons()', 0)" 
            />
        </sun:tableColumn>    
        
        <sun:tableColumn headerText="$resource{i18n.common.PropertyName}" rowHeader="$boolean{true}" id="col1">
            <event>
                <!beforeEncode
                    setPageSessionAttribute(key="clusterName" value="#{td.value.name}");
                    />				    
            </event>
            <sun:hyperlink id="link" text="#{td.value.name}" 
                           url="/cluster/clusterGeneral.jsf?clusterName=#{td.value.name}" />
        </sun:tableColumn>  

        <sun:tableColumn 
            headerText="$resource{i18n.common.lbTableColumn}"
            rowHeader="$boolean{true}" id="lb" >
            <event>
                <!beforeEncode
                    getLBforTarget(targetName="#{clusterName}", isCluster="$boolean{true}" result=>$page{listOfLBs});
                />				    
            </event>
            <foreach key="lb" list="#{listOfLBs}">
                <sun:hyperlink url="/loadBalancer/loadBalancerGeneral.jsf?lbName=#{lb.lbName}" text="#{lb.lbName}" />
                "<br />
            </foreach>
        </sun:tableColumn> 

 
        <sun:tableColumn 
            headerText="$resource{i18n.common.Configuration}" rowHeader="$boolean{true}" 
            id="col2">
            <sun:hyperlink id="configlink" text="#{td.value.config}"                           
                           url="/configuration/configuration.jsf?configName=#{td.value.config}" />
        </sun:tableColumn>
         <sun:tableColumn 
            headerText="$resource{i18n.cluster.InstancesTab}"  rendered="#{!isSummary}" 
            rowHeader="$boolean{true}" id="col3">
            <event>
                <!beforeEncode
                    getClusteredInstances(ClusterName="#{clusterName}", result=>$page{listOfInstances});
                    />				    
            </event>
            <foreach key="instance" list="#{listOfInstances}">
                <sun:hyperlink url="/cluster/clusterInstanceGeneral.jsf?clusterName=#{clusterName}&instanceName=#{instance.name}" text="#{instance.name}" />
                "&nbsp;
                <staticText id="colStopped" value="#{instance.image}" /> 
                "<br />
            </foreach>
        </sun:tableColumn> 
       <sun:tableColumn 
            headerText="$resource{i18n.nodeAgent.colInstances}"  rendered="#{isSummary}" 
            rowHeader="$boolean{true}" id="col4">
             <event>
                <!beforeEncode
                    getClusteredInstances(ClusterName="#{clusterName}", result=>$page{listOfInstances});
                    getClusteredInstancesStatus(Instances="#{listOfInstances}", Stopped=>$attribute{stopped}, Stopping=>$attribute{stopping}, Failed=>$attribute{failed}, Starting=>$attribute{starting}, Running=>$attribute{running}, Restart=>$attribute{restart});
                    />				    
            </event>
            <staticText id="colStopped" value="#{stopped}" />
            <staticText id="colStopping" value="#{stopping}" />
            <staticText id="colFailed" value="#{failed}" />
            <staticText id="colStarting" value="#{starting}" />
            <staticText id="colRunning" value="#{running}" />
        </sun:tableColumn>
        <sun:tableColumn 
            headerText="$resource{i18n.cluster.ColInstancesRestart}"  rendered="#{isSummary}" 
            rowHeader="$boolean{true}" id="col5">
            <staticText id="colRestart" value="#{restart}" 
            />
        </sun:tableColumn>     
    </sun:tableRowGroup>
    
</sun:table>    

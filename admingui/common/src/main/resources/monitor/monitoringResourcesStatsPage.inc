<!--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.

    The contents of this file are subject to the terms of either the GNU
    General Public License Version 2 only ("GPL") or the Common Development
    and Distribution License("CDDL") (collectively, the "License").  You
    may not use this file except in compliance with the License.  You can
    obtain a copy of the License at
    https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
    or packager/legal/LICENSE.txt.  See the License for the specific
    language governing permissions and limitations under the License.

    When distributing the software, include this License Header Notice in each
    file and include the License file at packager/legal/LICENSE.txt.

    GPL Classpath Exception:
    Oracle designates this particular file as subject to the "Classpath"
    exception as provided by Oracle in the GPL Version 2 section of the License
    file that accompanied this code.

    Modifications:
    If applicable, add the following below the License Header, with the fields
    enclosed by brackets [] replaced by your own identifying information:
    "Portions Copyright [year] [name of copyright owner]"

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

<!-- monitor/monitoringStatsPage.inc -->

<event>
    <!beforeCreate
	gf.getChildrenNamesList(endpoint="#{pageSession.monitorURL}/resources", result="#{pageSession.poolsList}");
        gf.getMonitoringPools(endpoint="#{sessionScope.REST_URL}/resources",
            poolNames = "#{pageSession.poolsList}",
            jdbcList="#{pageSession.jdbcList}",
            firstJdbc="#{pageSession.firstJdbc}",
            connectorList="#{pageSession.connectorList}",
            firstConnector="#{pageSession.firstConnector}");
        addToMonitorList(oldList="#{pageSession.jdbcList}", newList="#{pageSession.connectorList}"  result="#{pageSession.resourceList}");
        populateResourceMonitorDropDown(ResourceList="#{resourceList}", MonitorList=>$attribute{monitorList} FirstItem=>$attribute{firstVal});
        if(!#{viewVal}) {
            setAttribute(key="viewVal" value="#{firstVal}");
        }
        calculateHelpUrl(pluginId="#{pluginId}", helpKey: "$resource{help_web.monitorResources}", url="#{olhLink}");
    />
</event>    

 <sun:title id="propertyContentPage" title="$resource{i18nc.resourcesMonitor}"
     helpText="$resource{i18nc.monitoring.webContainer.PageHelp}">
     <!facet pageButtonsTop>
         <sun:panelGroup id="topButtons">
             <sun:button id="refreshButton" 
                         text="$resource{i18n.button.Refresh}">
                  <!command
                      setAttribute(key="viewVal" value="#{viewVal}");
                      setAttribute(key="monitorList" value="#{monitorList}");
                      if ("$pageSession{encodedInstanceName}") {
                          setAttribute(key="instanceName" value="$pageSession{encodedInstanceName}");
                      }
                      if ("$pageSession{encodedClusterName}") {
                          setAttribute(key="clusterName" value="$pageSession{encodedClusterName}");
                      }
                      gf.navigate(page="#{pageSession.selfPage}");
                  />              
             </sun:button>
         </sun:panelGroup>
     </facet> 
#include "/common/shared/nameSection.inc"
     <sun:propertySheet id="propertySheet">
         <sun:propertySheetSection id="viewPropertySection">
             <sun:property id="VsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}">
                 <sun:dropDown id="View" label="$resource{i18nc.monitoring.View} : " selected="#{viewVal}" items="$attribute{monitorList}" submitForm="#{true}" immediate="#{true}" >
                     <!command
                         setAttribute(key="click" value="$this{component}");
                         setAttribute(key="viewVal" value="#{click.selected}");
                         setAttribute(key="monitorList" value="#{monitorList}");
                         if ("$pageSession{encodedInstanceName}") {
                            setAttribute(key="instanceName" value="$pageSession{encodedInstanceName}");
                        }
                        if ("$pageSession{encodedClusterName}") {
                            setAttribute(key="clusterName" value="$pageSession{encodedClusterName}");
                        }
                         gf.navigate(page="#{pageSession.selfPage}");
                     />
                 </sun:dropDown>
             </sun:property>
         </sun:propertySheetSection>
     </sun:propertySheet>   
                   
     <!-- Table .... -->
     <sun:table id="resourcesTable" itemsText="$resource{i18nc.monitoring.statistics}"   style="padding: 10pt" title="$resource{i18nc.monitoring}"  >
         <!afterCreate
             getClientId(component="$this{component}" clientId=>$page{tableId});
         />
   
         <sun:tableRowGroup id="jdbcStats" rendered="#{hasStatsJdbc}" data={"$attribute{jdbcStatsList}"}  sourceVar="td" headerText="$resource{i18nc.monitoring.JDBC} : #{viewVal}"
             groupToggleButton="$boolean{true}" collapsed="$boolean{false}" aboveColumnHeader="$boolean{true}" emptyDataMsg="$resource{i18nc.monitoring.webContainer.NoStats}">
             <!beforeCreate
                 isPool(poolName="#{viewVal}", endpoint="#{sessionScope.REST_URL}/resources/jdbc-connection-pool", result=>$attribute{isJdbcPool});
                 if (#{isJdbcPool}) {
                     getStats(endpoint="#{pageSession.monitorURL}/resources/#{viewVal}", result=>$attribute{jdbcStatsList}  hasStats=>$attribute{hasStatsJdbc});
                 }
                 if (!#{isJdbcPool}) {
                     setAttribute(key="hasStatsJdbc" value="#{false}");
                 }
             />
#include "/common/monitor/monitoringTableRows.inc"
             "<br/>
         </sun:tableRowGroup>
         <sun:tableRowGroup id="connectionPoolStats" rendered="#{hasStatsConnectionPool}" data={"$attribute{connectionPoolStatsList}"}  sourceVar="td" headerText="$resource{i18nc.monitoring.ConnectorConnectionPool} : #{viewVal}"
             groupToggleButton="$boolean{true}" collapsed="$boolean{false}" aboveColumnHeader="$boolean{true}" emptyDataMsg="$resource{i18nc.monitoring.webContainer.NoStats}">
             <!beforeCreate
                 isPool(poolName="#{viewVal}", endpoint="#{sessionScope.REST_URL}/resources/connector-connection-pool", result=>$attribute{isConnectorPool});
                 if (#{isConnectorPool}) {
                     getStats(endpoint="#{pageSession.monitorURL}/resources/#{viewVal}", result=>$attribute{connectionPoolStatsList}  hasStats=>$attribute{hasStatsConnectionPool});
                 }
                 if (!#{isConnectorPool}) {
                    setAttribute(key="hasStatsConnectionPool" value="#{false}");
                 }
             />
#include "/common/monitor/monitoringTableRows.inc"
        "<br/>
    </sun:tableRowGroup>
</sun:table>
</sun:title>
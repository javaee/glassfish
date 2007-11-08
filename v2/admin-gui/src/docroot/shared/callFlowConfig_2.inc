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
<!-- appServer/callFlowConfig_2.inc -->


#include "alertMsg.inc"
                <!-- Page Title -->
                <sun:title id="title" title="$resource{i18n.callFlowConfig.pageTitle}"
                    helpText="$resource{i18n.callFlowConfig.pageTitleHelp}">
                    <sun:button id="clear" disabled="#{enabled}" text="$resource{i18n.button.ClearData}" style="margin-left: 8pt" 
                        onClick="if (! getConfirm(this, '$resource{i18n.msg.JS.confirmClearCallFlowData}')) 
                                        return false;
                                return submitAndDisable(this, '$resource{i18n.button.Processing}');"  >
                        <!command
                            clearCallFlowData(instanceName="#{instanceName}");
                            redirect(page="#{actualLink}?instanceName=#{instanceName}");
                        />
                        
                    </sun:button>
                    <!facet pageButtonsTop>
                      <sun:panelGroup id="topButtons">
                        <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}" 
                                onClick="return submitAndDisable(this, '$resource{i18n.button.Processing}');">  
                          <!command
                            prepareSuccessfulMsg();
                            saveCallFlowConfigInfo(instanceName="#{instanceName}", enabled="#{enabled}", host="${host}", user="${user}");
                            redirect(page="#{actualLink}?instanceName=#{instanceName}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                          />
                        </sun:button>
                        <sun:button id="refreshButton" immediate="#{true}" primary="#{false}" text="$resource{i18n.button.Refresh}" 
                                    onClick="return submitAndDisable(this, '$resource{i18n.button.Processing}');"> 
                            <!command
                                redirect(page="#{actualLink}?instanceName=#{instanceName}");
                            />
                        </sun:button>
                      </sun:panelGroup>
                    </facet>       
                 </sun:title>
                    <!-- PropertySheet .... -->
                    <sun:propertySheet id="propertySheet" label="$resource{i18n.common.Configuration}">			 
                        <sun:propertySheetSection id="section1" rendered="#{hasMonitor}"  label="$resource{i18n.common.Configuration}" >
                            <sun:property id="statusProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.callFlow.callFlowMonitoring}">
                                <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{enabled}" />  
                                "<br /><br />
                           </sun:property> 
                           <sun:property id="filterProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.callFlow.Filters}" >
                                <sun:staticText id="filter" text="$resource{i18n.callFlow.FiltersHelp}"/> 
                                "<br /><br />
                            </sun:property> 
                            
                            <sun:property id="hostProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.callFlow.clientHost}" helpText="$resource{i18n.callFlow.clientHostHelp}">
                                <sun:textField id="host" text="#{host}" columns="$int{55}" /> 
                            </sun:property> 

                            <sun:property id="principalProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.callFlow.userID}" helpText="$resource{i18n.callFlow.userIDHelp}">
                                <sun:textField id="principal" text="#{user}" columns="$int{55}" /> 
                            </sun:property>   

                        </sun:propertySheetSection>

                        </sun:propertySheet> 
#include "appServer/callFlowDataTable.inc"
	<sun:hidden id="helpKey" value="${helpKey}" />
            </sun:form>
        </sun:body>
#include "changeButtonsJS.inc"
    </sun:html>
</sun:page>


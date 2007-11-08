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
                    <!-- PropertySheet .... -->
                    <sun:propertySheet id="propertySheet">
                         <sun:propertySheetSection id="propertySectionTarget" rendered="#{supportCluster}">
                            <sun:property id="domainProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.diagReport.rb.GenerateReportForLabel}">
                                <sun:radioButton 
                                    id="domain"
                                    label="$resource{i18n.diagReport.rbOption.EntireDomain}" 
                                    name="targetType"
                                    onClick="javascript: disableTargetRB(); return true"
                                    onMouseMove="javascript: disableTargetRB(); return true"
                                    onFocus="javascript: disableTargetRB(); return true"    
                                    selected="domain"
                                    selectedValue="domain"/>
                                 <sun:hidden id="target" value="#{requestScope.target}"/>
                                 <sun:hidden id="domainHidden" value="$attribute{domain}">
                                    <!beforeCreate
                                        getDomainConfigMBean(
                                            DomainConfigMBean=>$attribute{domainConfig});
                                        getAMXPropertyValue(
                                            MBean="$attribute{domainConfig}"
                                            PropName="administrative.domain.name"
                                            PropValue=>$attribute{domain}); 
                                    />
                                 </sun:hidden>
                            </sun:property>
                            <sun:property id="clusterProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" >
                                <sun:radioButton 
                                    id="cluster" 
                                    label="$resource{i18n.common.Cluster}"
                                    name="targetType"  
                                    onClick="javascript: disableTargetRB(); return true"
                                    onMouseMove="javascript: disableTargetRB(); return true"
                                    onFocus="javascript: disableTargetRB(); return true"                                      
                                    selectedValue="cluster" />
                                <sun:dropDown 
                                    id="clusterDropDown"
                                    disabled="#{true}"
                                    items="$attribute{clusterOptions}">
                                    <!beforeCreate
                                        getClusters(Clusters=>$attribute{clusters});
                                        getSunOptions(
                                            labels="$attribute{clusters}"
                                            values="$attribute{clusters}"
                                            options=>$attribute{clusterOptions});
                                    />
                                </sun:dropDown>
                            </sun:property>
                            <sun:property id="instanceProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" >
                                <sun:radioButton 
                                    id="instance" 
                                    name="targetType"  
                                    label="$resource{i18n.common.Instance}"
                                    onClick="javascript: disableTargetRB(); return true"
                                    onMouseMove="javascript: disableTargetRB(); return true"
                                    onFocus="javascript: disableTargetRB(); return true"    
                                    selectedValue="instance" />
                                 <sun:dropDown 
                                    id="instanceDropDown"
                                    disabled="#{true}"
                                    items="$attribute{instanceOptions}">
                                    <!beforeCreate
                                        getInstances(Instances=>$attribute{instances});
                                        getSunOptions(
                                            labels="$attribute{instances}"
                                            values="$attribute{instances}"
                                            options=>$attribute{instanceOptions});
                                    />
                                 </sun:dropDown>
                            </sun:property>
                            <sun:property id="nodeAgentProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" >
                                <sun:radioButton 
                                    id="nodeAgent" 
                                    label="$resource{i18n.common.NodeAgent}" 
                                    name="targetType"
                                    onClick="javascript: disableTargetRB(); return true"
                                    onMouseMove="javascript: disableTargetRB(); return true"
                                    onFocus="javascript: disableTargetRB(); return true"    
                                    selectedValue="nodeagent" />
                                <sun:dropDown
                                    id="nodeAgentDropDown"
                                    disabled="#{true}"
                                    items="$attribute{nodeAgentOptions}">
                                    <!beforeCreate
                                        getNodeAgents(NodeAgents=>$attribute{nodeAgents});
                                        getSunOptions(
                                            labels="$attribute{nodeAgents}"
                                            values="$attribute{nodeAgents}"
                                            options=>$attribute{nodeAgentOptions});
                                    />
                                </sun:dropDown>
                            </sun:property> 
                         </sun:propertySheetSection>
                    </sun:propertySheet>
                         <sun:propertySheet>
                        <!-- Text Field section -->               
                        <sun:propertySheetSection id="propertySectionTextField">
                            <sun:property id="descriptionProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
                                <sun:textField 
                                    id="descripton"
                                    columns="$int{80}"
                                    maxLength="$int{512}"
                                    text="#{requestScope.description}"/> 
                            </sun:property>                       
                            <sun:property id="bugIdsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.diagReport.BugIDs}" helpText="$resource{i18n.diagReport.BugIDsHelp}">                           
                                <sun:textField id="bugIds" text="#{requestScope.bugIds}" />  
                            </sun:property>                  
                            <sun:property id="logStartDateProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.diagReport.LogStartDate}" >                           
                                <!-- minDate is 1 year ago -->
                                <!-- TODO - maxDate should be logEndDate -->  
                                <sun:calendar 
                                    id="logStartDate" 
                                    minDate="$attribute{startMinDate}"
                                    maxDate="$attribute{currentDate}"
                                    value="#{requestScope.logStartDate}">
                                    <!beforeCreate
                                        calendarAdd(
                                            Field="$int{1}", 
                                            Amount="$int{-1}",
                                            Date=>$attribute{startMinDate});
                                        calendarAdd(
                                            Field="$int{1}",
                                            Amount="$int{0}",
                                            Date=>$attribute{currentDate});
                                    />
        							<!afterCreate
            							getClientId(component="$this{component}" clientId=>$page{logStartDateId});
            							getDatePattern(calendarComponent="$this{component}" pattern=>$page{pattern});
        							/>
                                </sun:calendar>
                            </sun:property>  
                            <sun:property id="logEndDateProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.diagReport.LogEndDate}" >                           
                              <!-- TODO - minDate should be logStartDate  -->
                               <sun:calendar 
                                    id="logEndDate" 
                                    minDate="$attribute{startMinDate}"  
                                    maxDate="$attribute{currentDate}"
                                    value="#{logEndDate}">
        							<!afterCreate
            							getClientId(component="$this{component}" clientId=>$page{logEndDateId});
        							/>
                                </sun:calendar>
                            </sun:property>
 
                        </sun:propertySheetSection>

                    </sun:propertySheet>    
                "<br>
                <sun:table id="confPropTable" title="$resource{i18n.diagReport.ConfidentialProps}">
                    <sun:tableRowGroup id="rowGroup1" data={"$attribute{confProps}"} sourceVar="td">
                        <sun:tableColumn headerText="$resource{i18n.diagReport.ConfidentialProps}" rowHeader="$boolean{true}" id="col1">
                            <staticText id="confProp" value="#{td.value.prop}" />
                        </sun:tableColumn>
                        <!beforeCreate
                            getConfidentialProps(
                                ConfidentialProps=>$attribute{confProps});
                        />
                   </sun:tableRowGroup>
                </sun:table>

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
<!-- shared/loggingLevel_2.inc -->

#include "alertMsg.inc"
<!-- Page Title -->
<sun:title id="propertyContentPage" title="$resource{i18n.log.LogLevelsPageTitle}" 
    helpText="$resource{i18n.log.LogLevelsPageHelp}">
    <sun:button id="loadDefaults" style="margin-left: 8pt"  primary="#{false}" text="$resource{i18n.button.LoadDefaults}" >
        <!command
            getDefaultServerModuleLogLevels(ConfigName="#{configName}", Admin=>$attribute{admin}, 
                Classloader=>$attribute{classLoader}, Configuration=>$attribute{configuration}, 

                Connector=>$attribute{connector}, Corba=>$attribute{corba}, Javamail=>$attribute{javamail}, 
                Deployment=>$attribute{deployment}, Jaxr=>$attribute{jaxr}, Jaxrpc=>$attribute{jaxrpc}, 
                Jms=>$attribute{jms}, Jta=>$attribute{jta}, Jts=>$attribute{jts}, MDB=>$attribute{mdb}, 
                Root=>$attribute{root}, Saaj=>$attribute{saaj}, Security=>$attribute{security}, SelfManagement=>$attribute{selfManagement}, 
                Server=>$attribute{server}, Util=>$attribute{util}, Verifier=>$attribute{verifier}, WEB=>$attribute{web}, 
                Naming=>$attribute{naming}, EJB=>$attribute{ejb}, PersistenceLogLevel=>$attribute{logLevelPersistence},
                NodeAgent=>$attribute{nodeAgent}, Synchronization=>$attribute{synchronization}, Gms=>$attribute{gms}, 
                Jbi=>$attribute{jbi},
                Jaxws=>$attribute{jaxws});
        />    
    </sun:button>
    <!-- Buttons  -->                    
    <!facet pageButtonsTop>
    <sun:panelGroup id="topButtons">
#include "shared/loggingLevelButton.inc"
    </sun:panelGroup>
    </facet>  
</sun:title>
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">			 
    <!-- Text Field section -->               
    <sun:propertySheetSection id="propertSectionTextField">
        <sun:property id="AdminProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Admin}">
            <sun:dropDown id="Admin" selected="#{requestScope.admin}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="AdminLabel" text="#{logListA}">
               <!beforeCreate
                    getLoggerList(Module="admin", LogList=>$page{logListA});
                />                                      
            </sun:staticText>
        </sun:property>      
        <sun:property id="ClassLoaderProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.ClassLoader}">
            <sun:dropDown id="ClassLoader" selected="#{requestScope.classLoader}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="ClassLoaderLabel" text="#{logListC}">                                
             <!beforeCreate
                    getLoggerList(Module="classloader", LogList=>$page{logListC});  
                />                                      
            </sun:staticText>                                
        </sun:property>                                    
        <sun:property id="ConfigurationProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Configuration}">
            <sun:dropDown id="Configuration" selected="#{requestScope.configuration}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="ConfigurationLabel" text="#{logListConfig}">                                
             <!beforeCreate
                    getLoggerList(Module="configuration", LogList=>$page{logListConfig});
                />                                      
            </sun:staticText>                                  
        </sun:property>                                    
        <sun:property id="ConnectorProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Connector}">
            <sun:dropDown id="Connector" selected="#{requestScope.connector}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="ConnectorLabel" text="#{logListConnector}">                                
             <!beforeCreate
                    getLoggerList(Module="connector", LogList=>$page{logListConnector});
                />                                      
            </sun:staticText>                                    
        </sun:property>                                    
        <sun:property id="CorbaProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Corba}">
            <sun:dropDown id="Corba" selected="#{requestScope.corba}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="CorbaLabel" text="#{logListCorba}">                                
             <!beforeCreate
                    getLoggerList(Module="corba", LogList=>$page{logListCorba});
                />                                      
            </sun:staticText>                                    
        </sun:property>
        <sun:property id="DeploymentProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Deployment}">
            <sun:dropDown id="Deployment" selected="#{requestScope.deployment}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="DeploymentLabel" text="#{logListDeployment}">                                
             <!beforeCreate
                    getLoggerList(Module="deployment", LogList=>$page{logListDeployment});
                />                                      
            </sun:staticText>                                     
        </sun:property>
        <sun:property id="EjbContainerProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.EjbContainer}">
            <sun:dropDown id="EjbContainer" selected="#{requestScope.ejb}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="EjbContainerLabel" text="#{logListEjbContainer}">                                
             <!beforeCreate
                    getLoggerList(Module="ejb-container", LogList=>$page{logListEjbContainer});
                />                                      
            </sun:staticText>                                    
        </sun:property>

        <sun:property id="gms"  rendered="$session{supportCluster}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.GMS}">
            <sun:dropDown id="gms" selected="#{requestScope.gms}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="gmsLabel" text="#{logListGms}">                                
             <!beforeCreate
                    getLoggerList(Module="group-management-service", LogList=>$page{logListGms});
                />                                      
            </sun:staticText>                                        
        </sun:property>

        <sun:property id="JavamailProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Javamail}">
            <sun:dropDown id="Javamail" selected="#{requestScope.javamail}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="JavamailLabel" text="#{logListJavamail}">                                
             <!beforeCreate
                    getLoggerList(Module="javamail", LogList=>$page{logListJavamail});
                />                                      
            </sun:staticText>                                        
        </sun:property>
        <sun:property id="JaxrProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Jaxr}">
            <sun:dropDown id="Jaxr" selected="#{requestScope.jaxr}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="JaxrLabel" text="#{logListJaxr}">                                
             <!beforeCreate
                    getLoggerList(Module="jaxr", LogList=>$page{logListJaxr});
                />                                      
            </sun:staticText>                                   
        </sun:property>
        <sun:property id="JaxrpcProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Jaxrpc}">
            <sun:dropDown id="Jaxrpc" selected="#{requestScope.jaxrpc}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="JaxrpcLabel" text="#{logListJaxrpc}">                                
             <!beforeCreate
                    getLoggerList(Module="jaxrpc", LogList=>$page{logListJaxrpc});
                />                                      
            </sun:staticText>                                     
        </sun:property>

        
        <sun:property id="jaxwsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Jaxws}">
            <sun:dropDown id="Jaxws" selected="#{requestScope.jaxws}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="JaxwsLabel" text="#{logListJaxws}">                                
             <!beforeCreate
                    getLoggerList(Module="jaxws", LogList=>$page{logListJaxws});
                />                                      
            </sun:staticText>                                       
        </sun:property>

        <sun:property id="JmsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Jms}">
            <sun:dropDown id="Jms" selected="#{requestScope.jms}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="JmsLabel" text="#{logListJms}">                                
             <!beforeCreate
                    getLoggerList(Module="jms", LogList=>$page{logListJms});
                />                                      
            </sun:staticText>                                     
        </sun:property>

        <sun:property id="jbiProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.JBI}">
            <sun:dropDown id="Jbi" selected="#{requestScope.jbi}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="JbiLabel" text="#{logListJbi}">                                
             <!beforeCreate
                    getLoggerList(Module="jbi", LogList=>$page{logListJbi});
                />                                      
            </sun:staticText>                                       
        </sun:property>
        <sun:property id="JtaProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Jta}">
            <sun:dropDown id="Jta" selected="#{requestScope.jta}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="JtaLabel" text="#{logListJta}">                                
             <!beforeCreate
                    getLoggerList(Module="jta", LogList=>$page{logListJta});
                />                                      
            </sun:staticText>                                     
        </sun:property>
        <sun:property id="JtsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Jts}">
            <sun:dropDown id="Jts" selected="#{requestScope.jts}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="JtsLabel" text="#{logListJts}">                                
             <!beforeCreate
                    getLoggerList(Module="jts", LogList=>$page{logListJts});
                />                                      
            </sun:staticText>                                     
        </sun:property>
        <sun:property id="MdbContainerProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.MdbContainer}">
            <sun:dropDown id="MdbContainer" selected="#{requestScope.mdb}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="MdbContainerLabel" text="#{logListMdbContainer}">                                
             <!beforeCreate
                    getLoggerList(Module="mdb-container", LogList=>$page{logListMdbContainer});
                />                                      
            </sun:staticText>                                   
        </sun:property>
        <sun:property id="NamingProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Naming}">
            <sun:dropDown id="Naming" selected="#{requestScope.naming}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="NamingLabel" text="#{logListNaming}">                                
             <!beforeCreate
                    getLoggerList(Module="naming", LogList=>$page{logListNaming});
                />                                      
            </sun:staticText>                                        
        </sun:property>

        <sun:property id="nodeAgent"  rendered="$session{supportCluster}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.NodeAgent}">
            <sun:dropDown id="nodeAgent" selected="#{requestScope.nodeAgent}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="naLabel" text="#{logListNodeAgent}">                                
             <!beforeCreate
                    getLoggerList(Module="node-agent", LogList=>$page{logListNodeAgent});
                />                                      
            </sun:staticText>                                        
        </sun:property>

       <sun:property id="PersistenceProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Persistence}">                         
            <sun:dropDown id="Persistence"  selected="#{logLevelPersistence}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" >
             <!beforeCreate
                    getPersistenceLogLevel(ConfigName="#{configName}", PersistenceLogLevel=>$page{logLevelPersistence});
                 />                                      
            </sun:dropDown>
          <sun:staticText id="RootLabel" text="#{logListPersistence}">                                
             <!beforeCreate
                    getPersistenceLoggerList(LogList=>$page{logListPersistence});
                />                                      
            </sun:staticText>                                    
        </sun:property>
        <sun:property id="RootProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Root}">
            <sun:dropDown id="Root" selected="#{requestScope.root}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="RootLabel" text="#{logListRoot}">                                
             <!beforeCreate
                    getLoggerList(Module="root", LogList=>$page{logListRoot});
                />                                      
            </sun:staticText>                                  
        </sun:property>
        <sun:property id="SaajProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Saaj}">
            <sun:dropDown id="Saaj" selected="#{requestScope.saaj}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="SaajLabel" text="#{logListSaaj}">                                
             <!beforeCreate
                    getLoggerList(Module="saaj", LogList=>$page{logListSaaj});
                />                                      
            </sun:staticText>                                 
        </sun:property>
        <sun:property id="SecurityProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Security}">
            <sun:dropDown id="Security" selected="#{requestScope.security}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="SecurityLabel" text="#{logListSecurity}">                                
             <!beforeCreate
                    getLoggerList(Module="security", LogList=>$page{logListSecurity});
                />                                      
            </sun:staticText>                                             
        </sun:property>
        <sun:property id="SelfManagementProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.SelfManagement}">
            <sun:dropDown id="SelfManagement" selected="#{requestScope.selfManagement}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="SelfManagementLabel" text="#{logListSelfManagement}">                                
             <!beforeCreate
                    getLoggerList(Module="self-management", LogList=>$page{logListSelfManagement});
                />                                      
            </sun:staticText>                                        
        </sun:property>
        <sun:property id="ServerProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Server}">
            <sun:dropDown id="Server" selected="#{requestScope.server}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="ServerLabel" text="#{logListServer}">                                
             <!beforeCreate
                    getLoggerList(Module="server", LogList=>$page{logListServer});
                />                                      
            </sun:staticText>                                      
        </sun:property>

        <sun:property id="synchronization"  rendered="$session{supportCluster}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Synchronization}">
            <sun:dropDown id="synchronization" selected="#{requestScope.synchronization}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="NamingLabel" text="#{logListSyn}">                                
             <!beforeCreate
                    getLoggerList(Module="synchronization", LogList=>$page{logListSyn});
                />                                      
            </sun:staticText>                                        
        </sun:property>


        <sun:property id="UtilProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Util}">
            <sun:dropDown id="Util" selected="#{requestScope.util}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="UtilLabel" text="#{logListUtil}">                                
             <!beforeCreate
                    getLoggerList(Module="util", LogList=>$page{logListUtil});
                />                                      
            </sun:staticText>                                         
        </sun:property>
        <sun:property id="VerifierProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Verifier}">
            <sun:dropDown id="Verifier" selected="#{requestScope.verifier}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="VerifierLabel" text="#{logListVerifier}">                                
             <!beforeCreate
                    getLoggerList(Module="verifier", LogList=>$page{logListVerifier});
                />                                      
            </sun:staticText>                                          
        </sun:property>
        <sun:property id="WebContainerProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.WebContainer}">
            <sun:dropDown id="WebContainer" selected="#{requestScope.web}" labels="$attribute{levelsLabel}" values="$attribute{levelsValue}" />
            <sun:staticText id="WebContainerLabel" text="#{logListWebContainer}">                                
             <!beforeCreate
                    getLoggerList(Module="web-container", LogList=>$page{logListWebContainer});
                />                                      
            </sun:staticText>                                    
        </sun:property>                       
        "<br /><br />
    </sun:propertySheetSection>
</sun:propertySheet>    
#include "propertyTable.inc"

        <sun:title id="title2">
                <!facet pageButtonsBottom>
                <sun:panelGroup id="bottomButtons">
#include "shared/loggingLevelButton.inc"
                </sun:panelGroup>
                </facet> 
                </sun:title>                              
                
	<sun:hidden id="helpKey" value="$attribute{helpKey}" />
            </sun:form>
        </sun:body>
#include "changeButtonsJS.inc" 
    </sun:html>
</sun:page>

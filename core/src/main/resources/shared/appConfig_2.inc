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

<!-- shared/appConfig_2.inc -->

#include "/shared/alertMsg.inc"
    <!-- Page Title -->
    <sun:title id="propertyContentPage" title="$resource{i18n.server.AppsConfigPageTitle}" 
        helpText="$resource{i18n.server.AppsConfigPageHelp}">
        <sun:button id="loadDefaults" style="margin-left: 8pt"  primary="#{false}"  text="$resource{i18n.button.LoadDefaults}" >    
            <!command
                getServerDefaultAppsConfigAttributes(ConfigName="#{configName}", Reload=>$attribute{reload}, ReloadInterval=>$attribute{reloadInterval}, 
                AutoDeploy=>$attribute{autoDeploy}, AdminTimeout=>$attribute{adminTimeout}, AutoDeployInterval=>$attribute{autoDeployInterval},
                AutoDeployTimeout=>$attribute{autoDeployTimeout}, 
                Verifier=>$attribute{verifier}, Precompile=>$attribute{precompile}, AutoDeployDirectory=>$attribute{autoDeployDirectory});
                />
        </sun:button>
        <!-- Buttons  -->                    
        <!facet pageButtonsTop>
#include "/shared/appConfig_2Buttons.inc"
        </facet>       
        </sun:title>
        <!-- PropertySheet .... -->
        <sun:propertySheet id="propertySheet">			 
            <!-- Text Field section -->               
            <sun:propertySheetSection id="propertSectionTextField">
              <sun:property id="reloadProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.server.Reload}" helpText="$resource{i18n.server.ReloadHelp}">                        
                    <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{requestScope.reload}" >      
                    </sun:checkbox>
                </sun:property>                        
                <sun:property id="reloadIntervalProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.server.ReloadPollInterval}" helpText="$resource{i18n.server.ReloadPollIntervalHelp}">
                    <sun:textField id="ReloadInterval" styleClass="integer" columns="$int{15}" text="#{requestScope.reloadInterval}"> 

                    </sun:textField>
                    <sun:staticText id="secs" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>                                
                </sun:property>                       
                <sun:property id="AdminTimeoutProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.server.AdminTimeout}" helpText="$resource{i18n.server.AdminTimeoutHelp}">                           
                    <sun:textField id="AdminTimeout" styleClass="integer" columns="$int{15}" text="#{requestScope.adminTimeout}"/>     
                    <sun:staticText id="mins" style="padding: 8pt" text="$resource{i18n.common.Minutes}"/>
                </sun:property>     
            </sun:propertySheetSection>
            <sun:propertySheetSection id="propertSectionAutoDeploy" label="$resource{i18n.server.AutoDeploySettings}" >                   
                <sun:property id="AutoDeployProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.server.AutoDeploy}" helpText="$resource{i18n.server.AutoDeployHelp}">
                    <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{requestScope.autoDeploy}" />      
                </sun:property>           
               <sun:property id="AutoDeployIntervalProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.server.AutoDeployPollInterval}" helpText="$resource{i18n.server.AutoDeployPollIntervalHelp}">
                    <sun:textField id="AutoDeployInterval" styleClass="integer" columns="$int{15}" text="#{requestScope.autoDeployInterval}" />      
                    <sun:staticText id="secs" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
                </sun:property>
                <sun:property id="AutoDeployTimeoutProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.server.AutoDeployTimeout}" helpText="$resource{i18n.server.AutoDeployTimeoutHelp}">
                    <sun:textField id="AutoDeployTimeout" styleClass="integer" columns="$int{15}" text="#{requestScope.autoDeployTimeout}" />      
                    <sun:staticText id="secs" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
                </sun:property>
               <sun:property id="AutoDeployDirectoryProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.server.AutoDeployDirectory}" helpText="$resource{i18n.server.AutoDeployDirectoryHelp}">
                    <sun:textField id="AutoDeployDirectory" columns="$int{55}" text="#{requestScope.autoDeployDirectory}" />      
                </sun:property>                            
                <sun:property id="VerifierProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.server.Verifier}" helpText="$resource{i18n.server.VerifierHelp}">
                    <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{requestScope.verifier}" />      
                </sun:property>                        
                 <sun:property id="PrecompileProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.server.Precompile}" helpText="$resource{i18n.server.PrecompileHelp}">
                    <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{requestScope.precompile}" />      
                </sun:property>                        
            "<br /><br />
            </sun:propertySheetSection>

        </sun:propertySheet>    
#include "/shared/propertyTable.inc"                           
        <sun:title id="propertyContentPage2">
        <!facet pageButtonsBottom>
#include "/shared/appConfig_2Buttons.inc"
        </facet>       
        </sun:title>
		<sun:hidden id="helpKey" value="$attribute{helpKey}" />
            </sun:form>
#include "/shared/changeButtonsJS.inc"
</define>
</composition>

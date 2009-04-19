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

<!-- applications/edit.inc -->

<!-- Page Title -->
<sun:title id="propertyContentPage" title="#{pageTitle}" helpText="#{pageTitleHelp}" >
    <!-- Buttons  -->                    
    <!facet pageButtonsTop>
    <sun:panelGroup id="topButtons">
        <sun:button id="saveButton" text="$resource{i18n.button.Save}" primary="#{true}">  
           <!command
                saveApplicationInfo(
                    name="#{appName}"
                    enabled="#{enabled}"
                    description="#{description}"
                    javaWebStart="#{javaWebStart}"
                    contextRoot="#{contextRoot}"
                    );
		if("#{pe}&#{hasVS}") {
                  	createVSReferences(
				vsTargets="#{selectedVS}"
                        	targetName="server"
                        	name="#{appName}");
		}
                prepareSuccessfulMsg();
          />
          </sun:button>
          <sun:button id="cancelButton" text="$resource{i18n.button.Cancel}" primary="#{false}"  immediate="#{true}">
            <!command
                redirect(page="#{request.contextPath}/applications/applications.jsf?name=#{name}");
            />
        </sun:button>
    </sun:panelGroup>
    </facet> 
 </sun:title>
 
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">			 
    <!-- Text Field section -->               
    <sun:propertySheetSection id="propertSectionTextField">
        <event>
        <!beforeCreate
            getRequestValue(key="appName" value=>$page{appName});
            setPageSessionAttribute(key="pe" value="#{true}")                               
            getApplicationInfo(name="#{appName}" 
                location=>$page{location} description=>$attribute{description} 
                enabled=>$attribute{enabled} enabledString=>$attribute{enabledString}
                libraries=>$page{libraries} 
                contextRoot=>$attribute{contextRoot} javaWebStart=>$attribute{javaWebStart})
            setPageSessionAttribute(key="showHa" value="$boolean{false}");
            # if ( $session{supportCluster} & $pageSession{hasHa} ){
            #   setPageSessionAttribute(key="showHa" value="$boolean{true}");
            # }
            if ( $session{supportCluster}){
            	setPageSessionAttribute(key="pe" value="#{false}")                               
            }
        />
        </event>
        <sun:property id="jndiProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}">
            <sun:staticText id="name" text="#{appName}"/>
        </sun:property> 

        
       	<sun:property id="vsProp" rendered="#{hasVS}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.deploy.virtualServerLabel}" helpText="$resource{i18n.deploy.vsHelp}" >
            <sun:listbox id="vs" immediate="#{true}"  labels="$pageSession{availableVS}" multiple="#{true}"  rows="$int{4}"  selected="#{selectedVS}" >
                <!beforeCreate
                if("#{pe}") {
                    getAllVSList( targetName="server"  PE="#{pe}"  serverList=>$attribute{vsList});
                    getVSOptions(vsList="$attribute{vsList}" 
                            name="#{appName}"
                            PE="#{pe}"
                            targetName="server"
                            availableVS=>$pageSession{availableVS}
                            selectedVS=>$pageSession{selectedVS});
                }
                />
            </sun:listbox>
       	</sun:property>
        <sun:property id="ctxProp" rendered="#{hasContextRoot}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.webApp.ContextRoot}" helpText="$resource{i18n.webApp.contextRootHelp}" >
            <sun:textField id="ctx" columns="$int{40}" text="#{contextRoot}" />
        </sun:property>

        <sun:property id="descProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
            <sun:textField id="desc" columns="$int{40}" text="#{requestScope.description}" />
        </sun:property>

    
        <sun:property id="statusProp" rendered="$session{notSupportCluster}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
            <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{requestScope.enabled}" />  
       </sun:property>

       <sun:property id="statusProp2" rendered="$session{supportCluster}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" 
            label="$resource{i18n.common.status}" helpText="$resource{i18n.application.EnableTargetHelp}">
            <sun:staticText  text="#{requestScope.enabledString}" />  
       </sun:property>

       <sun:property id="jwsProp"  rendered="#{hasJavaWebStart}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.JavaWebStart}">
            <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{javaWebStart}" />  
       </sun:property> 

        <sun:property id="locationProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.location}">
            <sun:staticText id="location" text="#{location}"/>
        </sun:property> 

        <sun:property id="librariesProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Libraries}">
            <sun:staticText id="libraries" text="#{libraries}"/>
            "<br /><br />
        </sun:property> 
    </sun:propertySheetSection>
</sun:propertySheet> 



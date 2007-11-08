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
<!-- jbi/cluster/inc/sharedPropertySheet.inc -->

 <!-- PropertySheet .... -->
 <sun:propertySheet id="propertySheet">			 
 
    <!-- Text Field section -->               
    <sun:propertySheetSection id="propertySheetSection" label="#{sessionScope.pssLabel}">

        <sun:property id="showFileNameProperty"  
     	    overlapLabel="#{false}" 
     	    rendered="#{sessionScope.renderFileName}"
            label="$resource{i18n.jbi.show.filename.property.label}" 
            labelAlign="left" 
            noWrap="#{true}" 
            >

            <sun:staticText id="showFileNameText" 
                rendered="#{sessionScope.renderInstall}"
                text="#{sessionScope.sharedShowFileName}" 
                /> 

        </sun:property>

        <sun:property id="showNameProperty"  
     	    overlapLabel="#{false}" 
            label="$resource{i18n.jbi.show.name.property.label}" 
            labelAlign="left" 
            noWrap="#{true}" 
            >

            <sun:staticText id="installNameText" 
                rendered="#{sessionScope.renderInstall}"
                text="#{ArchiveBean.jbiName}"
                /> 

            <sun:staticText id="showNameText" 
                rendered="#{sessionScope.renderShow}"
                text="#{ShowBean.name}"
                /> 

        </sun:property>

        <sun:property id="showTypeProperty"  
            label="$resource{i18n.jbi.show.type.property.label}"
            labelAlign="left" 
            noWrap="#{true}" 
            overlapLabel="#{false}"
            rendered="#{sessionScope.renderType}"
            >                           


              <sun:staticText id="showTypeBindingText" 
                  rendered="#{'binding-component'==ShowBean.type}"
                  text="$resource{i18n.jbi.show.type.binding}"
                  />     

              <sun:staticText id="showTypeDeploymentText" 
                  rendered="#{'service-assembly'==ShowBean.type}"
                  text="$resource{i18n.jbi.show.type.deployment}"
                  />     

              <sun:staticText id="showTypeEngineText" 
                  rendered="#{'service-engine'==ShowBean.type}"
                  text="$resource{i18n.jbi.show.type.engine}"
                  />     

              <sun:staticText id="showTypeLibraryText" 
                  rendered="#{'shared-library'==ShowBean.type}"
                  text="$resource{i18n.jbi.show.type.library}"
                  />     

        </sun:property> 

        <sun:property id="showDescriptionProperty"  
            label="$resource{i18n.jbi.show.description.property.label}"
            labelAlign="left" 
            noWrap="#{true}" 
            overlapLabel="#{false}" 
            >  

            <sun:staticText id="showDescriptionText"  
                rendered="#{sessionScope.renderShow}"
                text="#{ShowBean.description}"
                />     

            <sun:staticText id="installDescriptionText"  
                rendered="#{sessionScope.renderInstall}"
                text="#{ArchiveBean.description}"
                />     

        </sun:property>

        <sun:property id="showMetadataProperty"  
            label="$resource{i18n.jbi.show.metadata.property.label}"
            labelAlign="left" 
            noWrap="#{true}" 
            overlapLabel="#{false}"
            rendered="#{sessionScope.renderMetadataLink}"
            >                           

            <sun:hyperlink id="showMetadataLink" 
                text="$resource{i18n.jbi.show.metadata.link.text}" 
                >
		<!command
			setMetaDataContents(compName="#{sessionScope.sharedShowName}" ,compType="#{ShowBean.type}");
			navigate(page="/jbi/cluster/showMetadata.jsf");	
		/>
           </sun:hyperlink>	

        </sun:property> 

        <sun:property id="showDetailsProperty"  
            label="Details:"
            labelAlign="left" 
            noWrap="#{true}" 
            overlapLabel="#{false}"
            rendered="#{false}"
            >                           <!-- TBD conditional rendering -->

            <sun:hyperlink id="showDetailsLink" 
                text="Component supplied properties" 
                >
		<!command
			setMetaDataContents(compName="#{sessionScope.sharedShowName}" ,compType="#{ShowBean.type}");
			navigate(page="/jbi/cluster/showDetails.jsf");	
		/>
           </sun:hyperlink>	

        </sun:property> 

        <sun:property id="showEnableStatusProperty"  
            label="$resource{i18n.jbi.wizard.status.label}"
            labelAlign="left" 
            noWrap="#{true}" 
            overlapLabel="#{false}"
	    rendered="#{sessionScope.renderEnableStatus}"
            helpText="$resource{i18n.jbi.wizard.enable.inlineHelp, 
                #{'binding-component'==ArchiveBean.jbiType ? requestScope.i18n['jbi.show.type.binding'] :
                'service-engine'==ArchiveBean.jbiType ? requestScope.i18n['jbi.show.type.engine'] : requestScope.i18n['jbi.show.type.deployment']}}"
            >                           

            <sun:checkbox id="enableCB" 
                label="$resource{i18n.jbi.wizard.enable.status.label}" 
		selected="#{sessionScope.isEnabledAfterInstallOrDeploy}"
                />
        </sun:property> 

     </sun:propertySheetSection>

 </sun:propertySheet>    


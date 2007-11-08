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
<!-- jbi/pe/inc/showTabs.inc -->

<sun:tabSet id="jbiShowTabs" immediate="$boolean{false}" selected="#{sessionScope.jbiShowTabs}">
     
    <sun:tab id="general" immediate="$boolean{false}" text="$resource{i18n.jbi.pe.show.GeneralTab}" >
        <!command
setSessionAttribute(key="jbiShowTabs" value="general");
redirect(page="#{'service-assembly'==sessionScope.sharedShowType? 'showDeployment.jsf' : 'shared-library'==sessionScope.sharedShowType ? 'showLibrary.jsf' : 'showBindingOrEngine.jsf'}?name=#{sessionScope.sharedShowName}&type=#{sessionScope.sharedShowType}");
        />       
    </sun:tab>


    <sun:tab id="configuration" immediate="$boolean{false}" 
         rendered="#{('binding-component'==sessionScope.sharedShowType)||('service-engine'==sessionScope.sharedShowType)}"
         text="$resource{i18n.jbi.pe.show.ConfigurationTab}">
	<!command
setSessionAttribute(key="jbiShowTabs" value="configuration");
#redirect(page="configuration.jsf?name=#{sessionScope.sharedShowName}&type=#{'binding-component'==sessionScope.sharedRequestType ? 'BC' : 'SE' }&tname=server");
redirect(page="configureComponent.jsf?name=#{sessionScope.sharedShowName}&type=#{sessionScope.sharedShowType}&tname=server");
        />
    </sun:tab>

    <sun:tab id="descriptor" immediate="$boolean{false}" text="$resource{i18n.jbi.pe.show.DescriptorTab}">
	<!command
setSessionAttribute(key="jbiShowTabs" value="descriptor");
redirect(page="showMetadata.jsf?name=#{sessionScope.sharedShowName}&type=#{sessionScope.sharedShowType}");
        />
    </sun:tab>

     <sun:tab id="loggers" immediate="$boolean{false}" 
         rendered="#{('binding-component'==sessionScope.sharedShowType)||('service-engine'==sessionScope.sharedShowType)}"
         text="$resource{i18n.jbi.pe.show.LoggersTab}">
	<!command
setSessionAttribute(key="jbiShowTabs" value="loggers");
redirect(page="configureLoggers.jsf?name=#{sessionScope.sharedShowName}&type=#{sessionScope.sharedShowType}");
        />
    </sun:tab>

     <sun:tab id="monitor" immediate="$boolean{false}" 
         rendered="#{('binding-component'==sessionScope.sharedShowType)||('service-engine'==sessionScope.sharedShowType)}"
         text="$resource{i18n.jbi.pe.show.MonitorTab}">
    <!command
setSessionAttribute(key="jbiShowTabs" value="monitor");
redirect(page="monitorComponent.jsf?name=#{sessionScope.sharedShowName}&type=#{sessionScope.sharedShowType}");
        />

    </sun:tab>


     <sun:tab id="saDependencies" immediate="$boolean{false}" 
         rendered="false"
         text="$resource{i18n.jbi.pe.show.sa.DependenciesTab}">
	<!command
setSessionAttribute(key="jbiShowTabs" value="saDependencies");
redirect(page="showSADependencies.jsf?name=#{sessionScope.sharedShowName}&type=#{sessionScope.sharedShowType}");
        />

    </sun:tab>

     <sun:tab id="compDependencies" immediate="$boolean{false}" 
         rendered="false"
         text="$resource{i18n.jbi.pe.show.comp.DependenciesTab}">
	<!command
setSessionAttribute(key="jbiShowTabs" value="compDependencies");
redirect(page="showCompDependenciesSA.jsf?name=#{sessionScope.sharedShowName}&type=#{sessionScope.sharedShowType}");
        />

#include "jbi/pe/inc/showCompDependenciesSubtabs.inc"

    </sun:tab>

     <sun:tab id="slDependencies" immediate="$boolean{false}" 
         rendered="false"
         text="$resource{i18n.jbi.pe.show.sl.DependenciesTab}">
	<!command
setSessionAttribute(key="jbiShowTabs" value="slDependencies");
redirect(page="showLibDependencies.jsf?name=#{sessionScope.sharedShowName}&type=#{sessionScope.sharedShowType}");
        />

    </sun:tab>

</sun:tabSet>



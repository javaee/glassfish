
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
<!-- appServer/serverInstTabs.jsf -->

<sun:tabSet id="serverInstTabs" immediate="true" selected="#{sessionScope.serverInstTabs}">

    <sun:tab id="general" immediate="true" text="$resource{i18n.common.General}" >
        <!command
            setSessionAttribute(key="serverInstTabs" value="general");
            redirect(page="#{request.contextPath}/appServer/serverInstGeneralPe.jsf");
	    />       
    </sun:tab>


    <sun:tab id="jvmSettings" immediate="true" text="$resource{i18n.jvm.JvmSettings}">
	<!command
            setSessionAttribute(key="serverInstTabs" value="jvmGeneral");
	    redirect(page="#{request.contextPath}/appServer/serverInstJvmGeneral.jsf?configName=server-config");
	/>
	<sun:tab id="jvmGeneral" immediate="true" text="$resource{i18n.common.General}">
	<!command
            setSessionAttribute(key="serverInstTabs" value="jvmGeneral");
	    redirect(page="#{request.contextPath}/appServer/serverInstJvmGeneral.jsf?configName=server-config");
	/>
	</sun:tab>
	<sun:tab id="pathSettings" immediate="true" text="$resource{i18n.jvm.PathSettings}">
	    <!command
                setSessionAttribute(key="serverInstTabs" value="pathSettings");
		redirect(page="#{request.contextPath}/appServer/serverInstJvmPathSettings.jsf?configName=server-config");
	    />        

	</sun:tab>
	<sun:tab id="jvmOptions" immediate="true" text="$resource{i18n.jvm.Options}" >
	    <!command
                setSessionAttribute(key="serverInstTabs" value="jvmOptions");
		redirect(page="#{request.contextPath}/appServer/serverJvmOptions.jsf?configName=server-config");
	    />        
	</sun:tab>
	<sun:tab id="profiler" immediate="true" text="$resource{i18n.jvm.Profiler}" >
	    <!command
                setSessionAttribute(key="serverInstTabs" value="profiler");
		redirect(page="#{request.contextPath}/appServer/serverInstProfiler.jsf?configName=server-config");
	    />        
	</sun:tab>
    </sun:tab>


    <!--
    <sun:tab id="logging" immediate="true" text="$resource{i18n.log.Logging}">
         <!command
                setSessionAttribute(key="serverInstTabs" value="loggingGeneral");
		redirect(page="#{request.contextPath}/appServer/serverInstLogging.jsf?configName=server-config");
	    />    
	<sun:tab id="loggingGeneral" immediate="true" text="$resource{i18n.common.General}" >
	    <!command
                setSessionAttribute(key="serverInstTabs" value="loggingGeneral");
		redirect(page="#{request.contextPath}/appServer/serverInstLogging.jsf?configName=server-config");
	    />       
	</sun:tab>
	<sun:tab id="logLevels" immediate="true" text="$resource{i18n.log.LogLevels}" >
	    <!command
                setSessionAttribute(key="serverInstTabs" value="logLevels");
		redirect(page="#{request.contextPath}/appServer/serverInstLoggingLevels.jsf?configName=server-config");
	    />             
	</sun:tab>
    </sun:tab>


    <sun:tab id="monitor" immediate="true" text="$resource{i18n.monitor.Monitor}">
         <!command
                setSessionAttribute(key="serverInstTabs" value="log");
		redirect(page="#{request.contextPath}/appServer/logAnalyzer.jsf?instanceName=server");
         />   
        <sun:tab id="log" immediate="true" text="$resource{i18n.log.Log}" >
	    <!command
                setSessionAttribute(key="serverInstTabs" value="log");
		redirect(page="#{request.contextPath}/appServer/logAnalyzer.jsf?instanceName=server");
	    />             
	</sun:tab>
	<sun:tab id="callFlow" immediate="true" text="$resource{i18n.monitor.CallFlow}" >
	    <!command
                setSessionAttribute(key="serverInstTabs" value="callFlow");
		redirect(page="#{request.contextPath}/appServer/callFlowConfig.jsf?instanceName=server");
	    />             
	</sun:tab>
	<sun:tab id="runtime" immediate="true" text="$resource{i18n.monitor.Runtime}" >
	    <!command
                setSessionAttribute(key="serverInstTabs" value="runtime");
		redirect(page="#{request.contextPath}/appServer/monitorRuntime.jsf?instanceName=server&configName=server-config");
	    />             
	</sun:tab>
	<sun:tab id="applications" immediate="true" text="$resource{i18n.monitor.Applications}" >
	    <!command
                setSessionAttribute(key="serverInstTabs" value="applications");
		redirect(page="#{request.contextPath}/appServer/monitorApps.jsf?instanceName=server&configName=server-config");
	    />             
	</sun:tab>
	<sun:tab id="resources" immediate="true" text="$resource{i18n.monitor.Resources}" >
	    <!command
                setSessionAttribute(key="serverInstTabs" value="resources");
		redirect(page="#{request.contextPath}/appServer/monitorResources.jsf?instanceName=server&configName=server-config");
	    />             
	</sun:tab>
	<sun:tab id="transactions" immediate="true" text="$resource{i18n.monitor.Transactions}" >
	    <!command
                setSessionAttribute(key="serverInstTabs" value="transactions");
		redirect(page="#{request.contextPath}/appServer/monitorTransactions.jsf?instanceName=server&configName=server-config");
	    />             
	</sun:tab>
    </sun:tab>

    <sun:tab id="diagnostics" immediate="true" text="$resource{i18n.headings.Diagnostics}" >
    	    <!command
                setSessionAttribute(key="serverInstTabs" value="diagnostics");
		redirect(page="#{request.contextPath}/appServer/diagReport.jsf");
	    />
    </sun:tab>

    <sun:tab id="adminPassword" immediate="true" text="$resource{i18n.headings.AdminPassword}" >
	    <!command
                setSessionAttribute(key="serverInstTabs" value="adminPassword");
		redirect(page="#{request.contextPath}/appServer/serverInstAdminPassword.jsf?configName=server-config");
	    />      
    </sun:tab>

    -->

    <sun:tab id="advanced" immediate="true" text="$resource{i18n.headings.Advanced}">
        <!command
                setSessionAttribute(key="serverInstTabs" value="advanced");
            redirect(page="#{request.contextPath}/appServer/serverInstAppsConfig.jsf?configName=server-config");
        />     
	<sun:tab id="appConfig" immediate="true" text="$resource{i18n.headings.AppConfig}" >
            <!command
                setSessionAttribute(key="serverInstTabs" value="appConfig");
		redirect(page="#{request.contextPath}/appServer/serverInstAppsConfig.jsf?configName=server-config");
	    />         
	</sun:tab>
	<sun:tab id="domainAttrs" immediate="true" text="$resource{i18n.headings.DomainAttributes}" >
            <!command
                setSessionAttribute(key="serverInstTabs" value="domainAttrs");
		redirect(page="#{request.contextPath}/appServer/serverInstDomainAttrs.jsf");
	    />           
	</sun:tab>
    </sun:tab>
</sun:tabSet>
<event>
    <!afterCreate
	includeIntegrations(type="org.glassfish.admingui:serverInstTab");
    />
</event>

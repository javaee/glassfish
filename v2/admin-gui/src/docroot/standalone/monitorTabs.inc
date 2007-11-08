
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
<!-- standalone/monitorTabs.jsf -->

    <sun:tab id="monitor" immediate="$boolean{true}" text="$resource{i18n.monitor.Monitor}">
         <!-- note: the extra "../" in the redirect below is required for the
              case where the server JBI tabs include this tabset. see: GlassFish IT:3173.
              This change has no impact on non JBI tabs including this tabset. -->
        <!command
            setSessionAttribute(key="standaloneTabs" value="log");
            redirect(page="../../standalone/eeLogAnalyzer.jsf?instanceName=#{instanceName}");
        />
        <sun:tab id="log" immediate="$boolean{true}" text="$resource{i18n.log.Log}" >
	    <!command
                setSessionAttribute(key="standaloneTabs" value="log");
                redirect(page="../standalone/eeLogAnalyzer.jsf?instanceName=#{instanceName}");
	    />
	</sun:tab>
	<sun:tab id="callFlow" immediate="$boolean{true}" text="$resource{i18n.monitor.CallFlow}" >
	    <!command
                setSessionAttribute(key="standaloneTabs" value="callFlow");
		redirect(page="../standalone/eeCallFlowConfig.jsf?instanceName=#{instanceName}");
	    />             
	</sun:tab>
        <sun:tab id="runtime" immediate="$boolean{true}" text="$resource{i18n.monitor.Runtime}" >
	    <!command
                setSessionAttribute(key="standaloneTabs" value="runtime");
                redirect(page="../standalone/monitorRuntime.jsf?instanceName=#{instanceName}&configName=#{config}");
	    />
	</sun:tab>
	<sun:tab id="monitorApplications" immediate="$boolean{true}" text="$resource{i18n.monitor.Applications}" >
	    <!command
                setSessionAttribute(key="standaloneTabs" value="monitorApplications");
                redirect(page="../standalone/monitorApps.jsf?instanceName=#{instanceName}&configName=#{config}");
	    />
	</sun:tab>
	<sun:tab id="monitorResources" immediate="$boolean{true}" text="$resource{i18n.monitor.Resources}" >
	    <!command
                setSessionAttribute(key="standaloneTabs" value="monitorResources");
                redirect(page="../standalone/monitorResources.jsf?instanceName=#{instanceName}&configName=#{config}");
	    />
	</sun:tab>
	<sun:tab id="transactions" immediate="$boolean{true}" text="$resource{i18n.monitor.Transactions}" >
	    <!command
                setSessionAttribute(key="standaloneTabs" value="transactions");
                redirect(page="../standalone/monitorTransactions.jsf?instanceName=#{instanceName}&configName=#{config}");
	    />
	</sun:tab>
    </sun:tab>
   

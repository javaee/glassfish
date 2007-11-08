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
<!-- shared/logging_2.inc -->
<!-- TODO: 

     - missing description link next to Log File field
-->

#include "alertMsg.inc"
                <!-- Page Title -->
                <sun:title id="propertyContentPage" title="$resource{i18n.log.LoggingSettings}" 
                    helpText="$resource{i18n.log.LoggingSettingsHelp}">
                    <sun:button id="logViewer" 
                            onClick="javascript:var win=window.open('../logViewer/logViewer.jsf?instanceName=server&loglevel=INFO&logFile=server.log&viewResults=true','LogViewerWindow','width='+(screen.width-300)+',height='+(screen.height-75)+',top=0,left=0,toolbar=no,status=no,menubar=no,scrollbars=yes,resizable=yes,directories=no,location=no');win.focus(); return false;" style="margin-left: 8pt" text="$resource{i18n.button.ViewLogFiles}" />    
                    <!-- Buttons  -->                    
                    <!facet pageButtonsTop>
#include "shared/logging_2Buttons.inc"                    
                    </facet>    
                    </sun:title>
                    <!-- PropertySheet .... -->
                    <sun:propertySheet id="propertySheet">			 
                        <!-- Text Field section -->               
                        <sun:propertySheetSection id="propertSectionTextField">
                            <sun:property id="logFileProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.log.LogFile}" helpText="$resource{i18n.log.LogFileHelp}">
                                <sun:textField id="LogFile" columns="$int{55}" text="#{requestScope.logFile}"/> 
                            </sun:property>
                           <sun:property id="alarmsEnabledProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.log.Alarms}" helpText="$resource{i18n.log.AlarmsHelp}">
                                <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{requestScope.alarms}" />   
                           </sun:property>
                          <sun:property id="writeSystemLogEnabledProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.log.WriteSystemLog}" helpText="$resource{i18n.log.WriteSystemlogHelp}">
                                <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{requestScope.systemLog}" />   
                           </sun:property>                           
                            <sun:property id="logHandlerProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.log.Handler}" helpText="$resource{i18n.log.HandlerHelp}">
                                <sun:textField id="LogHandler" columns="$int{55}" text="#{requestScope.logHandler}" />      
                            </sun:property>
                            <sun:property id="LogFilterProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.log.Filter}" helpText="$resource{i18n.log.FilterHelp}">
                                <sun:textField id="LogFilter" columns="$int{55}" text="#{requestScope.logFilter}"/>      
                            </sun:property> 
                            <sun:property id="FileRotationLimitProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.log.FileRotationLimit}" helpText="$resource{i18n.log.FileRotationLimitHelp}">
                                <sun:textField id="FileRotationLimit" styleClass="integer" columns="$int{15}" text="#{requestScope.rotationLimit}" />  
                                <sun:staticText id="byte" style="padding: 8pt" text="$resource{i18n.common.Bytes}"/>
                            </sun:property>      
                            <sun:property id="FileRotationTimeLimitProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.log.FileRotationTimeLimit}" helpText="$resource{i18n.log.FileRotationTimeLimitHelp}">
                                <sun:textField id="FileRotationTimeLimit" styleClass="integer" columns="$int{15}" text="#{requestScope.rotationTimeLimit}"/> 
                                <sun:staticText id="min" style="padding: 8pt" text="$resource{i18n.common.Minutes}"/>
                            </sun:property>  
                            <sun:property id="RetainErrorStatsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.log.RetainErrorStats}">
                                <sun:textField id="RetainErrorStats" styleClass="integer" columns="$int{15}" text="#{requestScope.retainErrorStats}"/>  
                                <sun:staticText id="hour" style="padding: 8pt" text="$resource{i18n.common.Hours}"/>
                            </sun:property>                             
                            "<br /><br />
                        </sun:propertySheetSection>
 
                    </sun:propertySheet>    
#include "propertyTable.inc"                    
                <sun:title id="propertyContentPage2">                    
                    <!facet pageButtonsBottom>
#include "shared/logging_2Buttons.inc"                    
                    </facet>    
                </sun:title>                
	<sun:hidden id="helpKey" value="$attribute{helpKey}" />
            </sun:form>
        </sun:body>
#include "changeButtonsJS.inc" 
    </sun:html>
</sun:page>

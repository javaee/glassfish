
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
<!-- shared/jvmProfiler_2.inc -->

#include "/shared/alertMsg.inc"
                <!-- Page Title -->
                <sun:title id="propertyContentPage" title="$resource{i18n.jvm.ProfilerPageName}"
                    helpText="$resource{i18n.jvm.ProfilerPageHelp}">
                    <!-- Buttons  -->                    
                    <!facet pageButtonsTop>
                    <sun:panelGroup id="topButtons">
                        <sun:button id="saveButton" text="$resource{i18n.button.Save}" 
                            onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " >       
                          <!command
                            getUIComponent(clientId="$pageSession{jvmTableRowGroupId}", component=>$attribute{tableRowGroup});      
                            getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});                            
                            convertListToArrayList(TableList="#{newList}", Name="option", NameList=>$attribute{nameList});
                            saveServerProfilerSettings(
                                ConfigName="#{configName}",
                                ProfilerName="#{profilerName}"
                                ProfilerEnabled="#{profilerEnabled}"
                                Classpath="#{classPath}"
                                NativeLibrary="#{nativeLibrary}"
                            );
                            saveProfilerJvmOptions(NameList="#{nameList}", ConfigName="#{configName}"); 
                            prepareSuccessfulMsg();
                  /> 
                        </sun:button>
                    </sun:panelGroup>
                    </facet>   
                    </sun:title>
                    <!-- PropertySheet .... -->
                    <sun:propertySheet id="propertySheet" requiredFields="true">			 
                        <!-- Text Field section -->               
                        <sun:propertySheetSection id="propertSectionTextField">
                            <sun:property id="profilerNameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jvm.ProfilerName}">
                                <sun:textField id="ProfilerName" rendered="#{!edit}" styleClass="required" columns="$int{55}" text="#{profilerName}" required="#{true}"/> 
                                <sun:staticText id="ProfilerName2" rendered="#{edit}" text="#{profilerName}"/>
                            </sun:property>
                           <sun:property id="profilerEnabledProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jvm.Profiler}">
                                <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{requestScope.profilerEnabled}" />   
                           </sun:property>
                            <sun:property id="ClasspathProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jvm.Classpath}" helpText="$resource{i18n.jvm.ClasspathHelp}">
                                <sun:textField id="Classpath" columns="$int{55}" text="#{requestScope.classPath}" />      
                            </sun:property>
                            <sun:property id="NativeLibraryProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jvm.NativeLibraryPath}" helpText="$resource{i18n.jvm.NativeLibraryPathHelp}">
                                <sun:textField id="NativeLibrary" columns="$int{55}" text="#{requestScope.nativeLibrary}"/>      
                            </sun:property> 
                            "<br /><br />
                        </sun:propertySheetSection>
                    </sun:propertySheet>  
#include "/shared/jvmOptionsTable.inc"                
                
	<sun:hidden id="helpKey" value="$attribute{helpKey}" />
            </sun:form>
#include "/shared/changeButtonsJS.inc"
</define>
</composition>

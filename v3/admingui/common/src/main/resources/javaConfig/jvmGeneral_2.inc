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
<!-- shared/jvmGeneral_2.jsf -->

#include "/common/shared/alertMsg.inc"
                <!-- Page Title -->
                <sun:title id="propertyContentPage" title="$resource{i18nc.jvm.GeneralTitle}" 
                    helpText="$resource{i18nc.jvm.GeneralPageHelp}">
#include "/common/shared/editPageButtons.inc"             
                </sun:title>
                    <!-- PropertySheet .... -->
                    <sun:propertySheet id="propertySheet" requiredFields="true">	
                        <!-- Text Field section -->               
                        <sun:propertySheetSection id="propertSectionTextField">
                            <sun:property id="javaHomeProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.JavaHomeLabel}" helpText="$resource{i18nc.jvm.JavaHomeHelp}">
                                <sun:textField id="JavaHome"  styleClass="required" columns="$int{55}" text="#{pageSession.valueMap['JavaHome']}" required="#{true}"/> 
                            </sun:property>                       
                            <sun:property id="JavacOptionsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.JavacOptionsLabel}" helpText="$resource{i18nc.jvm.JavacOptionsHelp}">                           
                                <sun:textField id="JavacOptions" columns="$int{55}" text="#{pageSession.valueMap['JavacOptions']}"/>     
                            </sun:property>                        
                          <sun:property id="debugEnabledProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.DebugLabel}" helpText="$resource{i18nc.jvm.DebugHelp}">
                                <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{pageSession.valueMap['DebugEnabled']}" selectedValue="true" />      
                            </sun:property>
                            <sun:property id="DebugOptionsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.DebugOptionsLabel}" helpText="$resource{i18nc.jvm.DebugOptionsHelp}">
                                <sun:textField id="DebugOptions" columns="$int{55}" text="#{pageSession.valueMap['DebugOptions']}" />      
                            </sun:property>
                            <sun:property id="RmicOptionsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.RMICompileOptionsLabel}" helpText="$resource{i18nc.jvm.RMICompileOptionsHelp}">
                                <sun:textField id="RmicOptions" columns="$int{55}" text="#{pageSession.valueMap['RmicOptions']}"/>      
                            </sun:property> 
                            <sun:property id="bytecodePreprocessorProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.BytecodePreprocessorLabel}" helpText="$resource{i18nc.jvm.BytecodePreprocessorHelp}">
                                <sun:textArea id="bytecodePreprocessor" rows="$int{8}" columns="$int{55}" text="#{pageSession.valueMap['BytecodePreprocessors']}"/>      
                            </sun:property>                  
                            "<br /><br />
                        </sun:propertySheetSection>

                    </sun:propertySheet>    
#include "/common/shared/propertyDescTable.inc"
                    <sun:hidden id="helpKey" value="$resource{help.serverInstJvmGeneral}" />
            </sun:form>
</define>
</composition>

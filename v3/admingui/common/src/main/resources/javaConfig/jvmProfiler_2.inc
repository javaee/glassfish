
<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 
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
<!-- javaConfig/jvmProfiler_2.inc -->

#include "/common/shared/alertMsg.inc"
                <!-- Page Title -->
                <sun:title id="propertyContentPage" title="$resource{i18nc.jvm.ProfilerPageName}"
                    helpText="$resource{i18nc.jvm.ProfilerPageHelp}">
    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18nc.button.CreateProfiler}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')){
                        disableAllButtons();
                        return submitAndDisable(this, '$resource{i18n.button.Processing}');
                    }" >
                <!command
                    createProxy(parentObjectNameStr="#{pageSession.parentObjectNameStr}" childType="#{pageSession.childType}"
                        attrs="#{pageSession.valueMap}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                        result => $attribute{objStr});
                    getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                    getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
                    saveJvmOptionValues(objectNameStr="amx:pp=/domain/configs/config[server-config]/java-config,type=profiler" options="$attribute{newList}");    
                    //prepareSuccessfulMsg();
                    gf.redirect(page="#{selfPage}?configName=#{pageSession.configName}");
                />
            </sun:button>
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')){
                        disableAllButtons();
                        return submitAndDisable(this, '$resource{i18n.button.Processing}');
                    }" >
                <!command
                    saveBeanAttributes(objectNameStr="#{pageSession.profilerObjectNameStr}"  attrs="#{pageSession.valueMap}"
                        skipAttrs="#{pageSession.skipAttrsList}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                    );
                    getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                    getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
                    saveJvmOptionValues(objectNameStr="#{pageSession.profilerObjectNameStr}" options="$attribute{newList}");
                prepareSuccessfulMsg();
                gf.redirect(page="#{selfPage}?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}&name=#{pageSession.Name}&configName=#{pageSession.configName}");
                />
            </sun:button>

            <sun:button id="deleteButton" rendered="#{edit}" text="$resource{i18nc.button.DeleteProfiler}"
                onClick="if ( getConfirm(this,'$pageSession{confirmDeleteMsg}') )
                    { disableAllButtons(); return submitAndDisable(this, '$resource{i18n.button.Processing}');}
                    else {return false;} " >
                <!command
                    deleteChild(objectNameStr="#{pageSession.parentObjectNameStr}" type="profiler")
                    gf.redirect(page="#{selfPage}?configName=#{pageSession.configName}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>

        </sun:panelGroup>
    </facet>
        </sun:title>
        <!-- PropertySheet .... -->
        <sun:propertySheet id="propertySheet">
            <!-- Text Field section -->
            <sun:propertySheetSection id="propertSectionTextField">
                <sun:property id="profilerNameProp" rendered="#{!edit}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.ProfilerName}">
                    <sun:textField id="ProfilerName"  styleClass="required" columns="$int{55}" text="#{pageSession.valueMap['Name']}" required="#{true}"/>
                </sun:property>
                <sun:property id="profilerNamePropEdit" rendered="#{edit}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.ProfilerName}">
                    <sun:staticText id="ProfilerName2" text="#{pageSession.valueMap['Name']}"/>
                </sun:property>
               <sun:property id="profilerEnabledProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
                    <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{pageSession.valueMap['Enabled']}" selectedValue="true" />
               </sun:property>
                <sun:property id="ClasspathProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.Classpath}" helpText="$resource{i18nc.jvm.ClasspathHelp}">
                    <sun:textField id="ClasspathLabel" columns="$int{55}" text="#{pageSession.valueMap['Classpath']}" />
                </sun:property>
                <sun:property id="NativeLibraryProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.NativeLibraryPath}" helpText="$resource{i18nc.jvm.NativeLibraryPathHelp}">
                    <sun:textField id="NativeLibrary" columns="$int{55}" text="#{pageSession.valueMap['NativeLibraryPath']}"/>
                </sun:property>
                "<br /><br />
            </sun:propertySheetSection>
        </sun:propertySheet>
#include "/common/javaConfig/jvmOptionsTable.inc"
                
	<sun:hidden id="helpKey" value="$resource{help.serverInstProfiler}" />
            </sun:form>
#include "/common/shared/changeButtonsJS.inc"
</define>
</composition>

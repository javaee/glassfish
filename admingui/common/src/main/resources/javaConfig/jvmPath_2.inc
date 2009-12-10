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
<!-- shared/jvmPath_2.inc -->

#include "/common/shared/alertMsg.inc"      
                <!-- Page Title -->
<sun:title id="propertyContentPage" title="$resource{i18nc.jvm.PathSettingsTitle}" helpText="$resource{i18nc.jvm.PathSettingsPageHelp}" >
    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" text="$resource{i18n.button.Save}">
                <!command
                  <!-- commented out. refer to issue# 10967
                    formatPathSeperatorStringsforSaving(string="#{pageSession.valueMap['ServerClasspath']}"  formattedString="#{requestScope.scp}");
                    mapPut(map="#{pageSession.valueMap}" key="ServerClasspath" value="$attribute{scp}");
                    formatPathSeperatorStringsforSaving(string="#{pageSession.valueMap['SystemClasspath']}"  formattedString="#{requestScope.syscp}");
                    mapPut(map="#{pageSession.valueMap}" key="SystemClasspath" value="$attribute{syscp}");
                    formatPathSeperatorStringsforSaving(string="#{pageSession.valueMap['ClasspathPrefix']}"  formattedString="#{requestScope.cpp}");
                    mapPut(map="#{pageSession.valueMap}" key="ClasspathPrefix" value="$attribute{cpp}");
                    formatPathSeperatorStringsforSaving(string="#{pageSession.valueMap['ClasspathSuffix']}"  formattedString="#{requestScope.cps}");
                    mapPut(map="#{pageSession.valueMap}" key="ClasspathSuffix" value="$attribute{cps}");
                  -->

                    formatPathSeperatorStringsforSaving(string="#{pageSession.valueMap['NativeLibraryPathPrefix']}"  formattedString="#{requestScope.npp}");
                    mapPut(map="#{pageSession.valueMap}" key="NativeLibraryPathPrefix" value="$attribute{npp}");
                    formatPathSeperatorStringsforSaving(string="#{pageSession.valueMap['NativeLibraryPathSuffix']}"  formattedString="#{requestScope.nps}");
                    mapPut(map="#{pageSession.valueMap}" key="NativeLibraryPathSuffix" value="$attribute{nps}");
                    saveBeanAttributes(objectNameStr="#{pageSession.objectNameStr}"  attrs="#{pageSession.valueMap}"
                        skipAttrs="#{pageSession.skipAttrsList}"
                        convertToFalse="#{pageSession.convertToFalseList}");
                    prepareSuccessfulMsg();
                    gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>

            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" rendered="#{pageSession.showCancelButton}" text="$resource{i18n.button.Cancel}" >
                <!command
                    gf.redirect(page="#{parentPage}?configName=#{configName}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>

</sun:title>                  
    <!-- PropertySheet .... -->
    <sun:propertySheet id="propertySheet">
        <!-- Text Field section -->
        <sun:propertySheetSection id="propertSectionTextField">

            <sun:property id="NativeLibraryPathPrefixProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.NativeLibraryPathPrefix}" helpText="$resource{i18nc.jvm.NativeLibraryPathPrefixHelp}">
                <sun:textArea id="NativeLibraryPathPrefix" rows="$int{4}" columns="$int{75}" text="#{pageSession.valueMap['NativeLibraryPathPrefix']}"/>
            </sun:property>
            <sun:property id="NativeLibraryPathSuffixProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.NativeLibraryPathSuffix}" helpText="$resource{i18nc.jvm.NativeLibraryPathSuffixHelp}">
                <sun:textArea id="NativeLibraryPathSuffix" rows="$int{4}" columns="$int{75}" text="#{pageSession.valueMap['NativeLibraryPathSuffix']}"/>
            </sun:property>
            <sun:property  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" >
                <sun:staticText text=" " />
            </sun:property>
        </sun:propertySheetSection>

            
        <sun:propertySheetSection label="">
        <!-- disable the following. refer to issue# 10967
             Woodstock has issue of setting readOnly for textArea, we need to use disable instead
         -->

            <sun:property id=""  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.unsupportedSectionHelp}" >
                <sun:staticText text=""/>
                "<br/>
            </sun:property>

            <sun:property id="EnvironmentClasspathProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.EnvironmentClasspath}"  helpText="$resource{i18nc.jvm.EnvironmentClasspathHelp}">
                <sun:checkbox disabled="true" label="$resource{i18n.common.Ignore}" selected="#{pageSession.valueMap['EnvClasspathIgnored']}" selectedValue="true" />
            </sun:property>

            <sun:property id="ServerClasspathProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.ServerClasspath}" helpText="$resource{i18nc.jvm.ServerClasspathHelp}">
                <sun:staticText id="ServerClasspath"  text="#{pageSession.valueMap['ServerClasspath']}"/>
            </sun:property>
            
            <sun:property id="SystemClasspathProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.SystemClasspath}" helpText="$resource{i18nc.jvm.SystemClasspathHelp}">
                <sun:textArea disabled="true"  id="systemClasspath" rows="$int{4}" columns="$int{75}" text="#{pageSession.valueMap['SystemClasspath']}"/>
            </sun:property>
            <sun:property id="ClasspathPrefixProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.ClasspathPrefix}" helpText="$resource{i18nc.jvm.ClasspathPrefixHelp}">
                <sun:textArea disabled="true"  id="ClasspathPrefix" rows="$int{4}" columns="$int{75}" text="#{pageSession.valueMap['ClasspathPrefix']}"/>
            </sun:property>
            <sun:property id="ClasspathSuffixProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.jvm.ClasspathSuffix}" helpText="$resource{i18nc.jvm.ClasspathSuffixHelp}">
                <sun:textArea disabled="true"  id="ClasspathSuffix" rows="$int{4}" columns="$int{75}" text="#{pageSession.valueMap['ClasspathSuffix']}"/>
            </sun:property>
            
        </sun:propertySheetSection>
    </sun:propertySheet>
"<br/><br/>
	<sun:hidden id="helpKey" value="$resource{help.serverInstJvmPathSettings}" />
            </sun:form>
</define>
</composition>

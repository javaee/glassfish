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
<!-- lifecycle.inc -->
<sun:propertySheet id="propertySheet">
    <!-- Text Field section -->
    <sun:propertySheetSection id="propertSectionTextField">

    <sun:property id="IdStaticProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" >
        <sun:staticText id="IdStatic" text="#{pageSession.valueMap['Name']}"/>
    </sun:property>

    <sun:property id="IdTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}">
        <sun:textField id="IdText" styleClass="required" text="#{pageSession.valueMap['Name']}" columns="$int{55}" required="#{true}" />
    </sun:property>

    <sun:property id="classNameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.lifecycleModule.classname}" helpText="$resource{i18nc.lifecycleModule.classnameHelp}" >
        <sun:textField id="classname" styleClass="required" columns="$int{70}" text="#{pageSession.valueMap['class-name']}"  required="#{true}" />
    </sun:property> 

    <sun:property id="pathProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}"  label="$resource{i18nc.lifecycleModule.classPath}" helpText="$resource{i18nc.lifecycleModule.classPathHelp}" >
        <sun:textField id="classPath" columns="$int{70}" text="#{pageSession.valueMap['classpath']}" />
    </sun:property> 

    <sun:property id="loadOrderProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.lifecycleModule.loadOrder}"  helpText="$resource{i18nc.lifecycleModule.loadOrderHelp}" >
        <sun:textField id="loadOrder" styleClass="integer" columns="$int{10}" text="#{pageSession.valueMap['load-order']}" />
    </sun:property> 

    

    <sun:property id="statusProp" rendered="#{useCheckBox}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
        <sun:checkbox label="$resource{i18n.common.Enabled}"  selected="#{pageSession.valueMap2['Enabled']}" selectedValue="true" />
   </sun:property>

    <sun:property id="failProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.lifecycleModule.onLoadFailure}" helpText="$resource{i18nc.lifecycleModule.onLoadFailureHelp}" >
        <sun:checkbox label="$resource{i18nc.lifecycleModule.preventStart}" selected="#{pageSession.valueMap['is-failure-fatal']}"  selectedValue="true" />
   </sun:property>

   <sun:property id="vsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.deploy.virtualServerLabel}" helpText="$resource{i18n.deploy.vsHelp}">
       <sun:listbox id="vs" immediate="#{true}" multiple="#{true}"  rows="$int{4}"
            labels="$pageSession{availableVS}"
            selected="#{pageSession.valueMap2['selectedVS']}" >
            <!beforeCreate
                getVsForDeployment(result="#{pageSession.vsList}");
                getListBoxOptions(
                    availableList="#{pageSession.vsList}",
                    selectedCommaString="#{pageSession.valueMap2['VirtualServers']}",
                    availableListResult="#{pageSession.availableVS}",
                    selectedOptions="#{pageSession.valueMap2['selectedVS']}"
                    addEmptyFirstChoice="true");
            />
        </sun:listbox>
    </sun:property>

    <sun:property id="descProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
        <sun:textField id="desc" columns="$int{70}" text="#{pageSession.valueMap['Description']}" />
    </sun:property>

    </sun:propertySheetSection>
</sun:propertySheet>

    "<br /><br />


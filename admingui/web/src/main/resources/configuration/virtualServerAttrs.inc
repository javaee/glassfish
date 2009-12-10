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

<!-- web/configuration/virtualServerAttrs.inc -->
<sun:propertySheet>
    <sun:propertySheetSection id="propertSectionTextField">
        <sun:property id="IdStaticProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.vs.Id}" >
            <sun:staticText id="IdStatic" text="#{pageSession.Name}"/>
        </sun:property>

        <sun:property id="IdTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.vs.Id}">
            <sun:textField id="IdText" styleClass="required" text="#{pageSession.valueMap['Id']}" columns="$int{55}" required="#{true}" />
        </sun:property>

        <sun:property id="hostsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.vs.Hosts}" helpText="$resource{i18n_web.vs.HostsHelp}">
            <sun:textField id="Hosts" styleClass="required" columns="$int{55}" text="#{pageSession.valueMap['Hosts']}" required="#{true}"/>
        </sun:property>

        <sun:property id="stateProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.vs.State}">
             <sun:radioButton id="on"  name="stateGp" label="$resource{i18n_web.vs.StateOn}" selected="#{pageSession.valueMap['State']}" selectedValue="on" />
             "<br />
             <sun:radioButton id="off"  name="stateGp" label="$resource{i18n_web.vs.StateOff}" selected="#{pageSession.valueMap['State']}" selectedValue="off" />
             "<br />
             <sun:radioButton id="disabled"  name="stateGp" label="$resource{i18n_web.vs.StateDisabled}" selected="#{pageSession.valueMap['State']}" selectedValue="disabled" />
             "<br />
         </sun:property>

         <sun:property id="enableLog"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.vs.sso}">
             <sun:radioButton id="httpS"  name="ssoGroup" label="$resource{i18n_web.vs.controlByHS}" selected="#{pageSession.valueMap['SsoEnabled']}" selectedValue="inherit" />
             " <b>$resource{i18n_web.vs.httpLink}</b>
             # <sun:helpInline id="flag1H" style="padding: 4pt" style="font-size: 8pt" text=""/>
             "<br />
             <sun:radioButton id="ena"  name="ssoGroup" label="$resource{i18n_web.vs.alwaysEnable}" selected="#{pageSession.valueMap['SsoEnabled']}" selectedValue="true" />
             "<br />
             <sun:radioButton id="dis"  name="ssoGroup" label="$resource{i18n_web.vs.alwaysDisable}" selected="#{pageSession.valueMap['SsoEnabled']}" selectedValue="false" />
             "<br />
         </sun:property>

        <sun:property id="nwProps"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.vs.NetworkListeners}" helpText="$resource{i18n_web.vs.NetworkListenersHelp}">
            <sun:listbox id="nw" immediate="#{true}" multiple="#{true}"  rows="$int{4}" 
                labels="$pageSession{availableNetworkListeners}"
                selected="#{pageSession.selectedNetworkListeners}" >
                <!beforeCreate
                    getChildrenByType(parentObjectNameStr="amx:pp=/domain/configs/config[#{pageSession.configName}]/network-config,type=network-listeners"
                        type="network-listener" result="#{pageSession.networkListeners}");
                    getListBoxOptions(
                        availableList="#{pageSession.networkListeners}",
                        selectedCommaString="#{pageSession.valueMap['NetworkListeners']}",
                        availableListResult="#{pageSession.availableNetworkListeners}",
                        selectedOptions="#{pageSession.selectedNetworkListeners}"
                        addEmptyFirstChoice="true");
                />
                </sun:listbox>
            </sun:property>

        <sun:property id="webProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.vs.DefaultWeb}" helpText="$resource{i18n_web.vs.DefaultWebHelp}">
            <sun:dropDown id="Web" labels="$pageSession{modules}" selected="#{pageSession.valueMap['DefaultWebModule']}" />
            <!beforeCreate
                getApplicationBySnifferType(type="web" fullName="#{true}" result="#{requestScope.modList}")
                addEmptyFirstElement(in="$attribute{modList}"  out="#{pageSession.modules}")
            />
        </sun:property>
        <sun:property id="logFileProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.vs.LogFile}" helpText="$resource{i18n_web.vs.LogFileHelp}">
            <sun:textField id="LogFile" columns="$int{55}" text="#{pageSession.valueMap['LogFile']}" />
        </sun:property>

        <sun:property id="docroot"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.vs.docroot}" helpText="$resource{i18n_web.vs.docrootHelp}">
            <sun:textField id="docroot" columns="$int{55}" text="#{pageSession.valueMap['Docroot']}" />
        </sun:property>

    </sun:propertySheetSection>

    <sun:propertySheetSection id="al" label="$resource{i18n_web.vs.accessLog}">

        <sun:property id="enableLog"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.vs.accessLogging}">
             <sun:radioButton id="httpS"  name="accessLoggingFlagGp" label="$resource{i18n_web.vs.controlByHS}" selected="#{pageSession.valueMap['AccessLoggingEnabled']}" selectedValue="inherit" />
             " <b>$resource{i18n_web.vs.httpLink}</b>
             # <sun:helpInline id="flag1H" style="padding: 4pt" style="font-size: 8pt" text=""/>
             "<br />
             <sun:radioButton id="ena"  name="accessLoggingFlagGp" label="$resource{i18n_web.vs.alwaysEnable}" selected="#{pageSession.valueMap['AccessLoggingEnabled']}" selectedValue="true" />
             "<br />
             <sun:radioButton id="dis"  name="accessLoggingFlagGp" label="$resource{i18n_web.vs.alwaysDisable}" selected="#{pageSession.valueMap['AccessLoggingEnabled']}" selectedValue="false" />
             "<br />
         </sun:property>

        <sun:property id="accesslog"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.vs.path}" helpText="$resource{i18n_web.vs.pathHelp}">
            <sun:textField id="accesslog" columns="$int{55}" text="#{pageSession.valueMap['AccessLog']}" />
        </sun:property>

    </sun:propertySheetSection>
</sun:propertySheet>
"<br><br>


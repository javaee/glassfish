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
<!-- securityAttrs.inc -->

<sun:propertySheet id="propertySheet">
    <!-- Text Field section -->
    <sun:propertySheetSection id="propertSectionTextField">
        <sun:property id="securityManagerProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.SecurityManager}"  helpText="$resource{i18nc.security.SecurityManagerHelp}">
            <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{pageSession.securityManagerStatus}" selectedValue="true"  >
            <!beforeCreate
                getSecurityManagerValue(configName="#{pageSession.configName}" value="#{pageSession.securityManagerStatus}")
            />
            </sun:checkbox>
        </sun:property>
        <sun:property id="auditLoggingProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.AuditLogging}" helpText="$resource{i18nc.security.AuditLoggingHelp}">
            <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{pageSession.valueMap['AuditEnabled']}" selectedValue="true" />
        </sun:property>

      <sun:property id="defaultRealmProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.DefaultRealm}" helpText="$resource{i18nc.security.DefaultRealmHelp}">
            <sun:dropDown id="DefaultRealm" labels="$pageSession{realms}" selected="#{pageSession.valueMap['DefaultRealm']}" />
            <!beforeCreate
                getChildrenByType(parentObjectNameStr="amx:pp=/domain/configs/config[#{pageSession.configName}],type=security-service"
                type="auth-realm" result="#{pageSession.realms}");
            />
        </sun:property>
        <sun:property id="defaultPrincipalProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.DefaultPrincipal}" helpText="$resource{i18nc.security.DefaultPrincipalHelp}">
            <sun:textField id="DefaultPrincipal" columns="$int{55}" text="#{pageSession.valueMap['DefaultPrincipal']}"/>
        </sun:property>
        <sun:property id="defaultPrincipalPasswordProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.DefaultPrincipalPassword}" helpText="$resource{i18nc.security.DefaultPrincipalPasswordHelp}">
            <sun:passwordField id="DefaultPrincipalPassword" columns="$int{55}" text="#{pageSession.valueMap['DefaultPrincipalPassword']}"/>
        </sun:property>
      <sun:property id="jaccProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.Jacc}" helpText="$resource{i18nc.security.JaccHelp}">
            <sun:dropDown id="Jacc" labels="$pageSession{jaccs}" selected="#{pageSession.valueMap['Jacc']}" />
            <!beforeCreate
                getChildrenByType(parentObjectNameStr="amx:pp=/domain/configs/config[#{pageSession.configName}],type=security-service" type="jacc-provider" result="#{pageSession.jaccs}");
            />
        </sun:property>
     <sun:property id="auditModulesProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.AuditModules}" helpText="$resource{i18nc.security.AuditModulesHelp}">
            <sun:listbox id="AuditModules" immediate="#{true}" multiple="#{true}"  rows="$int{4}"
                labels="$pageSession{availableAudits}"
                selected="#{pageSession.selectedAuditModules}" >
                <!beforeCreate
                    getChildrenByType(parentObjectNameStr="amx:pp=/domain/configs/config[#{pageSession.configName}],type=security-service" type="audit-module" result="#{pageSession.audits}");
                    getListBoxOptions(
                        availableList="#{pageSession.audits}",
                        selectedCommaString="#{pageSession.valueMap['AuditModules']}",
                        availableListResult="#{pageSession.availableAudits}",
                        selectedOptions="#{pageSession.selectedAuditModules}"
                        addEmptyFirstChoice="true");
                />
            </sun:listbox>
        </sun:property>
       <sun:property id="roleMappingProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.RoleMapping}" helpText="$resource{i18nc.security.RoleMappingHelp}">
            <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{pageSession.valueMap['ActivateDefaultPrincipalToRoleMapping']}" selectedValue="true" />
        </sun:property>
        <sun:property id="mappedPrincipalClassProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.security.MappedPrincipalClass}" helpText="$resource{i18nc.security.MappedPrincipalClassHelp}">
            <sun:textField id="MappedPrincipalClass" columns="$int{55}" text="#{pageSession.valueMap['MappedPrincipalClass']}"/>
        </sun:property>

        "<br /><br />
    </sun:propertySheetSection>

</sun:propertySheet>
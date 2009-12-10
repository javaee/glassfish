
<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 
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
<!-- jca/workSecurityMapAttr.inc -->

<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">
    <sun:propertySheetSection id="propertSectionTextField">
        <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{sheetId});
        />

        <sun:property id="mapName" rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.securityMapName}">
            <sun:staticText id="mapName" text="#{pageSession.Name}"/>
        </sun:property>

        <sun:property id="mapNameNew" rendered="#{!edit}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.securityMapName}" >
            <sun:textField id="mapName"   styleClass="required" text="#{pageSession.valueMap['Name']}" required="#{true}"/>
        </sun:property>
                <sun:property id="groupProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.workSecurityMap.Group}">

            <sun:radioButton id="optA"  name="usersOption" label="$resource{i18njca.workSecurityMap.GroupMapping}" selected="#{requestScope.usersOptionG}" selectedValue="users"
                    onClick="enableWorkSecurityMapFields('users'); "
                />
            "<br /><br />
            <sun:textField id="eisgrouptext" columns="$int{55}" text="#{pageSession.valueMap2['GroupMapping']}" >
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{eisuserGroupsId});
                    />
            </sun:textField>
            "<br />
            <sun:helpInline id="eisgroupHelp"text="$resource{i18njca.workSecurityMap.EISGroupInlineHelp}"/>
            "<br />
                    <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{grpPropId});
                />
        </sun:property>

        <sun:property id="principalsProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.workSecurityMap.Principal}">
                   <sun:radioButton id="optB"  name="usersOption" label="$resource{i18njca.workSecurityMap.PrincipalMapping}" selected="#{requestScope.usersOptionP}" selectedValue="principals"
                    onClick="javascript: enableWorkSecurityMapFields('principals');"
                />
            "<br /><br />
            <sun:textField id="eisprincipals" columns="$int{55}" text="#{pageSession.valueMap2['PrincipalMapping']}" >
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{eisprincipalsId});
                    />
            </sun:textField>
            "<br />
                    <sun:helpInline id="eisprincipalsHelp"text="$resource{i18njca.workSecurityMap.EISPrincipalInlineHelp}"/>
            "<br />
                    <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{pPropId});
                />
        </sun:property>
        <sun:property id="resAdaptorProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resourceAdapter}" helpText="$resource{i18njca.adminObjectResource.resourceAdapterHelp}">
            <sun:dropDown id="resAdaptorProp" selected="#{pageSession.valueMap['ResourceAdapterName']}" labels="$pageSession{connectorList}"  >
                <!beforeCreate
                     getApplicationBySnifferType(type="connector"  result="#{pageSession.connectorList}")
                     listAdd(list="#{pageSession.connectorList}" value="jmsra")
                />
           </sun:dropDown>
        </sun:property>
     <sun:property id="descriptionProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
            <sun:textField id="descAdaptor" columns="$int{55}" text="#{pageSession.valueMap['Description']}" />
        </sun:property>
        <sun:property id="status"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}" >
            <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{pageSession.valueMap['Enabled']}" selectedValue="true" />
       </sun:property>

</sun:propertySheetSection>
</sun:propertySheet>

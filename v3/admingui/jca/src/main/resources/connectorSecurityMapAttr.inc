
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
<!-- jca/connectorSecurityMapAttr.inc -->

<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">
    <sun:propertySheetSection id="propertSectionTextField">
        <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{sheetId});
        />
        <sun:property id="poolName" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.poolName}">
            <sun:staticText id="poolName" text="#{pageSession.Name}"/>
        </sun:property>  

        <sun:property id="mapName" rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.securityMapName}">
            <sun:staticText id="mapName" text="#{pageSession.mapName}"/>
        </sun:property>

        <sun:property id="mapNameNew" rendered="#{!edit}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.securityMapName}" >
            <sun:textField id="mapName"   styleClass="required" text="#{pageSession.valueMap['Name']}" required="#{true}"/>
        </sun:property> 

        <sun:property id="groupProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.userGroups}"  helpText="$resource{i18njca.connectorSecurityMap.userGroupsHelp}">
                <sun:radioButton id="optA"  name="usersOption" selected="#{requestScope.usersOptionU}" selectedValue="users"
                    onClick="enableClassnameFields('users'); "
                />         
            <sun:textField id="group" columns="$int{55}" text="#{pageSession.valueMap['UserGroup']}" >
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{userGroupsId});
                    />
            </sun:textField>
            <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{grpPropId});
                />
        </sun:property>

        <sun:property id="principalsProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.principals}"  helpText="$resource{i18njca.connectorSecurityMap.principalsHelp}">
                   <sun:radioButton id="optB"  name="usersOption" selected="#{requestScope.usersOptionP}" selectedValue="principals"
                    onClick="javascript: enableClassnameFields('principals');"
                />      
            <sun:textField id="principals" columns="$int{55}" text="#{pageSession.valueMap['Principal']}" >
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{principalsId});
                    />
            </sun:textField>
            <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{pPropId});
                />
        </sun:property>
        "<br /><br />
    </sun:propertySheetSection>

    <sun:propertySheetSection id="propertSectionTextField2" label="$resource{i18njca.connectorSecurityMap.backendPrincipal}">
        <sun:property id="userName" rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.userName}">
            <sun:textField id="userName" styleClass="required" columns="$int{55}" text="#{pageSession.userName}" required="#{true}" />
        </sun:property>
        <sun:property id="userNameEdit" rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.userName}">
            <sun:textField id="userNameEdit" styleClass="required" columns="$int{55}" text="#{pageSession.valueMap2['UserName']}" required="#{true}" />
        </sun:property>
        <sun:property id="password" rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.password}">
            <sun:textField id="password" styleClass="required" columns="$int{55}" text="#{pageSession.password}" required="#{true}" />
        </sun:property>
        <sun:property id="passwordEdit" rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.connectorSecurityMap.password}">
            <sun:textField id="passwordEdit" styleClass="required" columns="$int{55}" text="#{pageSession.valueMap2['Password']}" required="#{true}" />
        </sun:property>
    </sun:propertySheetSection>

</sun:propertySheet>    

            

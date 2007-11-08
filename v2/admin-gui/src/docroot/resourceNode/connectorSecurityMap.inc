
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
<!-- resourceNode/connectorSecurityMap.inc -->


<!-- Page Title -->
<sun:title id="propertyContentPage" title="#{pageTitle}" helpText="#{pageTitleHelp}" >
<!-- Buttons  -->                    
<!facet pageButtonsTop>
<sun:panelGroup id="topButtons">
    <sun:button id="saveButton" rendered="#{edit}" text="#{saveButtonLabel}" 
        onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " > 
       <!command
       prepareSuccessfulMsg();
       saveConnectorSecurityMap(
                edit="#{edit}"
                securityMapName="#{securityMapName}"
                poolName="#{poolName}"
                usersOption="#{usersOptionGrp}",
                userGroups="#{userGroups}"
                principals="#{principals}"
                userName="#{userName}"
                password="#{password}"
            );
        setAttribute(key="securityMapName", value="#{securityMapName}");
        setAttribute(key="poolName", value="#{poolName}");
        navigate(page="/resourceNode/connectorSecurityMapEdit.jsf");
        />    
    </sun:button>
    <sun:button id="backButton" rendered="#{edit}" primary="#{false}" immediate="#{true}" text="$resource{i18n.button.Back}" >
        <!command
            redirect(page="connectorSecurityMaps.jsf?name=#{poolName}");
        />
    </sun:button>

    <sun:button id="newButton" rendered="#{!edit}" text="#{saveButtonLabel}" 
            onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " > 
       <!command
            saveConnectorSecurityMap(
                edit="#{edit}"
                securityMapName="#{securityMapName}"
                poolName="#{poolName}"
                usersOption="#{usersOptionGrp}",
                userGroups="#{userGroups}"
                principals="#{principals}"
                userName="#{userName}"
                password="#{password}"
            );
            redirect(page="connectorSecurityMaps.jsf?name=#{poolName}");
        />    
    </sun:button>
    <sun:button id="cancelButton" rendered="#{!edit}" primary="#{false}" immediate="#{true}" text="$resource{i18n.button.Cancel}" >
        <!command
            redirect(page="connectorSecurityMaps.jsf?name=#{poolName}");
        />
    </sun:button>
</sun:panelGroup>
</facet>     
 </sun:title>
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">
    <sun:propertySheetSection id="propertSectionTextField">
        <sun:property id="poolName" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.connectorSecurityMap.poolName}">
            <sun:staticText id="poolName" text="#{poolName}"/> 
        </sun:property>  

        <sun:property id="mapName" rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.connectorSecurityMap.securityMapName}">
            <sun:staticText id="mapName" text="#{securityMapName}"/> 
        </sun:property>

        <sun:property id="mapNameNew" rendered="#{!edit}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.connectorSecurityMap.securityMapName}" >
            <sun:textField id="mapName"   styleClass="required" text="#{securityMapName}" required="#{true}"/>
        </sun:property> 

        <sun:property id="group" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.connectorSecurityMap.userGroups}"  helpText="$resource{i18n.connectorSecurityMap.userGroupsHelp}"> 
                <sun:radioButton id="optA"  name="usersOptionGrp" selected="#{requestScope.usersOption}" selectedValue="users" 
                    onClick="enableClassnameFields('users'); "
                />         
            <sun:textField id="group" columns="$int{55}" text="#{requestScope.userGroups}" >      
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{userGroupsId});
                    />
            </sun:textField>
        </sun:property>

        <sun:property id="principals" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.connectorSecurityMap.principals}"  helpText="$resource{i18n.connectorSecurityMap.principalsHelp}"> 
                   <sun:radioButton id="optB"  name="usersOptionGrp" selected="#{requestScope.usersOption}" selectedValue="principals"
                    onClick="javascript: enableClassnameFields('principals');"
                />      
            <sun:textField id="principals" columns="$int{55}" text="#{requestScope.principals}" >      
                    <!afterCreate
                        getClientId(component="$this{component}" clientId=>$page{principalsId});
                    />
            </sun:textField>
        </sun:property>
        "<br /><br />
    </sun:propertySheetSection>

    <sun:propertySheetSection id="propertSectionTextField2" label="$resource{i18n.connectorSecurityMap.backendPrincipal}">
        <sun:property id="userName" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.connectorSecurityMap.userName}"> 
            <sun:textField id="userName" styleClass="required" columns="$int{55}" text="#{requestScope.userName}" required="#{true}" />      
        </sun:property>

        <sun:property id="password" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.connectorSecurityMap.password}"> 
            <sun:textField id="password" styleClass="required" columns="$int{55}" text="#{requestScope.password}" required="#{true}" />      
        </sun:property>
    </sun:propertySheetSection>

</sun:propertySheet>    

            

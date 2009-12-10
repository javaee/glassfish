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
<sun:table id="resourcesTable" title="#{tableTitle}"
           deselectMultipleButton="$boolean{true}"
           deselectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)"
           selectMultipleButton="$boolean{true}"
           selectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)" >
    <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{tableId});
    />

#include "/jms/shared/tableButtons.inc"

    <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableRowGroupId});
        />
        <sun:tableColumn id="colSelect" selectId="select" rowHeader="$boolean{false}">
            <sun:checkbox id="select"
                          selected="#{td.value.selected}"
                          selectedValue="$boolean{true}"
                          onClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\'); admingui.table.initAllRows(\\\\\'#{pageSession.tableId}\\\\\');', 0);"/>
        </sun:tableColumn>

        <sun:tableColumn id="colName" headerText="$resource{i18njms.resourcesTable.colJndiName}" rendered="#{!isConnPage}" sort="Name" rowHeader="$boolean{true}">
            <sun:hyperlink id="link" text="#{td.value.Name}" url="#{pageSession.editLink}?name=#{td.value.Name}" />
        </sun:tableColumn>

        <sun:tableColumn id="colPoolName" headerText="$resource{i18njms.resourcesTable.colPoolName}" rendered="#{isConnPage}" sort="Name" rowHeader="$boolean{true}">
            <sun:hyperlink id="link" text="#{td.value.Name}" url="#{pageSession.editLink}?name=#{td.value.Name}&jndiName=#{td.value.JndiName}" />
        </sun:tableColumn>

        <sun:tableColumn id="jndiName" headerText="$resource{i18njms.resourcesTable.colJndiName}" rendered="#{isConnPage}" sort="Name" rowHeader="$boolean{true}">
            <sun:staticText id="jndi" text="#{td.value.JndiName}"  />
        </sun:tableColumn>

        <sun:tableColumn id="colEnabled1" headerText="$resource{i18njms.resourcesTable.colEnabled}" rendered="#{isConnPage}" sort="Enabled" rowHeader="$boolean{true}">
            <staticText id="enabled" value="#{requestScope.connRes and requestScope.resourceRef}">
                <!event type="beforeEncode">
                    getProxyAttribute(objectNameStr="amx:pp=/domain/resources,type=connector-resource,name=#{td.value.JndiName}", attrName="Enabled", value="#{requestScope.connRes}");
                    getProxyAttribute(objectNameStr="amx:pp=/domain/servers/server[server],type=resource-ref,name=#{td.value.JndiName}", attrName="Enabled", value="#{requestScope.resourceRef}");
                </event>
            </staticText>
        </sun:tableColumn>

        <sun:tableColumn id="colEnabled2" headerText="$resource{i18njms.resourcesTable.colEnabled}" rendered="#{!isConnPage}" sort="Enabled" rowHeader="$boolean{true}">
            <staticText id="enabled" value="#{td.value.Enabled and requestScope.resourceRef}">
                <!event type="beforeEncode">
                    getProxyAttribute(objectNameStr="amx:pp=/domain/servers/server[server],type=resource-ref,name=#{td.value.Name}", attrName="Enabled", value="#{requestScope.resourceRef}");
                </event>
            </staticText>
        </sun:tableColumn>

        <sun:tableColumn id="colConnDefName" headerText="$resource{i18njms.resourcesTable.colResType}" sort="ConnectionDefinitionName" rendered="#{isConnPage}" rowHeader="$boolean{true}">
            <h:outputText id="connDefName" value="#{td.value.ConnectionDefinitionName}" />
        </sun:tableColumn>

        <sun:tableColumn id="colResType" headerText="$resource{i18njms.resourcesTable.colResType}" sort="ResType" rendered="#{!isConnPage}" rowHeader="$boolean{true}">
            <h:outputText id="resType" value="#{td.value.ResType}"/>
        </sun:tableColumn>

        <sun:tableColumn id="colTypeDesc" headerText="$resource{i18njms.resourcesTable.colDesc}" sort="Description" rendered="#{!isConnPage}" rowHeader="$boolean{true}">
            <h:outputText id="typeDesc" value="#{td.value.Description}" />
        </sun:tableColumn>

    </sun:tableRowGroup>

</sun:table>

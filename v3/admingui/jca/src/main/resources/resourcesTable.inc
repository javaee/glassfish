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
<!-- jca/resourcesTable.inc -->

<sun:table id="resourcesTable" title="#{tableTitle}" 
            deselectMultipleButton="$boolean{true}"
            deselectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)"
            selectMultipleButton="$boolean{true}"
            selectMultipleButtonOnClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\');', 0)" >
         <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableId});
        />
    
#include "/jca/tableButtons.inc"

   <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
     <!afterCreate
         getClientId(component="$this{component}" clientId=>$page{tableableRowGroupId});
        />
   <sun:tableColumn selectId="select" rowHeader="$boolean{false}" id="col0">
            <sun:checkbox id="select" 
                selected="#{td.value.selected}" 
                selectedValue="$boolean{true}" 
                onClick="setTimeout('admingui.table.changeThreeTableButtons(\\\\\'#{pageSession.topActionGroup}\\\\\', \\\\\'#{pageSession.tableId}\\\\\'); admingui.table.initAllRows(\\\\\'#{pageSession.tableId}\\\\\');', 0);"
            />
    </sun:tableColumn>    

    <sun:tableColumn headerText="$resource{i18njca.resourcesTable.colJndiName}" sort="Name" rowHeader="$boolean{true}" id="col1">
        <sun:hyperlink id="link" text="#{td.value.Name}" 
            url="#{pageSession.editLink}?name=#{td.value.Name}" />
    </sun:tableColumn>

    <!--TODO enable when we enable cluster support
    <sun:tableColumn rendered="#{supportCluster}" headerText="$resource{i18n.deployTable.colStatus}" sort="enabled" rowHeader="$boolean{true}" id="col2">
        <sun:hyperlink rendered="#{supportCluster}" id="targetLink" text="#{td.value.enabled}" url="#{targetLink}?name=#{td.value.name}" />
    </sun:tableColumn>-->

    <sun:tableColumn headerText="$resource{i18n.deployTable.colEnabled}" sort="Enabled" rowHeader="$boolean{true}" id="col22">
        <sun:staticText escape="true" id="typeCol" value="#{td.value.Enabled}" />
    </sun:tableColumn>
    
    <sun:tableColumn headerText="$resource{i18njca.resourcesTable.colPool}" sort="PoolName" rendered="#{pageSession.hasPool}"  rowHeader="$boolean{true}" id="col3">
        <sun:hyperlink id="pool" text="#{td.value.PoolName}" 
            url="#{pageSession.editPoolLink}?name=#{td.value.PoolName}" />
    </sun:tableColumn>
    
    <sun:tableColumn headerText="$resource{i18njca.resourcesTable.colResType}" sort="ResType" rendered="#{pageSession.hasResType}"  rowHeader="$boolean{true}" id="coltype">
        <sun:staticText escape="true" id="typeCol" rendered="#{pageSession.hasResType}" value="#{td.value.ResType}" />
    </sun:tableColumn>

    <!-- TODO enable when we enable cluster support
    <sun:tableColumn rendered="$pageSession{showDetail}"
            headerText="$resource{i18n.common.TargetTab}"  
            rowHeader="$boolean{true}" id="colTarget">
            <event>
                <!beforeEncode
                    getTargetStatus(appName="#{td.value.name}", forApp="$boolean{false}",  result=>$page{listOfTargets});
                />				    
            </event>
            <foreach key="target" list="#{listOfTargets}">
		<staticText id="image" value="#{target.image}" />"&nbsp; 
                <sun:hyperlink url="#{target.targetURL}" text="#{target.target}" />
                "<br />
            </foreach>
        </sun:tableColumn> 
    -->
     
    
    <sun:tableColumn headerText="$resource{i18njca.resourcesTable.colDesc}" rowHeader="$boolean{true}" id="col4">
        <sun:staticText escape="true" id="typeDesc" value="#{td.value.Description}" />
    </sun:tableColumn>
    
  </sun:tableRowGroup>
  
</sun:table>

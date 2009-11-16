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
<!--
setAttribute(key="sectionTitle", value="#{listOfSection[0].sectionTitle}");
setAttribute(key="sectionDetail", value="#{listOfSection[0].sectionDetail}");
println("++++++ #{requestScope.listOfSection}")
println("sectionTitle =   $attribute{sectionTitle}");
println("sectionDetail =  $attribute{sectionDetail}");
-->

<!-- applications/subComponentTable.inc -->

<sun:table id="subcomponents"  title="$resource{i18n.ComponentTable.tableTitle}" style="padding: 10pt; width:95%" >
    <!beforeCreate
        getSubComponents(appName="#{appName}"  result="#{requestScope.listOfRows}");
    />
     <sun:tableRowGroup id="rowGroup1"  data={"$attribute{listOfRows}"} sourceVar="td">
        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colModule}" sort="moduleName" rowHeader="$boolean{true}" id="colModule">
            <staticText id="nameCol" value="#{td.value.moduleName}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.deployTable.ColEngines}"  sort="sniffers"  rowHeader="$boolean{true}" id="colsnif">
            <staticText id="sniffer" value="#{td.value.sniffers}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colName}" sort="name" rowHeader="$boolean{true}" id="col1">
            <staticText id="nameCol" value="#{td.value.name}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colType}" sort="type" rowHeader="$boolean{true}" id="col2">
            <staticText id="typeCol" value="#{td.value.type}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colAction}"  embeddedActions="#{true}" rowHeader="$boolean{true}" id="col3">
            <sun:hyperlink id="endpointLink" rendered="#{td.value.hasEndpoint}" text="$resource{i18n.ComponentTable.viewEndPoint}"
                url="#{request.contextPath}/common/applications/endpoint.jsf?appName=#{appName}&moduleName=#{td.value.moduleName}&subComponentName=#{td.value.name}&type=#{td.value.type}" />

            <sun:hyperlink id="launchLink" rendered="#{td.value.hasLaunch}" text="$resource{i18n.deployTable.launch}"
                onClick="window.open('#{td.value.launchLink}','_blank','width='+.75*screen.width+',height='+.75*screen.height+',top='+.1*screen.height+',left='+.1*screen.width+',toolbar=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes,directories=yes,location=yes'); return false;"
            />

            <sun:hyperlink id="appClientLink" rendered="#{td.value.hasAppClientLaunch}" text="$resource{i18n.deployTable.launch}"
                url="#{request.contextPath}/common/applications/appclientLaunch.jsf?appName=#{appName}&moduleName=#{td.value.moduleName}"/>
            />
        </sun:tableColumn>

    </sun:tableRowGroup>
</sun:table>
"<br /><br />

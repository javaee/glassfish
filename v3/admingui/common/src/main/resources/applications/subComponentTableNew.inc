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
        getSubComponentsNew(appName="#{appName}"  result="#{requestScope.listOfSection}"
        has2="#{requestScope.has2}"  has3="#{requestScope.has3}");
    />

<!--
   <foreach key="subComp" list="#{requestScope.listOfSection}">

  <sun:tableRowGroup id="group" data={"#{subComp['sectionDetail']}"}  sourceVar="td" headerText="#{subComp['sectionTitle']}"
        groupToggleButton="$boolean{true}" collapsed="$boolean{false}" aboveColumnHeader="$boolean{true}" emptyDataMsg="No subComponents">
        <!beforeCreate
            println("==============#{subComp['sectionTitle']}");
            println("==============#{subComp['sectionDetail']}");
        />
-->

     <sun:tableRowGroup id="group" data={"$attribute{sectionDetail0}"}  sourceVar="td" headerText="$attribute{sectionTitle0}"
        groupToggleButton="$boolean{true}" collapsed="$boolean{false}" aboveColumnHeader="$boolean{true}" emptyDataMsg="No subComponents">
        <!beforeCreate
            setAttribute(key="sectionTitle0", value="#{listOfSection[0].sectionTitle}");
            setAttribute(key="sectionDetail0", value="#{listOfSection[0].sectionDetail}");
        />

        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colName}" sort="name" rowHeader="$boolean{false}" id="col1">
            <staticText id="nameCol" value="#{td.value.name}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colType}" sort="type" rowHeader="$boolean{false}" id="col2">
            <staticText id="typeCol" value="#{td.value.type}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colAction}"  embeddedActions="#{true}" rowHeader="$boolean{false}" id="col3">
            <sun:hyperlink id="link" rendered="#{td.value.hasEndpoint}" text="$resource{i18n.ComponentTable.viewEndPoint}"
                url="#{request.contextPath}/common/applications/endpoint.jsf?appName=#{appName}&subComponentName=#{td.value.name}" />
        </sun:tableColumn>

    </sun:tableRowGroup>

    <sun:tableRowGroup id="group2" rendered="$attribute{has2}" data={"$attribute{sectionDetail2}"}  sourceVar="td" headerText="$attribute{sectionTitle2}"
        groupToggleButton="$boolean{true}" collapsed="$boolean{false}" aboveColumnHeader="$boolean{true}" emptyDataMsg="No subComponents">
        <!beforeCreate
            setAttribute(key="sectionTitle2", value="#{listOfSection[1].sectionTitle}");
            setAttribute(key="sectionDetail2", value="#{listOfSection[1].sectionDetail}");
        />
        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colName}" sort="name" rowHeader="$boolean{false}" id="col1">
            <staticText id="nameCol" value="#{td.value.name}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colType}" sort="type" rowHeader="$boolean{false}" id="col2">
            <staticText id="typeCol" value="#{td.value.type}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colAction}"  embeddedActions="#{true}" rowHeader="$boolean{false}" id="col3">
            <sun:hyperlink id="link" rendered="#{td.value.hasEndpoint}" text="$resource{i18n.ComponentTable.viewEndPoint}"
                url="#{request.contextPath}/common/applications/endpoint.jsf?appName=#{appName}&subComponentName=#{td.value.name}" />
        </sun:tableColumn>
    </sun:tableRowGroup>

    <sun:tableRowGroup id="group3" rendered="$attribute{has3}" data={"$attribute{sectionDetail3}"}  sourceVar="td" headerText="$attribute{sectionTitle3}"
        groupToggleButton="$boolean{true}" collapsed="$boolean{false}" aboveColumnHeader="$boolean{true}" emptyDataMsg="No subComponents">
        <!beforeCreate
            setAttribute(key="sectionTitle3", value="#{listOfSection[2].sectionTitle}");
            setAttribute(key="sectionDetail3", value="#{listOfSection[2].sectionDetail}");
        />
        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colName}" sort="name" rowHeader="$boolean{false}" id="col1">
            <staticText id="nameCol" value="#{td.value.name}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colType}" sort="type" rowHeader="$boolean{false}" id="col2">
            <staticText id="typeCol" value="#{td.value.type}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.ComponentTable.colAction}"  embeddedActions="#{true}" rowHeader="$boolean{false}" id="col3">
            <sun:hyperlink id="link" rendered="#{td.value.hasEndpoint}" text="$resource{i18n.ComponentTable.viewEndPoint}"
                url="#{request.contextPath}/common/applications/endpoint.jsf?appName=#{appName}&subComponentName=#{td.value.name}" />
        </sun:tableColumn>
    </sun:tableRowGroup>
  //</foreach>

</sun:table>
"<br /><br />

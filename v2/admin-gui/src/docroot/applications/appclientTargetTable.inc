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

<!-- applications/appclientTargetTable.inc -->

<sun:table id="deployTable" title="$resource{i18n.common.TargetTableTitle}" sortPanelToggleButton="#{false}" >
     <!beforeCreate
        getApplicationTargetTableList(appName="#{name}" appType="#{appType}" result=>$attribute{listOfRows});
    />

    <!facet actionsTop>
        <sun:button id="manageTargetButton" text="$resource{i18n.button.ManageTarget}" enabled="#{false}" >
            <!command
                    setAttribute(key="name" value="#{name}"); 
                    setAttribute(key="targetPage" value="#{targetPage}");
                    navigate(page="/applications/manageTargets.jsf");
            />
        </sun:button>
     </facet>

    <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}"  data={"$attribute{listOfRows}"} sourceVar="td">
        <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{targetTableRowGroupId});
        />

        <sun:tableColumn headerText="$resource{i18n.targetTable.colTargetName}" rowHeader="$boolean{true}" id="col1">
            <staticText id="nameCol" value="#{td.value.targetName}" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.TargetTable.colVirtualServer}" rendered="#{hasActionColumn}" rowHeader="$boolean{true}" id="col3">
            <sun:hyperlink id="link" text="$resource{i18n.TargetTable.virtualServerLink}" 
                url="/applications/virtualServerTargets.jsf?appName=#{name}&appType=#{appType}&targetName=#{td.value.targetName}&targetPage=#{targetPage}" />
        </sun:tableColumn>

    </sun:tableRowGroup>
 </sun:table>
    
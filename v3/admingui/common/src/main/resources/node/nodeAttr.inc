<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 
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
    <sun:title id="propertyContentPage" title="#{pageSession.title}" helpText="#{pageSession.titleHelp}">
    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="newButton" text="$resource{i18n.button.Save} (TBD)"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    //gf.restExecuteCommand("create-node-ssh");
                    gf.redirect(page="#{pageSession.listLink}");
                />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}"  text="$resource{i18n.button.Cancel}" >
                <!command
                    gf.redirect(page="#{pageSession.listLink}");
                />
            </sun:button>
        </sun:panelGroup>
     </facet>
    </sun:title>
        "<br>
    <sun:propertySheet id="propertySheet">
        <sun:propertySheetSection id="propertSectionTextField">
            <sun:property id="staticnameProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" >
                <sun:staticText id="IdStatic" text="#{pageSession.valueMap['Name']}"/>
            </sun:property>
            <sun:property id="nameProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" helpText="$resource{i18nc.node.NameHelp}">
                <sun:textField id="name" text="#{pageSession.valueMap['Name']}" columns="$int{60}" styleClass="required" required="#{true}" />
            </sun:property>
            <sun:property id="NodeHost"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.node.NodeHost}" helpText="$resource{i18nc.node.NodeHostHelp}">
                <sun:textField id="NodeHost" columns="$int{60}" text="#{pageSession.valueMap['NodeHost']}" />
            </sun:property>
            <sun:property id="NodeHome"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.node.NodeHome}" helpText="$resource{i18nc.node.NodeHomeHelp}">
                <sun:textField id="NodeHome" columns="$int{60}" text="#{pageSession.valueMap['NodeHome']}" />
            </sun:property>
        </sun:propertySheetSection>

        <sun:propertySheetSection id="sshConnector" label="$resource{i18nc.node.sshConnectorSection}" >
            <sun:property id="sshNodeHost"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.node.sshHost}" helpText="$resource{i18nc.node.sshHostHelp}">
                <sun:textField id="NodeHost" columns="$int{60}" text="#{pageSession.valueMap['sshHost']}" />
            </sun:property>
            <sun:property id="sshNodeHome"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.node.sshPort}" helpText="$resource{i18nc.node.sshPortHelp}">
                <sun:textField id="NodeHome" columns="$int{60}" text="#{pageSession.valueMap['sshPort']}" />
            </sun:property>
        </sun:propertySheetSection>

        <sun:propertySheetSection id="sshAuth" label="$resource{i18nc.node.sshAuthSection}" >
            <sun:property id="UserName"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.node.UserName}" helpText="$resource{i18nc.node.UserNameHelp}">
                <sun:textField id="UserName" columns="$int{60}" text="#{pageSession.valueMap['UserName']}" />
            </sun:property>
            <sun:property id="Keyfile"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.node.Keyfile}" helpText="$resource{i18nc.node.KeyfileHelp}">
                <sun:textField id="Keyfile" columns="$int{60}" text="#{pageSession.valueMap['Keyfile']}" />
            </sun:property>
        </sun:propertySheetSection>
    </sun:propertySheet>

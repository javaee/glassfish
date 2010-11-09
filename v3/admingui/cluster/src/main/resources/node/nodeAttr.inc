<!--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.

    The contents of this file are subject to the terms of either the GNU
    General Public License Version 2 only ("GPL") or the Common Development
    and Distribution License("CDDL") (collectively, the "License").  You
    may not use this file except in compliance with the License.  You can
    obtain a copy of the License at
    https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
    or packager/legal/LICENSE.txt.  See the License for the specific
    language governing permissions and limitations under the License.

    When distributing the software, include this License Header Notice in each
    file and include the License file at packager/legal/LICENSE.txt.

    GPL Classpath Exception:
    Oracle designates this particular file as subject to the "Classpath"
    exception as provided by Oracle in the GPL Version 2 section of the License
    file that accompanied this code.

    Modifications:
    If applicable, add the following below the License Header, with the fields
    enclosed by brackets [] replaced by your own identifying information:
    "Portions Copyright [year] [name of copyright owner]"

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

#include "/cluster/shared/handlers.inc"

<handler id="gfr.prepareNodeCmd" >
    if (!#{pageSession.valueMap['force']}){
        mapPut(map="#{pageSession.valueMap}"  key="force" value="false");
    }
    createMap(result="#{pageSession.attrsMap}");
    foreach(var="attr" list="#{requestScope.attrsList}"){
        mapGet(Map="#{pageSession.valueMap}" Key="#{requestScope.attr}" Value="#{requestScope.val}");
        mapPut(map="#{pageSession.attrsMap}", key="#{requestScope.attr}" value="#{requestScope.val}");
    }
</handler>

    <sun:title id="propertyContentPage" title="#{pageSession.title}" helpText="#{pageSession.titleHelp}">
<!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save} "
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'))
                    {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    setAttribute(key="includeInstallDir" value="false");
                    if ("#{pageSession.valueMap['type']}=CONFIG"){
                        setAttribute(key="attrsList" value={"nodehost", "nodedir"});
                        setAttribute(key="endpoint" value="nodes/node/#{pageSession.nodeName}/update-node-config");
                    }
                    if ("#{pageSession.valueMap['type']}=SSH"){
                        setAttribute(key="attrsList" value={"nodehost", "nodedir", "sshport", "sshuser", "sshkeyfile", "force"});
                        setAttribute(key="endpoint" value="nodes/node/#{pageSession.nodeName}/update-node-ssh");
                    }
                    gfr.prepareNodeCmd();
                    gf.restRequest(endpoint="#{sessionScope.REST_URL}/#{requestScope.endpoint}" method="POST" attrs="#{pageSession.attrsMap}")
                    prepareSuccessfulMsg();
                    gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>

            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
            <!command
                setAttribute(key="nameToCheck" value="#{pageSession.valueMap['id']}");
                gfj.checkNameExist();
                setAttribute(key="includeInstallDir" value="true");
                if ("#{pageSession.valueMap['type']}=CONFIG"){
                    setAttribute(key="attrsList" value={"id", "nodehost", "nodedir", "installdir"});
                    setAttribute(key="endpoint" value="nodes/create-node-config");
                }
                if ("#{pageSession.valueMap['type']}=SSH"){
                    setAttribute(key="attrsList" value={"id", "nodehost", "nodedir", "installdir", "sshport", "sshuser", "sshkeyfile", "force"});
                    setAttribute(key="endpoint" value="nodes/create-node");
                }
                gfr.prepareNodeCmd();
                gf.restRequest( endpoint="#{sessionScope.REST_URL}/#{requestScope.endpoint}" attrs="#{pageSession.attrsMap}" method="POST" )
                gf.redirect(page="#{request.contextPath}/cluster/node/nodes.jsf?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
             />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}" >
                <!command
                    gf.redirect(page="#{request.contextPath}/cluster/node/nodes.jsf");
                />
            </sun:button>

        </sun:panelGroup>
        </facet>

    </sun:title>
     <event>
        <!beforeEncode
            setPageSessionAttribute(key="showSSH" value="#{false}");
            if ("#{pageSession.valueMap['type']}=SSH"){
                   setPageSessionAttribute(key="showSSH" value="#{true}")
            }
        />
    </event>
    <sun:propertySheet id="propertySheet">
        
        <sun:propertySheetSection id="propertSectionTextField">
            <sun:property id="nameProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" helpText="$resource{i18ncs.node.NameHelp}">
                <sun:textField id="name" text="#{pageSession.valueMap['id']}" columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.node.Name']}" styleClass="required" required="#{true}" />
            </sun:property>
            
            <sun:property id="staticnameProp"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" >
                <sun:staticText id="IdStatic" text="#{pageSession.nodeName}"/>
            </sun:property>
            <sun:property id="typeProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncs.node.type}" helpText="$resource{i18ncs.node.typeHelp}">
                <sun:dropDown id="type" labels={"CONFIG" "SSH"}  selected="#{pageSession.valueMap['type']}"
                              onChange="javascript:showSshSection(this.value, '#{pageSession.sshConnectorSectionId}');">
                </sun:dropDown>
            </sun:property>

            <sun:property id="NodeHost"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncs.node.NodeHost}" helpText="$resource{i18ncs.node.NodeHostHelp}">
                <sun:textField id="NodeHost" columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.node.NodeHost']}" text="#{pageSession.valueMap['nodehost']}" styleClass="required" required="#{true}"/>
            </sun:property>

            <sun:property id="NodeHome"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncs.node.NodeDir}" helpText="$resource{i18ncs.node.NodeDirHelp}">
                <sun:textField id="NodeHome" columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.node.NodeDir']}" text="#{pageSession.valueMap['nodedir']}" />
            </sun:property>

            <sun:property id="installdir"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncs.node.InstallDir}" >
                <sun:staticText id="IdStatic" text="#{pageSession.valueMap['installdir']}"/>
            </sun:property>

            <sun:property id="installdir2"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncs.node.InstallDir}" helpText="$resource{i18ncs.node.InstallDirHelp}">
                <sun:textField id="installDir" columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.node.InstallDir']}" text="#{pageSession.valueMap['installdir']}" />
            </sun:property>

            <sun:property>
                <sun:staticText text=" "/>
            </sun:property>
            
        </sun:propertySheetSection>

        <sun:propertySheetSection id="sshConnectorSection" visible="#{pageSession.showSSH}" label="$resource{i18ncs.node.sshConnectorSection}" >
            <!afterCreate
                getClientId(component="$this{component}" clientId="#{pageSession.sshConnectorSectionId}");
            />
            <sun:property id="force"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncs.node.force}" helpText="$resource{i18ncs.node.forceHelp}">
                <sun:checkbox id="force" label="$resource{i18n.common.Enabled}" selected="#{pageSession.valueMap['force']}" selectedValue="true" />
            </sun:property>
            
            <sun:property id="sshport"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncs.node.sshPort}" helpText="$resource{i18ncs.node.sshPortHelp}">
                <sun:textField id="sshport" columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.node.sshPort']}" text="#{pageSession.valueMap['sshport']}" />
            </sun:property>
            <sun:property id="UserName"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncs.node.UserName}" helpText="$resource{i18ncs.node.UserNameHelp}">
                <sun:textField id="UserName" columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.node.UserName']}" text="#{pageSession.valueMap['sshuser']}" />
            </sun:property>
            <sun:property id="Keyfile"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18ncs.node.Keyfile}" helpText="$resource{i18ncs.node.KeyfileHelp}">
                <sun:textField id="Keyfile" columns="$int{60}" maxLength="#{sessionScope.fieldLengths['maxLength.node.Keyfile']}" text="#{pageSession.valueMap['sshkeyfile']}" />
            </sun:property>
        </sun:propertySheetSection>
    </sun:propertySheet>
         "<br /><br /><br />

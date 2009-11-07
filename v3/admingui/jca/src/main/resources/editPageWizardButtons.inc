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

<!-- editPageWizzardButtons.inc -->

    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="previousButton" immediate="#{false}" primary="#{false}" text="$resource{i18n.button.Previous}" >
                <!command
                    getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                    getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
                    setSessionAttribute(key="wizardPoolProperties", value="$attribute{newList}");
                    setAttribute(key="fromStep2" value="#{true}")
                    gf.navigate(page="#{request.contextPath}/jca/connectorConnectionPoolNew1.jsf")
                 />
            </sun:button>
            <sun:button id="finishButton" rendered="#{!edit}" text="$resource{i18n.button.Finish}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    updateConnectorConnectionPoolWizardStep2();
                    createProxy(parentObjectNameStr="#{pageSession.parentObjectNameStr}" childType="#{pageSession.childType}"
                            attrs="#{sessionScope.wizardMap}"
                            convertToFalse="#{pageSession.convertToFalseList}"
                            result => $attribute{objStr});
                    if (#{pageSession.hasPropertyTable}){
                        getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                        getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
                        setProxyProperties(objectNameStr="$attribute{objStr}", propertyList="$attribute{newList}", systemProp="#{pageSession.systemProp}" );
                    }
                    if ("#{sessionScope.wizardMap['Ping']}") {
                        pingJdbcConnectionPool(jndiName="#{sessionScope.wizardMap['Name']}" , wmsg="$resource{i18njca.msg.warning.poolCreatedPingFailed}");
                        setSessionAttribute(key="wizardMap" value="#{null}")
                        setSessionAttribute(key="wizardPoolExtra" value="#{null}")
                        setSessionAttribute(key="wizardPoolProperties" value="#{null}")
                        gf.redirect(page="#{parentPage}?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                    }
                    setSessionAttribute(key="wizardMap" value="#{null}")
                    setSessionAttribute(key="wizardPoolExtra" value="#{null}")
                    setSessionAttribute(key="wizardPoolProperties" value="#{null}")
                    gf.redirect(page="#{parentPage}")
                />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" rendered="#{pageSession.showCancelButton}" text="$resource{i18n.button.Cancel}" >
                <!command
                    setSessionAttribute(key="wizardMap" value="#{null}")
                    setSessionAttribute(key="wizardPoolExtra" value="#{null}")
                    setSessionAttribute(key="wizardPoolProperties" value="#{null}")
                    gf.redirect(page="#{parentPage}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>

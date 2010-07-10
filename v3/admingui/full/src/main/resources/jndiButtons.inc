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


<!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                updateJndiResourceAttrs(attrMap="#{attrMap}", entries="#{pageSession.valueMap['ResType']}", classnameOption="#{classnameOption}", resType="#{requestScope.resType}" );
                mapPut(map="#{pageSession.valueMap}" key="ResType" value="#{requestScope.resType}")

                if(#{pageSession.hasPropertySheet}){
                    gf.updateEntity(endpoint="#{pageSession.selfUrl}"  attrs="#{pageSession.valueMap}"
                        skipAttrs="#{pageSession.skipAttrsList}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                    );
                    if(#{pageSession.has2Objects}){
                        gf.updateEntity(endpoint="#{pageSession.selfUrl2}"  attrs="#{pageSession.valueMap2}"
                            skipAttrs="#{pageSession.skipAttrsList2}"
                            convertToFalse="#{pageSession.convertToFalseList2}"
                        );
                    }
                }
                if (#{pageSession.hasPropertyTable}){
                    getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                    getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
                    //REST - TO DO
                    setProxyProperties(objectNameStr="#{pageSession.objectNameStr}", propertyList="$attribute{newList}", systemProp="#{pageSession.systemProp}" );
                }
                prepareSuccessfulMsg();
                //gf.redirect(page="#{selfPage}?name=#{pageSession.encodedName}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>
            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    updateJndiResourceAttrs(attrMap="#{attrMap}", entries="#{pageSession.valueMap['ResType']}", classnameOption="#{classnameOption}", resType="#{requestScope.resType}" );
                    mapPut(map="#{pageSession.valueMap}" key="ResType" value="#{requestScope.resType}")
                    //TODO target
                    //The selected Targets will be available as #{pageSession.selectedTargets},  which is an array.
                    //We need to ensure source-ref of the selected targets are created accordingly.
                    println("selected target = #{pageSession.selectedTargets}");

                    gf.createEntity(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}"
                        attrs="#{pageSession.valueMap}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                        result => $attribute{resultEndPoint});
                    if (#{pageSession.hasPropertyTable}){
                        getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
                        getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
                        //REST - TO DO
                        if (#{pageSession['rest-api']}) {
                            setAttribute(key="objStr", value="#{pageSession.restObjStr},name=#{pageSession.valueMap['Name']}");
                        }
                        setProxyProperties(objectNameStr="$attribute{objStr}", propertyList="$attribute{newList}", systemProp="#{pageSession.systemProp}" );
                    }
                    gf.redirect(page="#{parentPage}")
                />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" rendered="#{pageSession.showCancelButton}" text="$resource{i18n.button.Cancel}" >
                <!command
                    gf.redirect(page="#{parentPage}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>

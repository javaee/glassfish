<!--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.

    The contents of this file are subject to the terms of either the GNU
    General Public License Version 2 only ("GPL") or the Common Development
    and Distribution License("CDDL") (collectively, the "License").  You
    may not use this file except in compliance with the License.  You can
    obtain a copy of the License at
    https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
    or packager/legal/LICENSE.txt.  See the License for the specific
    language governing permissions and limitations under the License.

    When distributing the software, include this License Header Notice in each
    file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.

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

<!-- httpSaveButtons.inc -->

    <!-- To use this general include files for page buttons,  the following page Session attributes should be set:
        setPageSessionAttribute(key="edit" value="#{false}" );
        setPageSessionAttribute(key="showDefaultButton" value="#{false}" );  //true is default
        setPageSessionAttribute(key="showCancelButton" value="#{false}" );   // true is default
        setPageSessionAttribute(key="hasPropertySheet" value="#{true}" );    //false is default
        setPageSessionAttribute(key="hasPropertyTable" value="#{true}" );   //false is default
        
        setPageSessionAttribute(key="has2Objects" value="#{true}" );        //false is default
        For creating new objects, if #{pageSession.parentObjectNameStr2} is specified, it will be used as the parent for creating the 2nd config.
        otherwise, it means the 2nd config is the sub-element, and the newly created object will be used as the parent to create
        the 2nd object.

    -->
#include "/common/shared/editPageLoadDefaultButton.inc"
    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                    onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command

                    gf.createEntity(endpoint="#{pageSession.selfUrl}"
                                    attrs="#{pageSession.valueMap}"
                                    skipAttrs="#{pageSession.skipAttrsList}"
                                    convertToFalse="#{pageSession.convertToFalseList}"
                                    onlyUseAttrs="#{pageSession.onlyUseAttrs}"
                    );
                    setPageSessionAttribute(key="protocolHttpUrl", value="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/network-config/protocols/protocol/#{pageSession.Name}/http");
                    setPageSessionAttribute(key="networkListenerUrl", value="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/network-config/network-listeners/network-listener");
                    setPageSessionAttribute(key="virtualServerUrl", value="#{sessionScope.REST_URL}/configs/config/#{pageSession.configName}/http-service/virtual-server");
                    gf.getChildrenNamesList(endpoint="#{pageSession.networkListenerUrl}",
                                            id="name",
                                            result="#{pageSession.networkListeners}");
                    gf.restRequest(endpoint="#{pageSession.parentUrl}.json",
                                   method="get",
                                   result="#{pageSession.protocolAttr}");
                    //gf.checkIfEndPointExist(endpoint="#{pageSession.protocolHttpUrl}",
                    //exists="#{pageSession.protocolHttpExist}");
                    if (#{pageSession.httpEndPointExist}) {
                        gf.restRequest(endpoint="#{pageSession.protocolHttpUrl}.json",
                                       method="get",
                                       result="#{pageSession.protocolAttrHttp}");
                        if ("!(#{pageSession.protocolAttrHttp.data.entity['defaultVirtualServer']}=#{pageSession.previousVSName})") {
                            foreach (var="listener" list="#{pageSession.networkListeners}") {
                                gf.restRequest(endpoint="#{pageSession.networkListenerUrl}/#{listener}.json",
                                               method="get",
                                               result="#{pageSession.networkListAttrs}");
                                if ("#{pageSession.networkListAttrs.data.entity['protocol']}=#{pageSession.protocolAttr.data.entity['name']}") {
                                    gf.restRequest(endpoint="#{pageSession.virtualServerUrl}/#{pageSession.previousVSName}.json",
                                                   method="get",
                                                   result="#{pageSession.vsAttrs}");
                                    setPageSessionAttribute(key="additionalSaveHandlerArgs" 
                                                            value="vsAttrs:%23{pageSession.vsAttrs.data.entity},
                                                                   listenerName:%23{pageSession.networkListAttrs.data.entity['name']},
                                                                   addFlag:{%23false},
                                                                   result:%23{pageSession.vsAttrModified}");
                                    if (#{pageSession.additionalSaveHandler}){
                                        gf.invokeHandler(handler="#{pageSession.additionalSaveHandler}", args="#{pageSession.additionalSaveHandlerArgs}" );
                                    }
                                    gf.restRequest(endpoint="#{pageSession.virtualServerUrl}/#{pageSession.previousVSName}",
                                                   attrs="#{pageSession.vsAttrModified}",
                                                   method="post");
                                    // Add the Network Listener in the current VS
                                    gf.restRequest(endpoint="#{pageSession.virtualServerUrl}/#{pageSession.protocolAttrHttp.data.entity['defaultVirtualServer']}.json",
                                                   method="get",
                                                   result="#{pageSession.vsAttrs}");
                                    setPageSessionAttribute(key="additionalSaveHandlerArgs"
                                                            value="vsAttrs:%23{pageSession.vsAttrs.data.entity},
                                                            listenerName:%23{pageSession.networkListAttrs.data.entity['name']},
                                                            addFlag:{%23false},
                                                            result:%23{pageSession.vsAttrModified}");
                                    if (#{pageSession.additionalSaveHandler}){
                                        gf.invokeHandler(handler="#{pageSession.additionalSaveHandler}", args="#{pageSession.additionalSaveHandlerArgs}" );
                                    }
                                    gf.restRequest(endpoint="#{pageSession.virtualServerUrl}/#{pageSession.protocolAttrHttp.data.entity['defaultVirtualServer']}",
                                                   attrs="#{pageSession.vsAttrModified}",
                                                   method="post");
                                }
                            }
                        }
                    }
                    prepareSuccessfulMsg();
                    gf.redirect(page="#{pageSession.selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />

                prepareSuccessfulMsg();
                gf.redirect(page="#{pageSession.selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>

            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" rendered="#{pageSession.showCancelButton}" text="$resource{i18n.button.Cancel}" >
                <!command
                    gf.redirect(page="#{parentPage}?configName=#{configName}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>

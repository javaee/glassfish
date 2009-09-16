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
                    onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " >
                <!command
               if(#{pageSession.hasRadioButtons}){
                    forLoop(end="#{pageSession.radioButtonCount}" varName="i"){
                        setValueExpression(expression="#{pageSession.valueMap[requestScope.attrNames[i]]}"   value="#{param[requestScope.RadioButtonNames[i]]}");
                    }

                }
                if(#{pageSession.hasPropertySheet}){
                    saveBeanAttributes(objectNameStr="#{pageSession.objectNameStr}"  attrs="#{pageSession.valueMap}"
                        skipAttrs="#{pageSession.skipAttrsList}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                        onlyUseAttrs="#{pageSession.onlyUseAttrs}"
                    );
                    if(#{usersOptionG}){
                        if(#{pexist}) {
                            deleteChild(objectNameStr="#{pageSession.objectNameStr}", type="principal-map", name="#{null}");
                        }
                        if(#{gexist}) {
                            saveBeanAttributes(objectNameStr="#{pageSession.objectNameStr2}" attrs="#{pageSession.valueMap2}"
                                skipAttrs="#{pageSession.skipAttrsList2}"
                                convertToFalse="#{pageSession.convertToFalseList2}"
                            );
                        }
                        if(!#{gexist}) {
                            createProxy(parentObjectNameStr="#{pageSession.parentObjectNameStr2}" childType="group-map"
                                attrs="#{pageSession.valueMap2}"
                                convertToFalse="#{pageSession.convertToFalseList2}"
                                result => $attribute{objStr2});
                        }

                    }
                    if(#{usersOptionP}){
                        if(#{gexist}) {
                            deleteChild(objectNameStr="#{pageSession.objectNameStr}", type="group-map", name="#{null}");
                        }
                        if(#{pexist}) {
                            saveBeanAttributes(objectNameStr="#{pageSession.objectNameStr3}" attrs="#{pageSession.valueMap3}"
                            skipAttrs="#{pageSession.skipAttrsList2}"
                            convertToFalse="#{pageSession.convertToFalseList2}"
                            );
                        }
                        if(!#{pexist}) {
                            createProxy(parentObjectNameStr="#{pageSession.parentObjectNameStr2}" childType="principal-map"
                                attrs="#{pageSession.valueMap3}"
                                convertToFalse="#{pageSession.convertToFalseList3}"
                                result => $attribute{objStr3});
                        }
                        
                    }
                }
                prepareSuccessfulMsg();
                redirect(page="#{selfPage}?name=#{pageSession.Name}&mapName=#{pageSession.mapName}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}&name=#{pageSession.Name}&configName=#{pageSession.configName}");
                />
            </sun:button>
            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
                    onClick="return guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}'); " >
                <!command
                 createProxy(parentObjectNameStr="#{pageSession.parentObjectNameStr}" childType="#{pageSession.childType}"
                        attrs="#{pageSession.valueMap}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                        result => $attribute{objStr});
                    setPageSessionAttribute(key="parentObjectNameStr2", value="$attribute{objStr}");
                    if(#{pageSession.has2Objects}){
                        if(#{usersOptionG}){
                            createProxy(parentObjectNameStr="#{pageSession.parentObjectNameStr2}" childType="#{pageSession.childType2}"
                                attrs="#{pageSession.valueMap2}"
                                convertToFalse="#{pageSession.convertToFalseList2}"
                                result => $attribute{objStr2});
                        }
                        if(#{usersOptionP}){
                            createProxy(parentObjectNameStr="#{pageSession.parentObjectNameStr2}" childType="#{pageSession.childType3}"
                                attrs="#{pageSession.valueMap3}"
                                convertToFalse="#{pageSession.convertToFalseList2}"
                                result => $attribute{objStr2});
                        }
                        if (!#{pageSession.parentObjectNameStr2}){
                            //create as a sub-element
                            createProxy(parentObjectNameStr="$attribute{objStr}" childType="#{pageSession.childType2}"
                                attrs="#{pageSession.valueMap2}"
                                convertToFalse="#{pageSession.convertToFalseList2}"
                                result => $attribute{objStr2});
                        }
                    }
                    redirect(page="#{pageSession.parentPage}?name=#{pageSession.Name}")
                />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" rendered="#{pageSession.showCancelButton}" text="$resource{i18n.button.Cancel}" >
                <!command
                    redirect(page="#{parentPage}?name=#{pageSession.Name}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>
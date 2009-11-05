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
                convertSecurityMapPropsToStringArray(edit="true", userGroups="#{pageSession.valueMap['UserGroup']}", principals="#{pageSession.valueMap['Principal']}", usersOptions="#{usersOptionU}", principalsSA =>$attribute{principalsSA}  usersSA =>$attribute{usersSA});
                mapPut(map="#{pageSession.valueMap}" key="UserGroup" value="#{usersSA}")
                mapPut(map="#{pageSession.valueMap}" key="Principal" value="#{principalsSA}")
                if(#{pageSession.hasPropertySheet}){
                    saveBeanAttributes(objectNameStr="#{pageSession.objectNameStr}"  attrs="#{pageSession.valueMap}"
                        skipAttrs="#{pageSession.skipAttrsList}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                        onlyUseAttrs="#{pageSession.onlyUseAttrs}"
                    );
                    if(#{pageSession.has2Objects}){
                        saveBeanAttributes(objectNameStr="#{pageSession.objectNameStr2}"  attrs="#{pageSession.valueMap2}"
                            skipAttrs="#{pageSession.skipAttrsList2}"
                            convertToFalse="#{pageSession.convertToFalseList2}"
                        );
                    }
                }
                prepareSuccessfulMsg();
                gf.redirect(page="#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>
            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                convertSecurityMapPropsToStringArray(edit="false", userGroups="#{pageSession.valueMap['UserGroup']}", principals="#{pageSession.valueMap['Principal']}", usersOptions="#{usersOptionU}",  principalsSA =>$attribute{principalsSA} usersSA =>$attribute{usersSA});
                if($attribute{usersSA}){
                   mapPut(map="#{pageSession.valueMap}" key="UserGroup" value="#{usersSA}")
                }
                if($attribute{principalsSA}) {
                    mapPut(map="#{pageSession.valueMap}" key="Principal" value="#{principalsSA}")
                }
                   createProxy(parentObjectNameStr="#{pageSession.parentObjectNameStr}" childType="#{pageSession.childType}"
                        attrs="#{pageSession.valueMap}"
                        convertToFalse="#{pageSession.convertToFalseList}"
                        result => $attribute{objStr});
                    setPageSessionAttribute(key="parentObjectNameStr2", value="$attribute{objStr}");
                    if(#{pageSession.has2Objects}){
                        if (#{pageSession.parentObjectNameStr2}){
                            createProxy(parentObjectNameStr="#{pageSession.parentObjectNameStr2}" childType="#{pageSession.childType2}"
                                attrs="#{pageSession.valueMap2}"
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
                    gf.redirect(page="#{pageSession.parentPage}")
                />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" rendered="#{pageSession.showCancelButton}" text="$resource{i18n.button.Cancel}" >
                <!command
                    gf.redirect(page="#{parentPage}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>
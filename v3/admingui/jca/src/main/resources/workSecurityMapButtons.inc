<!--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.

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

   <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save}"
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    gf.createEntity(endpoint="#{pageSession.selfUrl}"
                        attrs="#{pageSession.valueMap}"
                        convertToFalse="#{pageSession.convertToFalseList}");
                    if ('#{usersOptionG}=users') {
                        convertStringToMap(str="#{pageSession.valueMap2['groupMapping']}" result="#{requestScope.groupsMap}");
                        gf.getMapKeys(Map="#{requestScope.groupsMap}" Keys="#{requestScope.groupsList}");
                        gf.getChildList(parentEndpoint="#{pageSession.selfUrl}", childType="group-map", result="#{requestScope.oldGroupsInfo}", id="eisGroup");
                        gf.getMessageProps(messageListProps="#{requestScope.oldGroupsInfo}"
                           id="eisGroup"
                           keyList="#{pageSession.oldGroupsList}");
                        foreach(var="group" list="#{requestScope.groupsList}") {
                            //verify whether this group already existing or not.
                            //If it is already there, update the entity or create a new one.
                            gf.containedIn(list="#{pageSession.oldGroupsList}" testStr="#{group}" contain="#{requestScope.isExists}");
                            if (#{requestScope.isExists}) {
                                createMap(result="#{requestScope.attrsMap}");
                                mapGet(Map="#{requestScope.groupsMap}" Key="#{group}" Value="#{requestScope.val}");
                                mapPut(map="#{requestScope.attrsMap}" key="mappedGroup" value="#{val}");
                                gf.restRequest(endpoint="#{pageSession.selfUrl}/group-map/#{group}" attrs="#{requestScope.attrsMap}" method="POST");
                                removeListElement(list="#{pageSession.oldGroupsList}" name="#{group}" finalList="#{pageSession.oldGroupsList}");
                            }
                            //TO DO: As of now not able to add a new group element.
                            //Issue:13174. Remove the comments once it is available.
                            <!--if (!#{requestScope.isExists}) {
                                createMap(result="#{requestScope.attrsMap}");
                                mapPut(map="#{requestScope.attrsMap}" key="eisGroup" value="#{group}");
                                mapGet(Map="#{requestScope.groupsMap}" Key="#{group}" Value="#{requestScope.val}");
                                mapPut(map="#{requestScope.attrsMap}" key="mappedGroup" value="#{val}");
                                gf.restRequest(endpoint="#{pageSession.selfUrl}/group-map" attrs="#{requestScope.attrsMap}" method="POST");
                            }-->
                            //Remove the principal maps if there are any.
                            gf.getChildList(parentEndpoint="#{pageSession.selfUrl}", childType="principal-map", result="#{requestScope.oldPrincipalsInfo}", id="eisPrincipal");
                            gf.getMessageProps(messageListProps="#{requestScope.oldPrincipalsInfo}"
                               id="eisPrincipal"
                               keyList="#{requestScope.oldPrincipalsList}");
                            foreach(var="principal" list="#{requestScope.principalsList}") {
                                gf.restRequest(endpoint="#{pageSession.selfUrl}/principal-map/#{principal}" method="DELETE");
                            }
                        }
                        //Remove the remaining group old group elements if there are any.
                        foreach(var="group" list="#{pageSession.oldGroupsList}") {
                            gf.restRequest(endpoint="#{pageSession.selfUrl}/group-map/#{group}" method="DELETE");
                        }
                    }
                    if ('#{usersOptionP}=principals') {
                        convertStringToMap(str="#{pageSession.valueMap2['principalMapping']}" result="#{requestScope.principalsMap}");
                        gf.getMapKeys(Map="#{requestScope.principalsMap}" Keys="#{requestScope.principalsList}");
                        gf.getChildList(parentEndpoint="#{pageSession.selfUrl}", childType="principal-map", result="#{requestScope.oldPrincipalsInfo}", id="eisPrincipal");
                        gf.getMessageProps(messageListProps="#{requestScope.oldPrincipalsInfo}"
                           id="eisPrincipal"
                           keyList="#{pageSession.oldPrincipalsList}");
                        foreach(var="principal" list="#{requestScope.principalsList}") {
                            //verify whether this principal already existing or not.
                            //If it is already there, update the entity or create a new one.
                            gf.containedIn(list="#{pageSession.oldPrincipalsList}" testStr="#{principal}" contain="#{requestScope.isExists}");
                            if (#{requestScope.isExists}) {
                                createMap(result="#{requestScope.attrsMap}");
                                mapGet(Map="#{requestScope.principalsMap}" Key="#{principal}" Value="#{requestScope.val}");
                                mapPut(map="#{requestScope.attrsMap}" key="mappedPrincipal" value="#{val}");
                                gf.restRequest(endpoint="#{pageSession.selfUrl}/principal-map/#{principal}" attrs="#{requestScope.attrsMap}" method="POST");
                                removeListElement(list="#{pageSession.oldPrincipalsList}" name="#{principal}" finalList="#{pageSession.oldPrincipalsList}");
                            }
                            //TO DO: As of now not able to add a new prinicial element.
                            //Issue:13174. Remove the comments once it is available.
                            <!--if (!#{requestScope.isExists}) {
                                createMap(result="#{requestScope.attrsMap}");
                                mapPut(map="#{requestScope.attrsMap}" key="eisPrincipal" value="#{principal}");
                                mapGet(Map="#{requestScope.principalsMap}" Key="#{principal}" Value="#{requestScope.val}");
                                mapPut(map="#{requestScope.attrsMap}" key="mappedPrincipal" value="#{val}");
                                gf.restRequest(endpoint="#{pageSession.selfUrl}/principal-map" attrs="#{requestScope.attrsMap}" method="POST");
                            }-->
                            //Remove the group maps if there are any.
                            gf.getChildList(parentEndpoint="#{pageSession.selfUrl}", childType="group-map", result="#{requestScope.oldGroupsInfo}", id="eisGroup");
                            gf.getMessageProps(messageListProps="#{requestScope.oldGroupsInfo}"
                               id="eisGroup"
                               keyList="#{requestScope.oldGroupsList}");
                            foreach(var="group" list="#{requestScope.groupsList}") {
                                gf.restRequest(endpoint="#{pageSession.selfUrl}/group-map/#{group}" method="DELETE");
                            }
                        }
                        //Remove the remaining principal elements#{pageSession.oldPrincipalsList}.
                        foreach(var="principal" list="#{pageSession.oldPrincipalsList}") {
                            gf.restRequest(endpoint="#{pageSession.selfUrl}/principal-map/#{principal}" method="DELETE");
                        }
                    }                   
                    prepareSuccessfulMsg();
                    gf.redirect(page="#{selfPage}&mapName=#{pageSession.valueMap['name']}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
                />
            </sun:button>
            <sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
                <!command
                    if ('#{usersOptionG}=users') {                        
                        mapPut(map="#{pageSession.valueMap}" key="groupsMap" value="#{pageSession.valueMap2['groupMapping']}");
                    }
                    if ('#{usersOptionP}=principals') {
                        mapPut(map="#{pageSession.valueMap}" key="principalsMap" value="#{pageSession.valueMap2['principalMapping']}");
                    }
                    gf.createEntity(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}"
                        attrs="#{pageSession.valueMap}"
                        convertToFalse="#{pageSession.convertToFalseList}");
                    
                    gf.redirect(page="#{pageSession.parentPage}")
                />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" primary="#{false}"  text="$resource{i18n.button.Cancel}" >
                <!command
                    gf.redirect(page="#{parentPage}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>

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


<sun:form id="propertyForm">
#include "/common/shared/alertMsg.inc"
    <sun:title id="propertyContentPage" title="$resource{i18nc.standaloneInstanceNew.PageTitle}" helpText="$resource{i18nc.standaloneInstanceNew.PageTitleHelp}">
        <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
            <sun:button id="newButton" text="$resource{i18n.button.OK} (TBD)"
                onClick="if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')) {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;" >
            <!command
                //createInstance(Name="#{name}", Config="#{pageSession.config}", Node="#{pageSession.Node}", forCluster=#{pageSession.forCluster}" CopyConfig="#{configOptionGrp}");
                gf.redirect(page="#{pageSession.listLink}?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
             />
            </sun:button>
            <sun:button id="cancelButton" immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}" >
                <!command
                    gf.redirect(page="#{pageSession.listLink}");
                />
            </sun:button>

        </sun:panelGroup>
        </facet>
       
    </sun:title>

    <sun:propertySheet id="propertySheet">

        <sun:propertySheetSection id="propertSectionTextField">
            <sun:property id="NameTextProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" >
                <sun:textField id="NameText" text="#{name}" styleClass="required" columns="$int{55}" required="#{true}" />
            </sun:property>
            <sun:property id="node"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.cluster.Node}" helpText="$resource{i18nc.cluster.NodeHelp}">
                <sun:dropDown id="node" labels="#{pageSession.nodesList}" selected="#{pageSession.Node}" >
                </sun:dropDown>
            </sun:property>
            <sun:property id="configProp"  rendered="#{pageSession.forStandalone}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18nc.clusterNew.Configuration}" helpText="$resource{i18nc.clusterNew.ConfigurationHelp}">
                <sun:dropDown id="Config" labels="#{pageSession.configsList}" selected="#{pageSession.config}" >
                </sun:dropDown>
            </sun:property>

            <sun:property id="configOptionProp" rendered="#{pageSession.forStandalone}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}">
                <sun:radioButton id="optC"  name="configOptionGrp" label="$resource{i18nc.clusterNew.CopyConfig}" selected="#{configOption}" selectedValue="copy" />
                "<br />
                <sun:radioButton id="optR"  name="configOptionGrp" label="$resource{i18nc.clusterNew.ReferenceConfig}" selected="#{configOption}" selectedValue="ref" />
                "<br />
            </sun:property>
        </sun:propertySheetSection>
    </sun:propertySheet>

    <sun:hidden id="helpKey" value="#{pageSession.helpId}" />
</sun:form>

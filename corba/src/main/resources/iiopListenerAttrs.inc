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
<!-- iiopListener.inc  -->

<sun:propertySheet id="propertySheet">			 
    <!-- General Settings section -->
    <sun:propertySheetSection id="generalSettingsSetion">
        <sun:property id="IiopNameProp"  rendered="#{pageSession.edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}" >
            <sun:staticText id="IiopName" text="#{pageSession.valueMap['Id']}" />
        </sun:property>
        <sun:property id="IiopNameTextProp"  rendered="#{!pageSession.edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}">
            <sun:textField id="IiopNameText" styleClass="required" text="#{pageSession.valueMap['Id']}" columns="$int{55}" required="#{true}" />
        </sun:property>
        <sun:property id="NetwkAddrProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_corba.iiopListener.netwkAddrLabel}" helpText="$resource{i18n_corba.iiopListener.netwkAddrHelp}" >
            <sun:textField id="NetwkAddr" styleClass="required"  columns="$int{20}" text="#{pageSession.valueMap['Address']}" required="#{true}" />
        </sun:property>
        <sun:property id="ListenerPortProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_corba.iiopListener.listPortLabel}" helpText="$resource{i18n_corba.iiopListener.listPortHelp}" >
            <sun:textField id="ListenerPort" styleClass="required port" columns="$int{20}" text="#{pageSession.valueMap['Port']}" required="#{true}" />
        </sun:property>
        <sun:property id="ListenerProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_corba.iiopListener.listenerLabel}" helpText="$resource{i18n_corba.iiopListener.listenerHelp}" >
            <sun:checkbox label="$resource{i18n.desc.Enabled}" selected="#{pageSession.valueMap['Enabled']}" selectedValue="true" />
        </sun:property>
        <sun:property id="SecurityProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_corba.iiopListener.securityEnable}"  helpText="$resource{i18n_corba.iiopListener.securityEnableHelp}" >
            <sun:checkbox id="Security" label="$resource{i18n.desc.Enabled}" selected="#{pageSession.valueMap['SecurityEnabled']}" selectedValue="true" />
        </sun:property>
    </sun:propertySheetSection>
</sun:propertySheet>    


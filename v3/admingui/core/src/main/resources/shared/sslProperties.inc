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
<!-- configuration/sslProperties.inc -->

    <sun:title id="title" title="#{sslPageTitle}" >
        <sun:helpInline id="pageHelp" rendered="#{!edit}" style="padding: 8pt" type="page" text="$resource{i18n.edJmxConnector.sslPageHelp}"/>
        <sun:title id="subtitle" rendered="#{!edit}" title="$resource{i18n.edJmxConnector.sslSettingsLabel}" />
        <!-- Buttons  -->                    
        <!facet pageButtonsTop>
#include "/shared/sslPropertiesButtons.inc"
        </facet>                     
     </sun:title>
     <!-- PropertySheet .... -->
     <sun:propertySheet id="propertySheet">			 
        <sun:propertySheetSection>
            <!beforeCreate
                getSSLProperties(ConfigName="#{configName}", Name="#{tabName}", Type="#{type}", Edit="#{edit}", 
                ClientAuth=>$attribute{clientAuth}, CertNickname=>$attribute{certNickname}, SSL3Prop=>$attribute{ssl3prop}, 
                SSL2Prop=>$attribute{ssl2prop}, TLSProp=>$attribute{tlsprop}, CommonCiphersList=>$pageSession{commonCiphers}, EphemeralCiphersList=>$pageSession{ephCiphers}, 
                OtherCiphersList=>$pageSession{otherCiphers}, EccCiphersList=>$pageSession{eccCiphers},
                SelectedCommon=>$pageSession{selectedCommonCiphers}, SelectedEph=>$pageSession{selectedEphCiphers},
                SelectedOther=>$pageSession{selectedOtherCiphers}, SelectedEcc=>$pageSession{selectedEccCiphers}); 
             />
             <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{sheetId});
             />
             <sun:property id="ClientAuthProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edJmxConnector.clientAuthLabel}" helpText="$resource{i18n.edJmxConnector.clientAuthHelp}" >
                <sun:checkbox id="ClientAuth" label="$resource{i18n.desc.Enabled}" selected="#{requestScope.clientAuth}" />   
             </sun:property>
             <sun:property id="CertNicknameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edJmxConnector.certNicknameLabel}" helpText="$resource{i18n.edJmxConnector.certNicknameHelp}" >
                <sun:textField id="CertNickname" columns="$int{20}" text="#{requestScope.certNickname}" />      
             </sun:property>
             <sun:property id="SSL3Prop"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edJmxConnector.ssl3Label}" >
                <sun:checkbox id="SSL3" label="$resource{i18n.desc.Enabled}" selected="#{requestScope.ssl3prop}" />   
             </sun:property>
             <sun:property id="SSL2Prop"  rendered="#{ssl2}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.ssl2Label}" >
                <sun:checkbox id="SSL2" label="$resource{i18n.desc.Enabled}" selected="#{requestScope.ssl2prop}" />   
             </sun:property>
             <sun:property id="TLSProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edJmxConnector.tlsLabel}" >
                <sun:checkbox id="TLS" label="$resource{i18n.desc.Enabled}" selected="#{requestScope.tlsprop}" />   
             </sun:property>
            </sun:propertySheetSection>
            <sun:propertySheetSection label="$resource{i18n.ssl.cipherssuites}"  helpText="$resource{i18n.edJmxConnector.ciphersHelp}">
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{cipherId});
             />
             <sun:property id="ciphersProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{true}" >
               <sun:staticText id="ciphers2" text="$resource{i18n.edJmxConnector.ciphersHelp}"/> 
             </sun:property>
               <sun:property id="CommonCiphersProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{true}">
                    <sun:addRemove id="commonAddRemove"
                        selectAll="$boolean{true}" 
                        items="#{commonCiphers}"
                        selected="#{selectedCommonCiphers}"
                        availableItemsLabel="$resource{i18n.ssl.availableCommonCiphersLabel}"
                        selectedItemsLabel="$resource{i18n.ssl.selectedCommonCiphersLabel}">
                    </sun:addRemove>       
                </sun:property>
            <sun:property id="EphemeralCiphersProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{true}" >
                <sun:addRemove id="ephemeralAddRemove"
                    selectAll="$boolean{true}" 
                    items="#{ephCiphers}"
                    selected="#{selectedEphCiphers}"
                    availableItemsLabel="$resource{i18n.ssl.availableEphCiphersLabel}"
                    selectedItemsLabel="$resource{i18n.ssl.selectedEphCiphersLabel}">
                </sun:addRemove>       
             </sun:property>
             <sun:property id="OtherCiphersProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{true}" >
                <sun:addRemove id="otherAddRemove"
                    selectAll="$boolean{true}" 
                    items="#{otherCiphers}"
                    selected="#{selectedOtherCiphers}"
                    availableItemsLabel="$resource{i18n.ssl.availableOtherCiphersLabel}"
                    selectedItemsLabel="$resource{i18n.ssl.selectedOtherCiphersLabel}">
                </sun:addRemove>       
             </sun:property>
             <sun:property id="EccCiphersProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{true}" >
                <sun:addRemove id="eccAddRemove"
                    selectAll="$boolean{true}" 
                    items="#{eccCiphers}"
                    selected="#{selectedEccCiphers}"
                    availableItemsLabel="$resource{i18n.ssl.availableEccCiphersLabel}"
                    selectedItemsLabel="$resource{i18n.ssl.selectedEccCiphersLabel}">
                </sun:addRemove>       
             </sun:property>
        </sun:propertySheetSection>
     </sun:propertySheet>

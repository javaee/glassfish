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
<!-- sslAttrs.inc -->

<sun:propertySheet id="propertySheet">
<sun:propertySheetSection>
    <!beforeCreate
        getProxyAttribute(objectNameStr="amx:pp=/,type=runtime" attrName="SupportedCipherSuites" value="#{requestScope.supportedCiphers}")
        convertToDifferentCiphersGroup(ciphers="#{requestScope.supportedCiphers}"
                CommonCiphersList="#{requestScope.availableCommon}"
                EphemeralCiphersList="#{requestScope.availableEphemeral}"
                OtherCiphersList="#{requestScope.availableOther}"
                EccCiphersList="#{requestScope.availableEcc}")
    />
     <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{sheetId});
     />
     
     <sun:property id="SSL3Prop"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.ssl3Label}" >
        <sun:checkbox id="SSL3" label="$resource{i18n.desc.Enabled}" selected="#{pageSession.valueMap['Ssl3Enabled']}" selectedValue="true" />
     </sun:property>
     <!--
     <sun:property id="SSL2Prop"  rendered="#{ssl2}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.ssl2Label}" >
        <sun:checkbox id="SSL2" label="$resource{i18n.desc.Enabled}" selected="#{pageSession.valueMap['Ssl2Enabled']}" selectedValue="true" />
     </sun:property>
     -->
     <sun:property id="TLSProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.tlsLabel}" >
        <sun:checkbox id="TLS" label="$resource{i18n.desc.Enabled}" selected="#{pageSession.valueMap['TlsEnabled']}" selectedValue="true"/>
     </sun:property>

     <sun:property id="ClientAuthProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.clientAuthLabel}" helpText="$resource{i18n.ssl.clientAuthHelp}" >
        <sun:checkbox id="ClientAuth" label="$resource{i18n.desc.Enabled}" selected="#{pageSession.valueMap['ClientAuthEnabled']}" selectedValue="true" />
     </sun:property>
     <sun:property id="CertNicknameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.certNicknameLabel}" helpText="$resource{i18n.ssl.certNicknameHelp}" >
        <sun:textField id="CertNickname" columns="$int{20}" text="#{pageSession.valueMap['CertNickname']}" />
     </sun:property>
     <sun:property id="keystore"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.keyStore}" helpText="$resource{i18n.ssl.keyStoreHelp}" >
        <sun:textField id="keystore" columns="$int{20}" text="#{pageSession.valueMap['KeyStore']}" />
     </sun:property>
     <sun:property id="trustedAlg"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.TrustAlgorithm}" helpText="$resource{i18n.ssl.TrustAlgorithmHelp}" >
        <sun:textField id="trustedAlg" columns="$int{20}" text="#{pageSession.valueMap['TrustAlgorithm']}" />
     </sun:property>
     <sun:property id="maxCertLength"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.trustMaxCert}" helpText="$resource{i18n.ssl.trustMaxCertHelp}" >
        <sun:textField id="maxCertLength" columns="$int{20}" text="#{pageSession.valueMap['TrustMaxCertLength']}" />
     </sun:property>
     <sun:property id="trustStore"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.ssl.trustStore}" helpText="$resource{i18n.ssl.trustStoreHelp}" >
        <sun:textField id="trustStore" columns="$int{20}" text="#{pageSession.valueMap['TrustStore']}" />
     </sun:property>


    </sun:propertySheetSection>


    <sun:propertySheetSection label="$resource{i18n.ssl.cipherssuites}"  helpText="$resource{i18n.ssl.ciphersHelp}">
    <!afterCreate
        getClientId(component="$this{component}" clientId=>$page{cipherId});
     />
     <sun:property id="ciphersProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{true}" >
       <sun:staticText id="ciphers2" text="$resource{i18n.ssl.ciphersHelp}"/>
     </sun:property>

       <sun:property id="CommonCiphersProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{true}">
            <sun:addRemove id="commonAddRemove"
                selectAll="$boolean{true}"
                items="#{pageSession.commonCiphers}"
                selected="#{pageSession.selectedCommon}"
                availableItemsLabel="$resource{i18n.ssl.availableCommonCiphersLabel}"
                selectedItemsLabel="$resource{i18n.ssl.selectedCommonCiphersLabel}">
            </sun:addRemove>
            <!beforeCreate
                StringArrayToSelectItemArray(stringArray="#{requestScope.availableCommon}" item="#{pageSession.commonCiphers}")
            />
        </sun:property>

        <sun:property id="EphemeralCiphersProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{true}" >
            <sun:addRemove id="ephemeralAddRemove"
                selectAll="$boolean{true}"
                items="#{pageSession.ephCiphers}"
                selected="#{pageSession.selectedEphemeral}"
                availableItemsLabel="$resource{i18n.ssl.availableEphCiphersLabel}"
                selectedItemsLabel="$resource{i18n.ssl.selectedEphCiphersLabel}">
            </sun:addRemove>
            <!beforeCreate
                StringArrayToSelectItemArray(stringArray="#{requestScope.availableEphemeral}" item="#{pageSession.ephCiphers}")
            />
         </sun:property>

     <sun:property id="OtherCiphersProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{true}" >
        <sun:addRemove id="otherAddRemove"
            selectAll="$boolean{true}"
            items="#{pageSession.other}"
            selected="#{pageSession.selectedOther}"
            availableItemsLabel="$resource{i18n.ssl.availableOtherCiphersLabel}"
            selectedItemsLabel="$resource{i18n.ssl.selectedOtherCiphersLabel}">
        </sun:addRemove>
        <!beforeCreate
                StringArrayToSelectItemArray(stringArray="#{requestScope.availableOther}" item="#{pageSession.other}")
            />
     </sun:property>
     <sun:property id="EccCiphersProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{true}" >
        <sun:addRemove id="eccAddRemove"
            selectAll="$boolean{true}"
            items="#{pageSession.eccCiphers}"
            selected="#{pageSession.selectedEcc}"
            availableItemsLabel="$resource{i18n.ssl.availableEccCiphersLabel}"
            selectedItemsLabel="$resource{i18n.ssl.selectedEccCiphersLabel}">
        </sun:addRemove>
        <!beforeCreate
                StringArrayToSelectItemArray(stringArray="#{requestScope.availableEcc}" item="#{pageSession.eccCiphers}")
            />
     </sun:property>
</sun:propertySheetSection>
</sun:propertySheet>
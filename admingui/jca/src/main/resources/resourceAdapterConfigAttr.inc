
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
<!-- jca/resourceAdapterConfigAttr.inc -->
<!-- used by resourceAdapterConfigNew.jsf and resourceAdapterConfigEdit.jsf -->
   

<sun:propertySheet id="propertySheet">
    <!-- Text Field section -->
    <sun:propertySheetSection id="propertSectionTextField">
        <sun:property id="name"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.resourceAdapterConfig.Name}">
            <sun:staticText id="name" text="#{pageSession.Name}"  rendered="#{Edit}" />
        </sun:property>

        <sun:property id="nameNew"  rendered="#{!edit}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.resourceAdapterConfig.Name}">
            <sun:dropDown id="resAdaptorProp" selected="#{pageSession.valueMap['Name']}" labels="$pageSession{connectorList}"
                    required="#{true}" submitForm="#{true}" immediate="#{true}" >
                <!beforeCreate
                     getApplicationBySnifferType(type="connector"  result="#{pageSession.connectorList}")
                     listAdd(list="#{pageSession.connectorList}" value="jmsra")
                />
                <!command
                    setAttribute(key="click" value="$this{component}");
                    setAttribute(key="name" value="#{click.selected}");
                    setAttribute(key="reload" value="#{true}" );
                    gf.navigate(page="#{pageSession.selfPage}");
                />
           </sun:dropDown>
        </sun:property>

        <sun:property id="threadPoolsIdProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.resourceAdapterConfig.threadPoolId}"
                helpText="$resource{i18njca.resourceAdapterConfig.ThreadPoolIdsHelp}" >
            //<sun:textField id="threadpoolsid" columns="$int{55}" text="#{pageSession.valueMap['ThreadPoolIds']}" />
            <sun:dropDown id="threadpoolsid" selected="#{pageSession.valueMap['ThreadPoolIds']}" labels="#{pageSession.threadPoolList}" >
                <!beforeCreate
                    getChildrenByType(parentObjectNameStr="amx:pp=/domain/configs/config[server-config],type=thread-pools" 
                        type="thread-pool" result="#{requestScope.tmpList}");
                    addEmptyFirstElement(in="#{requestScope.tmpList}" out="#{pageSession.threadPoolList}")
                />
                </sun:dropDown>

</sun:property>
        "<br /><br />
    </sun:propertySheetSection>
</sun:propertySheet> 



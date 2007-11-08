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
<!-- resourceNode/javaMailSession.inc -->
    
<!-- Page Title -->
<sun:title id="propertyContentPage" title="#{pageTitle}" helpText="#{pageTitleHelp}">
    <sun:button id="def" rendered="#{edit}"  primary="#{false}" text="$resource{i18n.button.LoadDefaults}" > 
    <!command
        getJavaMailDefaultInfo(jndiName="#{jndiName}"
            enabled=>$attribute{enabled} debug=>$attribute{debug}
            storeProtocol=>$attribute{storeProtocol}
            storeProtocolClass=>$attribute{storeProtocolClass}
            transportProtocol=>$attribute{transportProtocol}
            transportProtocolClass=>$attribute{transportProtocolClass}
        )
    />
    </sun:button>

    <!facet pageButtonsTop>
#include "resourceNode/javaMailSessionButtons.inc"
    </facet> 
</sun:title>
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">			 
        <event>
        <!beforeCreate
            getJavaMailInfo(jndiName="#{jndiName}" 
                    edit="#{edit}"
                    host=>$attribute{host} user=>$attribute{user}
                    from=>$attribute{from} description=>$attribute{description}
                    enabled=>$attribute{enabled} enabledString=>$page{enabledString}
                    debug=>$attribute{debug}
                    storeProtocol=>$attribute{storeProtocol}
                    storeProtocolClass=>$attribute{storeProtocolClass}
                    transportProtocol=>$attribute{transportProtocol}
                    transportProtocolClass=>$attribute{transportProtocolClass},
                    Properties=>$page{props});
            getTableList(Properties="#{props}", TableList=>$attribute{tableList}); 
        />
        </event>

    <sun:propertySheetSection id="propertSectionTextField" label="$resource{i18n.common.GeneralSettings}" >
        <sun:property id="jndiStatic"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}" >
            <sun:staticText id="Jndi" text="#{jndiName}"/> 
        </sun:property> 

        <sun:property id="jndiTextProp"  rendered="#{!edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}" helpText="$resource{i18n.javaMail.jndiHelp}">
            <sun:textField id="JndiText" styleClass="required" text="#{jndiName}" columns="$int{55}" required="#{true}" /> 
        </sun:property> 

        <sun:property id="hostProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.javaMail.mailHost}" helpText="$resource{i18n.javaMail.mailHostHelp}">
            <sun:textField id="host" styleClass="required" text="#{host}" columns="$int{55}" required="#{true}" /> 
        </sun:property>   

        <sun:property id="userProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.javaMail.user}" helpText="$resource{i18n.javaMail.userHelp}">
            <sun:textField id="user" styleClass="required" text="#{user}" columns="$int{55}" required="#{true}" /> 
        </sun:property>   

        <sun:property id="fromProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.javaMail.returnAddr}" helpText="$resource{i18n.javaMail.returnAddrHelp}">
            <sun:textField id="from" styleClass="required" text="#{from}" columns="$int{55}" required="#{true}" /> 
        </sun:property>   

        <sun:property id="descProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}" helpText="$resource{i18n.javaMail.descHelp}">
            <sun:textField id="desc" columns="$int{55}" text="#{description}" />      
        </sun:property>

        <sun:property id="statusProp" rendered="#{useCheckBox}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
            <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{requestScope.enabled}" />  
       </sun:property>

       <sun:property id="statusProp2" rendered="#{useString}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" 
            label="$resource{i18n.common.status}" helpText="$resource{i18n.application.EnableTargetHelp}">
            <sun:staticText  text="#{enabledString}" />  
       </sun:property>
   
        "<br /><br />
    </sun:propertySheetSection>

    <sun:propertySheetSection id="section2" label="$resource{i18n.common.Advanced}" >
        <sun:property id="storeProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.javaMail.storeProtocol}" helpText="$resource{i18n.javaMail.storeProtocolHelp}">
            <sun:textField id="storeP" columns="$int{55}" text="#{storeProtocol}" />      
        </sun:property>

        <sun:property id="storeClassProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.javaMail.storeClass}" helpText="$resource{i18n.javaMail.storeClassHelp}">
            <sun:textField id="storeClass" columns="$int{55}" text="#{storeProtocolClass}" />      
        </sun:property>

        <sun:property id="transPProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.javaMail.transportProtocol}" helpText="$resource{i18n.javaMail.transportProtocolHelp}">
            <sun:textField id="transP" columns="$int{55}" text="#{transportProtocol}" />      
        </sun:property>

        <sun:property id="transClassProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.javaMail.transProtocolClass}" helpText="$resource{i18n.javaMail.transProtocolClassHelp}">
            <sun:textField id="transPClass" columns="$int{55}" text="#{transportProtocolClass}" />      
        </sun:property>

        <sun:property id="debugProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.javaMail.Debug}">
            <sun:checkbox selected="#{debug}" helpText="$resource{i18n.javaMail.DebugHelp}" label="$resource{i18n.common.Enabled}" />  
       </sun:property>   
        "<br /><br />
    </sun:propertySheetSection>
</sun:propertySheet>    
#include "propertyTable.inc"





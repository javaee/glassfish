
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
<!-- jca/connectorConnectionPoolAdvance.jsf -->
<!-- used by connectorConnectionPool.jsf -->
   

<!-- PropertySheet .... -->
<sun:propertySheetSection id="connectionPropertySheet" label="$resource{i18njca.jcaPool.connectionSettings}">

    <sun:property id="p1"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.validateAtmostOnceInSeconds}" helpText="$resource{i18njca.jcaPool.validateAtmostOnceInSecondsHelp}">
        <sun:textField id="va" styleClass="integer" columns="$int{30}" text="#{pageSession.valueMap['ValidateAtmostOncePeriodInSeconds']}" /> 
        <sun:staticText id="vasec" text="$resource{i18n.common.Seconds}"/>
    </sun:property>

    <sun:property id="p2"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.connectionLeakTimeoutInSeconds}" helpText="$resource{i18njca.jcaPool.connectionLeakTimeoutInSecondsHelp}">
        <sun:textField id="p2" styleClass="integer" columns="$int{30}" text="#{pageSession.valueMap['ConnectionLeakTimeoutInSeconds']}"/> 
        <sun:staticText id="vasec" text="$resource{i18n.common.Seconds}"/>
   </sun:property>   

    <sun:property id="p3"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.connectionLeakReclaim}" helpText="$resource{i18njca.jcaPool.connectionLeakReclaimHelp}">
        <sun:checkbox  selected="#{pageSession.valueMap['ConnectionLeakReclaim']}" label=" " selectedValue="true"/>
    </sun:property> 

    <sun:property id="p4"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.connectionCreationRetryAttempts}" helpText="$resource{i18njca.jcaPool.connectionCreationRetryAttemptsHelp}">
        <sun:textField id="p4" styleClass="integer" columns="$int{30}" text="#{pageSession.valueMap['ConnectionCreationRetryAttempts']}"/> 
   </sun:property>   

    <sun:property id="p5"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.connectionCreationRetryIntervalInSeconds}" helpText="$resource{i18njca.jcaPool.connectionCreationRetryIntervalInSecondsHelp}">
        <sun:textField id="p5" styleClass="integer" columns="$int{30}" text="#{pageSession.valueMap['ConnectionCreationRetryIntervalInSeconds']}"/> 
        <sun:staticText id="p51" text="$resource{i18n.common.Seconds}"/>
   </sun:property>

   <sun:property id="poolingProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.pooling}" helpText="$resource{i18njca.jcaPool.poolingHelp}">
            <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{pageSession.valueMap['Pooling']}" selectedValue="true" />
       </sun:property>

    <sun:property id="p7"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.lazyConnectionAssociation}" helpText="$resource{i18njca.jcaPool.lazyConnectionAssociationHelp}">
        <sun:checkbox id="associate"  selected="#{pageSession.valueMap['LazyConnectionAssociation']}" label="$resource{i18n.common.Enabled}" onClick="enableDisableLazyConnection('#{associateId}');" selectedValue="true" >  
           <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{associateId});
            />
        </sun:checkbox>
   </sun:property>

    <sun:property id="p6"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.lazyConnectionEnlistment}" helpText="$resource{i18njca.jcaPool.lazyConnectionEnlistmentHelp}">
        <sun:checkbox id="enlist"  selected="#{pageSession.valueMap['LazyConnectionEnlistment']}" label="$resource{i18n.common.Enabled}"  selectedValue="true">  
            <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{enlistId});
            />
        </sun:checkbox>
    </sun:property>

    <sun:property id="p8"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.associationWithThread}" helpText="$resource{i18njca.jcaPool.associationWithThreadHelp}">
        <sun:checkbox  selected="#{pageSession.valueMap['AssociateWithThread']}" label="$resource{i18n.common.Enabled}" selectedValue="true"/>  
   </sun:property>

    <sun:property id="p9"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.matchConnections}" helpText="$resource{i18njca.jcaPool.matchConnectionsHelp}">
        <sun:checkbox  selected="#{pageSession.valueMap['MatchConnections']}" label="$resource{i18n.common.Enabled}" selectedValue="true"/>  
   </sun:property>

    <sun:property id="p10"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njca.jcaPool.maxConnectionUsageCount}" helpText="$resource{i18njca.jcaPool.maxConnectionUsageCountHelp}">
        <sun:textField id="p5" styleClass="integer" columns="$int{30}" text="#{pageSession.valueMap['MaxConnectionUsageCount']}"/> 
   </sun:property>

</sun:propertySheetSection>


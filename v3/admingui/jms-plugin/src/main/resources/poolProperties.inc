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
<sun:propertySheet id="propertySheet">
    <event>
        <!beforeCreate
            setAttribute(key="enabled" value="#{true}");
        />
    </event>
    <sun:propertySheetSection id="generalPropertySheet" label="$resource{i18n.common.GeneralSettings}">
        <sun:property id="jndiProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.jndiName}">
            <sun:textField id="jndiProp" styleClass="required" columns="$int{55}" text="#{pageSession.valueMap['Name']}" required="#{true}" rendered="#{!edit}" />
            <sun:staticText text="#{pageSession.valueMap['Name']}" required="#{true}" rendered="#{edit}"/>
        </sun:property>
        <sun:property id="nameProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.poolName}" rendered="#{edit}">
            <sun:staticText text="#{pageSession.valueMap['Name']}" required="#{true}"/>
        </sun:property>
        <sun:property id="resType"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.resType}">
            <sun:dropDown id="resType" selected="#{pageSession.valueMap['ConnectionDefinitionName']}" rendered="#{!edit}"
                labels={"","javax.jms.TopicConnectionFactory","javax.jms.QueueConnectionFactory","javax.jms.ConnectionFactory"} required="#{true}"/>
            <sun:staticText text="#{pageSession.valueMap['ConnectionDefinitionName']}" required="#{true}" rendered="#{edit}"/>
        </sun:property>
        <sun:property id="descProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.description}">
            <sun:textField id="descProp" columns="$int{55}" text="#{pageSession.valueMap['Description']}" />
        </sun:property>
        <sun:property id="statusProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.status}">
            <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{enabled}" />
        </sun:property>

        "<br /><br />
    </sun:propertySheetSection>
    <sun:propertySheetSection id="poolPropertySheet" label="$resource{i18n.common.PoolSettings}">
        <sun:property id="initSizeProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.initPoolSize}" helpText="$resource{i18n.common.initPoolSizeHelp}">
            <sun:textField id="ds" styleClass="required" columns="$int{10}" text="#{pageSession.valueMap['SteadyPoolSize']}" />
            <sun:staticText id="con1" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
        </sun:property>
        <sun:property id="maxProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.maxPoolSize}" helpText="$resource{i18n.common.maxPoolSizeHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" text="#{pageSession.valueMap['MaxPoolSize']}" />
            <sun:staticText id="con" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
        </sun:property>
        <sun:property id="resizeProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.poolResize}" helpText="$resource{i18n.common.poolResizeHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" text="#{pageSession.valueMap['PoolResizeQuantity']}" />
            <sun:staticText id="con" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
        </sun:property>
        <sun:property id="idleProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.idleTimeout}" helpText="$resource{i18n.common.idleTimeoutHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" text="#{pageSession.valueMap['IdleTimeoutInSeconds']}" />
            <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
        </sun:property>
        <sun:property id="maxWaitProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.maxWait}" helpText="$resource{i18n.common.maxWaitHelp}">
            <sun:textField id="ds" styleClass="integer" columns="$int{10}" text="#{pageSession.valueMap['MaxWaitTimeInMillis']}" />
            <sun:staticText id="mil" style="padding: 8pt" text="$resource{i18n.common.Milliseconds}"/>
        </sun:property>


        <sun:property id="failProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.connectorConnectionPool.onFail}" helpText="$resource{i18n.connectorConnectionPool.onFailHelp}">
            <sun:checkbox label="$resource{i18n.connectorConnectionPool.closeAll}" selected="#{pageSession.valueMap['FailAllConnections']}" selectedValue="true"/>
        </sun:property>
        <sun:property id="transprop"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.connectorConnectionPool.transSupport}" helpText="$resource{i18n.connectorConnectionPool.transSupportHelp}" >
            <sun:dropDown id="trans" selected="#{pageSession.valueMap['TransactionSupport']}" labels={"", "XATransaction", "LocalTransaction", "NoTransaction"} />
        </sun:property>
        <sun:property id="conValidationProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.jdbcPool.conValidation}" helpText="$resource{i18n.connectorConnectionPool.conValidationHelp}">
            <sun:checkbox label="$resource{i18n.common.Required}" selected="#{pageSession.valueMap['IsConnectionValidationRequired']}" selectedValue="true" />
        </sun:property>


    </sun:propertySheetSection>
</sun:propertySheet>
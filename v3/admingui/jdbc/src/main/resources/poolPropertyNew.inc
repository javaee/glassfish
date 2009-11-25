
<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 
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
<!-- jdbc/poolPropertyNew.inc -->

<!-- PropertySheet .... -->
<sun:propertySheet id="sheet" requiredFields="true">

<sun:propertySheetSection id="generalSheet" label="$resource{i18njdbc.jdbcPool.general}">
    <sun:property id="jndiProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.common.Name}">
        <sun:staticText escape="true" id="Jndi" text="#{wizardPoolExtra.Name}" />
    </sun:property>

    <sun:property id="resTypeWizProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n.common.resType}"  >
        <sun:staticText escape="true" id="resType" text="#{wizardPoolExtra.ResType}"  />
    </sun:property>

    <sun:property id="dbProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.dbVendor}"  >
        <sun:staticText escape="true" id="db" text="#{wizardPoolExtra.DBVendor}"/>
    </sun:property>

    <sun:property id="dsProp" labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.datasource}" helpText="$resource{i18njdbc.jdbcPool.datasourceHelpEnter}">
        <sun:dropDown  id="datasource" selected="#{wizardPoolExtra.DatasourceClassname}" labels="$attribute{DSList}"  disabled="#{!wizardPoolExtra.dsClassname}" submitForm="#{true}" immediate="#{true}">
            <!command
                    setAttribute(key="click" value="$this{component}");
                    mapPut(map="#{wizardPoolExtra}" key="DatasourceClassname" value="#{click.selected}");
                    updateJdbcConnectionPoolPropertiesTable();
                    gf.navigate(page="#{selfPage}");
                />
        </sun:dropDown>
        "<br/>
        <sun:textField id="datasourceField"  columns="$int{60}" text="#{wizardPoolExtra.DatasourceClassnameField}"  disabled="#{!wizardPoolExtra.dsClassname}" />
    </sun:property>
    <sun:property id="ddsProp" labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.driver}" helpText="$resource{i18njdbc.jdbcPool.driverHelpEnter}">
        <sun:dropDown id="ddatasource"  selected="#{wizardPoolExtra.DriverClassname}" labels="$attribute{DList}"  disabled="#{wizardPoolExtra.dsClassname}" />
        "<br/>
        <sun:textField id="ddatasourceField"  columns="$int{60}" text="#{wizardPoolExtra.DriverClassnameField}"  disabled="#{wizardPoolExtra.dsClassname}" />
    </sun:property>

    <sun:property id="pingProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.ping}" helpText="$resource{i18njdbc.jdbcPool.pingHelp}">
        <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{sessionScope.wizardMap.Ping}" selectedValue="true" />
   </sun:property>
    <sun:property id="descProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18n.common.description}" >
        <sun:textField id="desc" columns="$int{60}" text="#{sessionScope.wizardMap.Description}" />
    </sun:property>
    "<br /><br />
</sun:propertySheetSection>

			<!-- -------------------- Pool Settings ------------ -->
<sun:propertySheetSection id="poolSettingsPropertySheet" label="$resource{i18njdbc.jdbcPool.poolSettings}">
    <sun:property id="initSizeProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.initPoolSize}" helpText="$resource{i18njdbc.jdbcPool.initPoolSizeHelp}">
        <sun:textField id="ds" styleClass="integer" columns="$int{10}" text="#{sessionScope.wizardMap.SteadyPoolSize}" />
        <sun:staticText id="con1" style="padding: 8pt" text="$resource{i18n.common.Connections}"/> 
    </sun:property>

    <sun:property id="maxProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.maxPoolSize}" helpText="$resource{i18njdbc.jdbcPool.maxPoolSizeHelp}">
        <sun:textField id="ds2" styleClass="integer" columns="$int{10}" text="#{sessionScope.wizardMap.MaxPoolSize}" />
        <sun:staticText id="con2" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
    </sun:property>

    <sun:property id="resizeProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.poolResize}" helpText="$resource{i18njdbc.jdbcPool.poolResizeHelp}">
        <sun:textField id="ds3" styleClass="integer" columns="$int{10}" text="#{sessionScope.wizardMap.PoolResizeQuantity}" />
        <sun:staticText id="con3" style="padding: 8pt" text="$resource{i18n.common.Connections}"/>
    </sun:property>

    <sun:property id="idleProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.idleTimeout}" helpText="$resource{i18njdbc.jdbcPool.idleTimeoutHelp}">
        <sun:textField id="ds" styleClass="integer" columns="$int{10}" text="#{sessionScope.wizardMap.IdleTimeoutInSeconds}" />
        <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
    </sun:property>

    <sun:property id="maxWaitProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.maxWait}" helpText="$resource{i18njdbc.jdbcPool.maxWaitHelp}">
        <sun:textField id="ds" styleClass="integer" columns="$int{10}" text="#{sessionScope.wizardMap.MaxWaitTimeInMillis}" />
        <sun:staticText id="mil" style="padding: 8pt" text="$resource{i18n.common.Milliseconds}"/>
    </sun:property>

    "<br /><br />
</sun:propertySheetSection>

			

<!-- -------------------- Transaction Isolation ------------ -->

<sun:propertySheetSection id="transactionPropertySheet" label="$resource{i18njdbc.jdbcPool.transIsolationSection}">

    <sun:property id="nonTransProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.nonTransConnection}" helpText="$resource{i18njdbc.jdbcPool.nonTransConnectionHelp}">
        <sun:checkbox label="$resource{i18n.common.Enabled}" selected="#{sessionScope.wizardMap.NonTransactionalConnections}" selectedValue="true" />
   </sun:property>

    <sun:property id="transProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.transIsolation}" helpText="$resource{i18njdbc.jdbcPool.transIsolationHelp}" >
        <sun:dropDown id="transIso" selected="#{sessionScope.wizardMap.TransactionIsolationLevel}" labels={"" "read-uncommitted" "read-committed" "repeatable-read" "serializable" } />
    </sun:property>

    <sun:property id="levelProp"  labelAlign="left" noWrap="#{false}" overlapLabel="#{false}" label="$resource{i18njdbc.jdbcPool.isoLevel}" helpText="$resource{i18njdbc.jdbcPool.isoLevelHelp}">
        <sun:checkbox label="$resource{i18njdbc.jdbcPool.Guaranteed}" selected="#{sessionScope.wizardMap.IsIsolationLevelGuaranteed}" selectedValue="true" />
   </sun:property>
   "<br /><br />
</sun:propertySheetSection>
</sun:propertySheet>


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
<!-- mgmtRules/monitorRule.inc --> 
    <sun:property id="ObservedMbeanProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.observedMbeanLabel}" >
        <sun:staticText id="ObservedMbean" text="${observedMbean}" /> 
    </sun:property> 
    <sun:property id="ObservedAttrProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.observedAttrLabel}" >
        <sun:staticText id="ObservedAttr" text="${observedAttr}" /> 
    </sun:property> 
    <sun:property id="GranularityProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.granularityLabel}" >
        <sun:textField id="Granularity" styleClass="integer" columns="$int{65}" text="#{requestScope.granularity}" />      
    </sun:property> 
    <sun:property id="MonitorTypeProp" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.monitorTypeLabel}" >
        <sun:staticText id="MonitorType" text="#{monitorType}" /> 
        "<br /><br />
    </sun:property> 
    <sun:property id="DiffModeProp" rendered="#{!isString}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.diffModeLabel}" helpText="$resource{i18n.edmgmtRules.diffModeHelp}" >
        <sun:checkbox selected="#{requestScope.diffMode}" />   
    </sun:property>
    <sun:property id="NumTypeProp" rendered="#{!isString}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.numTypeLabel}" >
        <sun:dropDown id="NumType" selected="#{requestScope.numType}" items="$attribute{numTypesList}" />
    </sun:property> 
    <sun:property id="InitThresholdProp" rendered="#{isCounter}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.initThresholdLabel}" helpText="$resource{i18n.edmgmtRules.initThresholdHelp}" >
        <sun:textField id="InitThreshold" columns="$int{65}" text="#{requestScope.initThreshold}" required="#{isCounter}" />      
    </sun:property> 
    <sun:property id="OffsetProp" rendered="#{isCounter}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.offsetLabel}" helpText="$resource{i18n.edmgmtRules.offsetHelp}" >
        <sun:textField id="Offset" columns="$int{65}" text="#{requestScope.offset}" />      
    </sun:property> 
    <sun:property id="ModulusProp" rendered="#{isCounter}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.modulusLabel}" helpText="$resource{i18n.edmgmtRules.modulusHelp}" >
        <sun:textField id="Modulus" columns="$int{65}" text="#{requestScope.modulus}" />      
    </sun:property> 
    <sun:property id="ThreshholdProp" rendered="#{isGauge}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.ThreshholdLabel}" helpText="$resource{i18n.edmgmtRules.ThreshholdHelp}" >
        <sun:staticText id="LowPropInfo" text="$resource{i18n.edmgmtRules.ThreshholdLow}" />
        <sun:textField id="LowProp" columns="$int{65}" text="#{requestScope.lowThreshhold}" required="#{isGauge}" />      
        "<br />
        "<br />
        <sun:staticText id="HighPropInfo" text="$resource{i18n.edmgmtRules.ThreshholdHigh}" />
        <sun:textField id="HighProp" columns="$int{65}" text="#{requestScope.highThreshhold}" />      
    </sun:property> 
    <sun:property id="TriggerProp" rendered="#{isString}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.triggerLabel}" >
        <sun:dropDown id="Trigger" selected="#{requestScope.trigger}" items="$attribute{triggersList}" />
    </sun:property> 
    <sun:property id="ValueProp" rendered="#{isString}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.valueLabel}" helpText="$resource{i18n.edmgmtRules.valueHelp}" >
        <sun:textField id="Value" columns="$int{65}" text="#{requestScope.valueProp}" required="#{isString}" />      
    </sun:property>

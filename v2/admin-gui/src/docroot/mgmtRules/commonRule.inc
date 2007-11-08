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
<!-- mgmtRules/commonRule.inc --> 
<!-- LifeCycle and Trace Events --> 
<sun:property id="EventProp" rendered="#{isCommon}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.eventLabel}" >
    <sun:dropDown id="Event" selected="#{requestScope.event}" items="$attribute{eventsList}" required="#{true}" />
</sun:property> 
<!-- Timer Events --> 
<sun:property id="DateStringProp" rendered="#{isTimer}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.dateStringLabel}" helpText="$resource{i18n.edmgmtRules.dateStringHelp}" >
    <sun:textField id="DateString" columns="$int{65}" text="#{requestScope.dateString}" required="#{true}" />      
</sun:property> 
<sun:property id="PatternProp" rendered="#{isTimer}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.patternLabel}" helpText="$resource{i18n.edmgmtRules.patternHelp}" >
    <sun:textField id="Pattern" columns="$int{65}" text="#{requestScope.pattern}" />      
</sun:property> 
<sun:property id="PeriodProp" rendered="#{isTimer}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.periodLabel}" helpText="$resource{i18n.edmgmtRules.periodHelp}" >
    <sun:textField id="Period" columns="$int{65}" text="#{requestScope.period}" />      
    <sun:staticText id="PeriodInfo" text="$resource{i18n.common.Milliseconds}" />
</sun:property> 
<sun:property id="NoOccurencesProp" rendered="#{isTimer}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.noOccurencesLabel}" helpText="$resource{i18n.edmgmtRules.noOccurencesHelp}" >
    <sun:textField id="NoOccurences" columns="$int{65}" text="#{requestScope.noOccurences}" />      
</sun:property> 
<sun:property id="MessageProp" rendered="#{isTimer}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.messageLabel}" helpText="$resource{i18n.edmgmtRules.messageHelp}" >
    <sun:textField id="Message" columns="$int{65}" text="#{requestScope.message}" />      
</sun:property> 
<!-- Log Events --> 
<sun:property id="LoggerProp" rendered="#{isLog}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.loggerLabel}" helptext="$resource{i18n.edmgmtRules.loggerHelp}" >
    <sun:listbox id="Logger" items="$attribute{loggerList}" selected="#{requestScope.logger}" required="#{true}" />
</sun:property> 
<sun:property id="LevelProp" rendered="#{isLog}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.edmgmtRules.levelLabel}" >
    <sun:dropDown id="Level" selected="#{requestScope.level}" items="$attribute{levels}" required="#{true}" />
</sun:property> 

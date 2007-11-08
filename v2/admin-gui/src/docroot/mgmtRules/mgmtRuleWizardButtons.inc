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
<!-- mgmtRules/mgmtRuleWizardButtons.inc -->  
<!facet pageButtonsTop>
<sun:panelGroup id="topButtons">
    <sun:button id="previousButton" text="$resource{i18n.button.Previous}"
        primary="#{false}"
        onClick='javascript: document.getElementById("form2:validate").value="false"; ' >
        <!command
            setAttribute(key="fromStep2" value="#{true}")
            setMgmtRulesSessionValues(
            FromStep2="#{fromStep2}"
            EventType="#{wizardMgmtRule.eventType}"
            EventDesc="#{eventDesc}"
            Action="#{action}"
            SrcGroup="#{srcGroup}"
            CustomMbean="#{custMbean}"
            ObjectName="#{objName}"
            NotifType="#{notifType}"
            Event="#{event}"
            DateString="#{dateString}"
            Pattern="#{pattern}"
            Period="#{period}"
            NoOccurences="#{noOccurences}"
            Message="#{message}"
            Logger="#{logger}"
            Level="#{level}"
            ObservedMbean="#{observedMbean}"
            ObservedAttr="#{observedAttr}"
            Granularity="#{granularity}"
            MonitorType="#{monitorType}" 
            DiffMode="#{diffMode}"
            DiffModeGg="#{diffModeGg}"
            NumType="#{numType}"
            NumTypeGg="#{numTypeGg}"
            InitThreshold="#{initThreshold}"
            Offset="#{offset}"
            Modulus="#{modulus}"
            LowThresh="#{lowThreshhold}"
            HighThresh="#{highThreshhold}"
            Trigger="#{trigger}"
            ValueProp="#{valueProp}"
            ServerName="#{serverName}"
            );
            redirect(page="mgmtRulesNew1.jsf?configName=#{wizardMgmtRule.configName}&fromStep2=#{fromStep2}");
            />
    </sun:button>
    
    <sun:button id="finishButton" text="$resource{i18n.button.Finish}" 
        onClick="if(validateMgmtRule('#{wizardMgmtRule.eventType}', 'false')){
                    document.getElementById('form2:validate').value='false'; 
					date = getTextElement('#{dateStringId}').value;
					pattern = getTextElement('#{patternId}').value;
					if(!checkDatePattern(date, pattern, '')) {
						if(pattern == '') {
							pattern = 'mm/dd/yyyy';
						}
						return showAlert('$resource{i18n.msg.dateFormat}'+pattern);
					}
                    return true;
                  }
                  return false; ">
        <!command
            prepareSuccessfulMsg(); 
            setMgmtRulesSessionValues(
            FromStep2="#{false}"
            EventType="#{wizardMgmtRule.eventType}"
            EventDesc="#{eventDesc}"
            Action="#{action}"
            SrcGroup="#{srcGroup}"
            CustomMbean="#{custMbean}"
            ObjectName="#{objName}"
            NotifType="#{notifType}"
            Event="#{event}"
            DateString="#{dateString}"
            Pattern="#{pattern}"
            Period="#{period}"
            NoOccurences="#{noOccurences}"
            Message="#{message}"
            Logger="#{logger}"
            Level="#{level}"
            ObservedMbean="#{observedMbean}"
            ObservedAttr="#{observedAttr}"
            Granularity="#{granularity}"
            MonitorType="#{monitorType}" 
            DiffMode="#{diffMode}"
            DiffModeGg="#{diffModeGg}"
            NumType="#{numType}"
            NumTypeGg="#{numTypeGg}"
            InitThreshold="#{initThreshold}"
            Offset="#{offset}"
            Modulus="#{modulus}"
            LowThresh="#{lowThreshhold}"
            HighThresh="#{highThreshhold}"
            Trigger="#{trigger}"
            ValueProp="#{valueProp}"
            ServerName="#{serverName}"
            ); 
            redirect(page="managementRules.jsf?configName=#{wizardMgmtRule.configName}")
        />    
    </sun:button>

    <sun:button id="cancelButton" immediate="#{true}" primary="#{false}" text="$resource{i18n.button.Cancel}" > 
        <!command
            setAttribute(key="configName" value="#{wizardMgmtRule.configName}");
            setSessionAttribute(key="wizardMgmtRule" value="#{null}")
            setSessionAttribute(key="wizardRuleTypes" value="#{null}")
            redirect(page="managementRules.jsf?configName=${configName}")
        />    
    </sun:button>
    
</sun:panelGroup>
</facet> 

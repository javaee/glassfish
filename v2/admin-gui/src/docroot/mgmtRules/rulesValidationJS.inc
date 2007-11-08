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

" <script type="text/javascript">
    " function validateMgmtRule(ruleType, edit) { \
        if(ruleType == 'monitor') { \
            var generalId = '#{generalSheetId}'; \
            var counterId = '#{counterSectionId}'; \
            var gaugeId = '#{gaugeSectionId}'; \
            var stringId = '#{stringSectionId}'; \
            var counterMonitor; \
            var gaugeMonitor; \
            var stringMonitor; \
            var period = checkForIntValueOrEmpty(getTextElement(generalId.concat(':GranularityProp:Granularity')).value); \
            if(period != true){ \
                return showAlert('$resource{i18n.msg.JS.rules.periodNumRequired}'); \
            } \
            if(edit != 'true'){ \
                var observed = checkForValue(getTextElement(generalId.concat(':ObservedMbeanProp:ObservedMbean'))); \
                if(observed != true){ \
                    return showAlert('$resource{i18n.msg.JS.rules.observedRequired}'); \
                } \
                var attr = checkForValue(getTextElement('#{generalSheetId}:ObservedAttrProp:ObservedAttr')); \
                if(attr != true){ \
                    return showAlert('$resource{i18n.msg.JS.rules.attrRequired}'); \
                } \
                counterMonitor = document.getElementById(generalId.concat(':MonitorTypeProp:counterBtn')).checked; \
                gaugeMonitor = document.getElementById(generalId.concat(':MonitorTypeProp:gaugeBtn')).checked; \
                stringMonitor = document.getElementById(generalId.concat(':MonitorTypeProp:stringBtn')).checked; \
                if((counterMonitor == false) && (gaugeMonitor == false) && (stringMonitor == false)) { \
                    return showAlert('$resource{i18n.msg.JS.rules.typeRequired}'); \
                } \
            } \
            if(edit == 'true'){ \
                counterMonitor = '#{isCounter}'; \
                gaugeMonitor = '#{isGauge}'; \
                stringMonitor = '#{isString}'; \
            } \
            if((counterMonitor == true) || (counterMonitor == 'true')) { \
                var initTh = checkForValue(getTextElement(counterId.concat(':InitThresholdProp:InitThreshold'))); \
                if(initTh != true){ \
                    return showAlert('$resource{i18n.msg.JS.rules.initThreshRequired}'); \
                } \
                var initThVal = checkForIntValue(getTextElement(counterId.concat(':InitThresholdProp:InitThreshold')).value); \
                if(initThVal != true){ \
                    return showAlert('$resource{i18n.msg.JS.rules.initThreshNumRequired}'); \
                } \
                var offsetVal = checkForIntValueOrEmpty(getTextElement(counterId.concat(':OffsetProp:Offset')).value); \
                if(offsetVal != true){ \
                    return showAlert('$resource{i18n.msg.JS.rules.offsetNumRequired}'); \
                } \
                var modulusVal = checkForIntValueOrEmpty(getTextElement(counterId.concat(':ModulusProp:Modulus')).value); \
                if(modulusVal != true){ \
                    return showAlert('$resource{i18n.msg.JS.rules.modulusNumRequired}'); \
                } \
            } \
            if((gaugeMonitor == true) || (gaugeMonitor == 'true')) { \
                var lowProp = checkForValue(getTextElement(gaugeId.concat(':ThreshholdProp:LowProp'))); \
                var highProp = checkForValue(getTextElement(gaugeId.concat(':ThreshholdProp:HighProp'))); \
                if((lowProp != true) || (highProp != true)) { \
                    return showAlert('$resource{i18n.msg.JS.rules.threshRequired}'); \
                } \
                if(lowProp == true) { \
                    var lowVal = checkForIntValue(getTextElement(gaugeId.concat(':ThreshholdProp:LowProp')).value); \
                    if(lowVal != true){ \
                        return showAlert('$resource{i18n.msg.JS.rules.lowThreshNumRequired}'); \
                    } \
                } \
                if(highProp == true) { \
                    var highVal = checkForIntValue(getTextElement(gaugeId.concat(':ThreshholdProp:HighProp')).value); \
                    if(highVal != true){ \
                        return showAlert('$resource{i18n.msg.JS.rules.highThreshNumRequired}'); \
                    } \
                } \
            } \
            if((stringMonitor == true) || (stringMonitor == 'true')) { \
                var value = checkForValue(getTextElement(stringId.concat(':ValueProp:Value'))); \
                if(value != true){ \
                    return showAlert('$resource{i18n.msg.JS.rules.strValueReqiured}'); \
                } \
            } \
            document.getElementById('form2:validate').value='false'; \
            return true; \
        } \
        if(ruleType == 'notification') { \
            var mbeanOpt = document.getElementById('#{srcMbeanPropId}:opt1').checked; \
            var objOpt = document.getElementById('#{srcMbeanPropId}:opt2').checked; \
            if(mbeanOpt == true) { \
                var mbeanField = document.getElementById('#{srcMbeanPropId}:custMbean'); \
                var custMbean = (mbeanField.value != '') && (isWhitespace(mbeanField.value) == false); \
                if(custMbean == false) { \
                    return showAlert('$resource{i18n.msg.JS.rules.custMBeanRequired}'); \
                } \
            } \
            if(objOpt == true) { \
                var objName = checkForValue(getTextElement('#{srcMbeanPropId}:EnterObjNameProp')); \
                if(objName == false) { \
                    return showAlert('$resource{i18n.msg.JS.rules.objNameRequired}'); \
                } \
            } \
            return true; \
        } \
        if(ruleType == 'timer') { \
            var dateString = checkForValue(getTextElement('#{timerSheetId}:DateStringProp:DateString')); \
            if(dateString == false) { \
                return showAlert('$resource{i18n.msg.JS.rules.dateStringRequired}'); \
            } \
            if(guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')){ \
                document.getElementById('form2:validate').value='false'; \
                return true; \
            } \
        } \
    } 

" </script>

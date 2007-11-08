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
<!-- shared/logAnalyzerLoggers_1.inc -->
<!-- when including this page, should followed by including logAnalyzerLoggers_2.inc to make a complete page

    The beginning of the <!initPage  is in the actual file that include this page. 
     we need to set the help id. there.
-->

    setResourceBundle(key="i18n" bundle="com.sun.enterprise.tools.admingui.resources.Strings")
    setResourceBundle(key="help" bundle="com.sun.enterprise.tools.admingui.resources.Helplinks")
    
    if (!$pageSession{instanceName}){
        getRequestValue(key="instanceName" value=>$page{instanceName} );
        getRequestValue(key="timeStamp" value=>$page{timeStamp} );
    }
    getFormattedTime(ms="$pageSession{timeStamp}"  value=>$page{endTime});

    getChartingCookieInfo(name=>$page{cookieName},  doCharting=>$page{doCharting}, setCookieTo=>$page{setCookieTo});
    #println(" !!!!!!!!!!!!!!! $pageSession{cookieName} ,  $pageSession{setCookieTo} , $pageSession{doCharting}");
    if( $pageSession{doCharting}){
        setPageSessionAttribute(key="NotDoCharting" value="$boolean{false}");
    }
    if(! $pageSession{doCharting}){
        setPageSessionAttribute(key="NotDoCharting" value="$boolean{true}");
    }
    getErrorLoggersList(instanceName="#{instanceName}" timeStamp="#{timeStamp}"
            result=>$attribute{listOfRows}, hasResults=>$attribute{hasResults}
            #demo="8"
            doCharting="$pageSession{doCharting}"
            severeChartValues=>$attribute{severeChartValues}, severeChartLabels=>$attribute{severeChartLabels} 
            warningChartValues=>$attribute{warningChartValues}, warningChartLabels=>$attribute{warningChartLabels}
            hasSevereChart=>$attribute{hasSevereChart}  hasWarningChart=>$attribute{hasWarningChart}
            );
    if (!$pageSession{doCharting}){
            setAttribute(key="showSevereText" value="$boolean{false}");
            setAttribute(key="showWarningText" value="$boolean{false}");
    }
    if ($pageSession{doCharting}){
            setAttribute(key="showSevereText"  value="#{!requestScope.hasSevereChart}");
            setAttribute(key="showWarningText" value="#{!requestScope.hasWarningChart}");
    }

        <!-- 
    println("severeChartLabels :  $attribute{severeChartLabels}");
    println("severeChartValues:  $attribute{severeChartValues}");
    println("warningChartLabels :  $attribute{warningChartLabels}");
    println("warningChartValues:  $attribute{warningChartValues}");
        -->
    />
<sun:page id="page1">
<!beforeCreate
    getRequestValue(key="backLink", value=>$page{backLink} );
/>
        
#include "shared/restart.inc"
<sun:html id="html2">
    <sun:head id="propertyhead" title="$resource{i18n.logAnalyzerLoggers.pageTitle}">
        <sun:script url="../js/restart.js" />
    </sun:head>

    <sun:body id="body3" onLoad="javascript: synchronizeRestartRequired('#{requestScope.restartRequired}', '#{sessionScope.restartRequired}')">
        <sun:form id="form1">
#include treeBreadcrumbs.inc
        

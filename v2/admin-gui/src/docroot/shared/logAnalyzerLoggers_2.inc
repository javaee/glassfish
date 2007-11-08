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
<!-- shared/logAnalyzerLoggers_2.inc -->

<sun:title id="title" title="$resource{i18n.logAnalyzerLoggers.pageTitle}"
        helpText="$resource{i18n.logAnalyzerLoggers.pageTitleHelp}">

#include "shared/chartButton.inc"

    <!facet pageButtonsTop>
        <sun:button id="closeButton" immediate="#{true}" text="$resource{i18n.logAnalyzerLoggers.back.button}" >
            <!command
                redirect(page="#{backLink}?instanceName=#{instanceName}");
            />
        </sun:button>
        </facet>
 </sun:title>

"<br /><br />
     <sun:label id="time" style="padding: 8pt" text="$resource{i18n.logAnalyzerLoggers.ending} $pageSession{endTime}" />
"<br /><br/>
	<sun:markup tag="div" rendered="#{requestScope.showSevereText}" style="width:45%; float:left; padding-top:130px; padding-right:10px;vertical-align:middle; text-align:center;" >
	    <staticText value="$resource{i18n.logAnalyzerLoggers.noSevereChart}" />
	</sun:markup>
        <sun:markup tag="div" rendered="#{requestScope.hasSevereChart}" style="height:300px; width:45%; float:left; padding-left:10px;" > 
            <jmaki:chart
                type="jmaki.charting.bar"
                paddingBottom="$resource{i18n.logAnalyzerLoggers.chart.severe.paddingBottom}"
                xTitle="$resource{i18n.logAnalyzerLoggers.chart.severe.MapTitle}"
                xLabels="${severeChartLabels}"
                xRotate="$resource{i18n.logAnalyzerLoggers.chart.severe.xRotate}"
                yTitle="$resource{i18n.logAnalyzerLoggers.chart.severe.yTitle}"
                yTickCount="2"
                valueList="${severeChartValues}"
            />
        </sun:markup>

	<sun:markup tag="div" rendered="#{requestScope.showWarningText}" style="width:45%; float:right; padding-top:130px; padding-right:10px;vertical-align:middle; text-align:center;" >
	    <staticText value="$resource{i18n.logAnalyzerLoggers.noWarningChart}" />
	</sun:markup>
        <sun:markup tag="div" rendered="#{requestScope.hasWarningChart}" style="height:300px; width:45%; float:right; padding-right:10px;" > 
            <jmaki:chart
                type="jmaki.charting.bar"
                paddingBottom="$resource{i18n.logAnalyzerLoggers.chart.warning.paddingBottom}"
                xTitle="$resource{i18n.logAnalyzerLoggers.chart.warning.MapTitle}"
                xLabels="${warningChartLabels}"
                xRotate="$resource{i18n.logAnalyzerLoggers.chart.warning.xRotate}"
                yTitle="$resource{i18n.logAnalyzerLoggers.chart.warning.yTitle}"
                yTickCount="5"
                valueList="${warningChartValues}"
            />
        </sun:markup>

        "<br style="clear:both;" /><br />

    <sun:table id="logErrorTable" title="$resource{i18n.logAnalyzerLoggers.tableTitle}" sortPanelToggleButton="#{true}">
       <sun:tableRowGroup id="rowGroup1"  data={"$attribute{listOfRows}"} sourceVar="td">
           <!afterCreate
                 getClientId(component="$this{component}" clientId=>$page{logErrorTableRowGroup});
            />

        <sun:tableColumn headerText="$resource{i18n.logAnalyzerLoggers.Loggers}"  sort="loggerName" rowHeader="$boolean{true}" extrahtml="nowrap" id="col1">
            <sun:hyperlink id="link" text="#{td.value.loggerName}" 
                url="/logViewer/logViewer.jsf?timestamp=#{timeStamp}&logFile=server.log&instanceName=#{instanceName}&logger=#{td.value.loggerName}&level=WARNING" 
                target="LogViewerWindow" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.logAnalyzer.severeCount}"  sort="severe" rowHeader="$boolean{true}" id="severe">
            <sun:hyperlink id="severe" text="#{td.value.severe}" 
                url="/logViewer/logViewer.jsf?timestamp=#{timeStamp}&logFile=server.log&instanceName=#{instanceName}&logger=#{td.value.loggerName}&onlyLevel=true&level=SEVERE" 
                target="LogViewerWindow" />
        </sun:tableColumn>

          <sun:tableColumn headerText="$resource{i18n.logAnalyzer.warningCount}"  sort="warningCount" rowHeader="$boolean{true}" id="warning">
            <sun:hyperlink id="warning" text="#{td.value.warning}" 
                url="/logViewer/logViewer.jsf?timestamp=#{timeStamp}&logFile=server.log&instanceName=#{instanceName}&logger=#{td.value.loggerName}&onlyLevel=true&level=WARNING" 
                target="LogViewerWindow" />
        </sun:tableColumn>
 
       </sun:tableRowGroup>

    </sun:table>
 
	<sun:hidden id="helpKey" value="${helpKey}" />
            </sun:form>
        </sun:body>
"<br /><br/><br /><br/>
    </sun:html>
</sun:page>


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
<!-- shared/logAnalyzer_2.jsf -->

        <sun:title id="title" title="$resource{i18n.logAnalyzer.pageTitle}"
            helpText="$resource{i18n.logAnalyzer.pageTitleHelp}">

#include "shared/chartButton.inc"

            <!facet pageButtonsTop>
                <sun:button id="refreshButton" immediate="#{true}" text="$resource{i18n.button.Refresh}" 
                            onClick="javascript: return submitAndDisable(this, '$resource{i18n.button.Processing}');"> 
                    <!command
                        redirect(page="#{actualLink}?instanceName=#{instanceName}");
                    />
                </sun:button>
                </facet>
         </sun:title>
     "<br /><br/>
    
    <!if !#{hasResult}>
        <sun:staticText id="msg" style="padding: 8pt" text="$resource{i18n.logAnalyzer.noData}"/> 
    </!if> 

    <sun:label id="time" rendered="#{hasResult}" style="padding: 8pt" text="$resource{i18n.logAnalyzer.lastRefreshed} $pageSession{currentTime}" />
   
    <sun:markup tag="div" style="height:300px"  rendered="#{requestScope.hasChart}"   >
            <jmaki:chart rendered="#{requestScope.hasChart}"
                type="jmaki.charting.line"
                paddingBottom="90"
                #paddingBottom="$resource{i18n.logAnalyzer.chart.severe.paddingBottom}"
                legend="legend"
                xTitle="$attribute{chartTitle}"
                xLabels="${chartLabels}"
                xRotate="$resource{i18n.logAnalyzer.chart.severe.xRotate}"
                yTitle="$resource{i18n.logAnalyzer.chart.yLabel}"
                yTickCount="5"
                valueList="${chartValues}"
            />
            "<div id = "legend"  >
            # <sun:markup tag="div" id = "legend" style="width:100px" singleton="$boolean{true}"/>
    </sun:markup>

   "<br /><br />

    <!if #{hasResult} >
    <sun:table id="logErrorTable" title="$resource{i18n.logAnalyzer.tableTitle}" sortPanelToggleButton="#{true}" 
            deselectMultipleButton="$boolean{true}"
            deselectMultipleButtonOnClick="setTimeout('changeOneTableButton()', 0)"
            selectMultipleButton="$boolean{false}"
            selectMultipleButtonOnClick="setTimeout('changeOneTableButton()', 0)" >
         <!afterCreate
            getClientId(component="$this{component}" clientId=>$page{tableId});
        />

        <!facet actionsTop>
            <sun:panelGroup id="topActionsGroup1">
            <!afterCreate
                getClientId(component="$this{component}" clientId=>$page{topActionGroup});
            />
            <sun:button id="button1" text="$resource{i18n.logAnalyzer.showLoggers}" disabled="#{true}" toolTip="$resource{i18n.logAnalyzer.showLoggers.tooltip}" >
                <!command 
                    getUIComponent(clientId="$pageSession{logErrorTableRowGroup}", component=>$attribute{tableRowGroup});      
                    getSelectedSingleMapRows(TableRowGroup="$attribute{tableRowGroup}" selectedRows=>$attribute{selectedRows});
                    setAttribute(key="oneRow" value="#{selectedRows[0]}");
                    if ($oneRow) {
                        redirect(page="#{lgLoggersLink}?instanceName=#{instanceName}&timeStamp=#{oneRow.timeStamp}&backLink=#{actualLink}");
                    }
                />
            </sun:button>
            </sun:panelGroup>
         </facet>

       <sun:tableRowGroup id="rowGroup1" selected="#{td.value.selected}" data={"$attribute{listOfRows}"} sourceVar="td">
           <!afterCreate
                 getClientId(component="$this{component}" clientId=>$page{logErrorTableRowGroup});
            />

       <sun:tableColumn selectId="select" rowHeader="$boolean{false}" id="col0">
                <sun:radioButton id="select" name="aGroup" selectedValue="$boolean{true}" 
                        disabled="#{td.value.disabled}" selected="#{td.value.selected}" 
                        onClick="setTimeout('initAllRows(); changeTwoTableButtons()', 0)" />
       </sun:tableColumn>    

        <sun:tableColumn headerText="$resource{i18n.logAnalyzer.beginSampleTime}"  sort="timeStamp" rowHeader="$boolean{true}" extrahtml="nowrap" id="col1">
            <sun:hyperlink id="link" text="#{td.value.timeStampFormatted}" toolTip="$resource{i18n.logAnalyzer.timestampTooltip}" 
                url="/logViewer/logViewer.jsf?timestamp=#{td.value.timeStamp}&logFile=server.log&instanceName=#{instanceName}&level=WARNING" target="LogViewerWindow" />
        </sun:tableColumn>

        <sun:tableColumn headerText="$resource{i18n.logAnalyzer.severeCount}"  sort="severeCount" rowHeader="$boolean{true}" id="severe">
            <sun:hyperlink id="severe" text="#{td.value.severeString}" toolTip="$resource{i18n.logAnalyzer.severeLinkTooltip}"
                url="/logViewer/logViewer.jsf?timestamp=#{td.value.timeStamp}&logFile=server.log&instanceName=#{instanceName}&onlyLevel=true&level=SEVERE" target="LogViewerWindow"/>
        </sun:tableColumn>

          <sun:tableColumn headerText="$resource{i18n.logAnalyzer.warningCount}"  sort="warningCount" rowHeader="$boolean{true}" id="warning">
            <sun:hyperlink id="warning" text="#{td.value.warningString}"   toolTip="$resource{i18n.logAnalyzer.warningLinkTooltip}"
                url="/logViewer/logViewer.jsf?timestamp=#{td.value.timeStamp}&logFile=server.log&instanceName=#{instanceName}&onlyLevel=true&level=WARNING" target="LogViewerWindow" />
        </sun:tableColumn>

       </sun:tableRowGroup>

    </sun:table>
</if>


	<sun:hidden id="helpKey" value="$attribute{helpKey}" />
            </sun:form>
"<br /><br/><br /><br/><br /><br/>
        </sun:body>
#include "changeButtonsJS.inc" 
    </sun:html>
</sun:page>


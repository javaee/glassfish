<?xml version="1.0" encoding="UTF-8"?>
<!--
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 */
 -->
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">

    <jsp:directive.page contentType="text/html;charset=ISO-8859-1" pageEncoding="UTF-8"/>
    
    <f:view>
        <webuijsf:page frame="true">
            <webuijsf:html>
                <webuijsf:form style="margin-left:10px;margin-right:10px;" >

                <f:loadBundle basename="com.sun.enterprise.tools.admingui.resources.Strings" var="msgs" />
                <webuijsf:head title="#{msgs['jbi.statistics.title']}" />

                    <table>
                      <tr>
                        <td align="left" valign="middle" width="90%" >
                            <webuijsf:contentPageTitle id="pagetitle" title="#{StatisticsBean.title}" helpText="#{msgs['jbi.statistics.helpInline']}" />
                        </td>
                      </tr>
                    </table>

                    <br></br>
      
                    <webuijsf:table id="table1" > 

                        <!-- Title -->
                        <f:facet name="title">
                         <webuijsf:staticText text="#{StatisticsBean.tableTitle}"/>
                        </f:facet>

                        <webuijsf:tableRowGroup id="rowHeaderDef" collapsed="true" >

                            <webuijsf:tableColumn id="endPoint"
                                headerText="#{msgs['jbi.statistics.endpoint']}" rowHeader="true" >
                            </webuijsf:tableColumn>

                            <webuijsf:tableColumn id="receivedRequests"
                                headerText="#{msgs['jbi.statistics.receivedRequests']}" rowHeader="true">
                            </webuijsf:tableColumn>
                            
                            <webuijsf:tableColumn id="receivedReplies" headerText="#{msgs['jbi.statistics.receivedReplies']}" rowHeader="true">
                            </webuijsf:tableColumn>
                            
                            <webuijsf:tableColumn id="receivedErrors" headerText="#{msgs['jbi.statistics.receivedErrors']}" rowHeader="true">
                            </webuijsf:tableColumn>
                            
                            <webuijsf:tableColumn id="receivedDones" headerText="#{msgs['jbi.statistics.receivedDones']}" rowHeader="true">
                            </webuijsf:tableColumn>
                            
                            <webuijsf:tableColumn id="sentRequests"
                                headerText="#{msgs['jbi.statistics.sentRequests']}" rowHeader="true">
                            </webuijsf:tableColumn>
                            
                            <webuijsf:tableColumn id="sentReplies" headerText="#{msgs['jbi.statistics.sentReplies']}" rowHeader="true">
                            </webuijsf:tableColumn>
                            
                            <webuijsf:tableColumn id="sentErrors" headerText="#{msgs['jbi.statistics.sentErrors']}" rowHeader="true">
                            </webuijsf:tableColumn>
                            
                            <webuijsf:tableColumn id="sentDones" headerText="#{msgs['jbi.statistics.sentDones']}" rowHeader="true">
                            </webuijsf:tableColumn>
                           
                        </webuijsf:tableRowGroup>

                        <webuijsf:tableRowGroup id="provisioningGroup" sourceData="#{StatisticsBean.provisioningStatistics}" sourceVar="stats" 
                          groupToggleButton="true">

                           <!-- Row group header -->
                           <f:facet name="header">
                            <webuijsf:panelGroup id="groupHeader">
                              <webuijsf:markup tag="span" extraAttributes="class='TblGrpLft'">
                                <webuijsf:staticText styleClass="TblGrpTxt" text="#{msgs['jbi.statistics.provisioningEndpoint']}"/>
                              </webuijsf:markup>
                              <webuijsf:markup tag="span" extraAttributes="class='TblGrpRt'">
                                <webuijsf:staticText styleClass="TblGrpMsgTxt" text=""/>
                              </webuijsf:markup>
                            </webuijsf:panelGroup>
                           </f:facet>  

                          <webuijsf:tableColumn id="endpoint"
                            extraHeaderHtml="nowrap='nowrap'"
                            rowHeader="true" >
                            <webuijsf:staticText text="#{stats.value.endpointShort}" toolTip="#{stats.value.namespace}" />
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="receivedRequests" >
                            <webuijsf:staticText text="#{stats.value.receivedRequests}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="receivedReplies" >
                            <webuijsf:staticText text="#{stats.value.receivedReplies}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="receivedErrors" >
                            <webuijsf:staticText text="#{stats.value.receivedErrors}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="receivedDones" >
                            <webuijsf:staticText text="#{stats.value.receivedDones}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="sentRequests">
                            <webuijsf:staticText text="#{stats.value.sentRequests}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="sentReplies" >
                            <webuijsf:staticText text="#{stats.value.sentReplies}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="sentErrors" >
                            <webuijsf:staticText text="#{stats.value.sentErrors}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="sentDones" >
                            <webuijsf:staticText text="#{stats.value.sentDones}"/>
                          </webuijsf:tableColumn>
                        </webuijsf:tableRowGroup>

                        <webuijsf:tableRowGroup id="consumingGroup"
                          
                          sourceData="#{StatisticsBean.consumingStatistics}" sourceVar="stats"
                          groupToggleButton="true">

                          <!-- Row group header -->
                          <f:facet name="header">
                            <webuijsf:panelGroup id="groupHeader">
                              <webuijsf:markup tag="span" extraAttributes="class='TblGrpLft'">
                                <webuijsf:staticText styleClass="TblGrpTxt" text="#{msgs['jbi.statistics.consumingEndpoint']}"/>
                              </webuijsf:markup>
                              <webuijsf:markup tag="span" extraAttributes="class='TblGrpRt'">
                                <webuijsf:staticText styleClass="TblGrpMsgTxt" text=""/>
                              </webuijsf:markup>
                            </webuijsf:panelGroup>
                          </f:facet>
                          
                          <webuijsf:tableColumn id="endpoint"
                            extraHeaderHtml="nowrap='nowrap'" >
                            <webuijsf:staticText text="#{stats.value.endpointShort}" toolTip="#{stats.value.namespace}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="receivedRequests" >
                            <webuijsf:staticText text="#{stats.value.receivedRequests}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="receivedReplies" >
                            <webuijsf:staticText text="#{stats.value.receivedReplies}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="receivedErrors" >
                            <webuijsf:staticText text="#{stats.value.receivedErrors}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="receivedDones" >
                            <webuijsf:staticText text="#{stats.value.receivedDones}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="sentRequests">
                            <webuijsf:staticText text="#{stats.value.sentRequests}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="sentReplies" >
                            <webuijsf:staticText text="#{stats.value.sentReplies}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="sentErrors" >
                            <webuijsf:staticText text="#{stats.value.sentErrors}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="sentDones" >
                            <webuijsf:staticText text="#{stats.value.sentDones}"/>
                          </webuijsf:tableColumn>
                        </webuijsf:tableRowGroup>

                        <webuijsf:tableRowGroup id="totalsGroup" groupToggleButton="true"
                          sourceData="#{StatisticsBean.totalsStatistics}" sourceVar="stats" >
                          <!-- Row group header -->
                          <f:facet name="header">
                            <webuijsf:panelGroup id="totalsHeader">
                              <webuijsf:markup tag="span" extraAttributes="class='TblGrpLft'">
                                <webuijsf:staticText styleClass="TblGrpTxt" text="#{msgs['jbi.statistics.totals']}"/>
                              </webuijsf:markup>
                              <webuijsf:markup tag="span" extraAttributes="class='TblGrpRt'">
                                <webuijsf:staticText styleClass="TblGrpMsgTxt" text=""/>
                              </webuijsf:markup>
                            </webuijsf:panelGroup>
                          </f:facet>

                          <webuijsf:tableColumn id="endpoint"
                            extraHeaderHtml="nowrap='nowrap'" >
                            <webuijsf:staticText text="#{stats.value.endpoint}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="receivedRequests" >
                            <webuijsf:staticText text="#{stats.value.receivedRequests}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="receivedReplies" >
                            <webuijsf:staticText text="#{stats.value.receivedReplies}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="receivedErrors" >
                            <webuijsf:staticText text="#{stats.value.receivedErrors}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="receivedDones" >
                            <webuijsf:staticText text="#{stats.value.receivedDones}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="sentRequests">
                            <webuijsf:staticText text="#{stats.value.sentRequests}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="sentReplies" >
                            <webuijsf:staticText text="#{stats.value.sentReplies}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="sentErrors" >
                            <webuijsf:staticText text="#{stats.value.sentErrors}"/>
                          </webuijsf:tableColumn>
                          <webuijsf:tableColumn id="sentDones" >
                            <webuijsf:staticText text="#{stats.value.sentDones}"/>
                          </webuijsf:tableColumn>

                        </webuijsf:tableRowGroup>


                    </webuijsf:table>

               </webuijsf:form>
            </webuijsf:html>
        </webuijsf:page>
    </f:view>

</jsp:root>

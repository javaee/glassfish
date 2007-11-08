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
              <webuijsf:head />
               <webuijsf:body>
                    <f:loadBundle basename="com.sun.enterprise.tools.admingui.resources.Strings" var="msgs" />

                    <webuijsf:form id="form">

                    <table cellpadding="2" cellspacing="2" border="0" style="margin-left:20px;">

                    <tr><td colspan="2" align="left">
                        <!-- Alert -->
                        <webuijsf:alert id="alert"
                                        summary="#{ConfigurationBean.alertSummary}"
                                        type="#{ConfigurationBean.alertType}"
                                        rendered="#{ConfigurationBean.alertMessageRendered}"
                                        detail="#{ConfigurationBean.alertMessage}" />
                        <br />
                   </td>
                   </tr>

                    <tr>
                    <td width="50%">
                    <webuijsf:contentPageTitle id="pagetitle" title="#{ConfigurationBean.title}" helpText="#{msgs['jbi.configuration.helpInline']}" style="text-align:left;" />    
                    </td>
    
                    <td style="white-space:nowrap;">
                        <webuijsf:button id="Save" text="#{msgs['jbi.configuration.label.save']}" 
                            actionExpression="#{ConfigurationBean.saveConfigs}" primary="true"    
                            rendered="#{ConfigurationBean.renderButtons}" />
                        <webuijsf:button id="actionReset" reset="true" text="#{msgs['jbi.configuration.label.reset']}" 
                            rendered="#{ConfigurationBean.renderButtons}" />
                     
                        <br />
                    </td>
                    </tr>
                    
                    <tr>
                        <td colspan="2">
                          <br />
                          <br />
                            <webuijsf:listbox label="#{msgs['jbi.configuration.label.instances']}"
                                required="true"
                                toolTip="#{msgs['jbi.configuration.label.instances.tooltip']}"
                                items="#{ConfigurationBean.targetOptions}"
                                multiple="true"
                                rows="5"
                                rendered="#{ConfigurationBean.renderButtons}"
                                selected="#{ConfigurationBean.selectedTargetOptions}"
                                style="margin-left:10px;"
                            />
                        </td>
                    </tr>

                    <tr>
                    <td>

                        <webuijsf:propertySheet id="propSheet" jumpLinks="true" style="text-align:left;" binding="#{ConfigurationBean.propertySheet}" >
                            
                        </webuijsf:propertySheet>                    
                    </td>
                    <td> <br /> </td>
                    </tr>
            
                    <tr>
                    <td>
                      <br />
                    </td>
                    <td style="white-space:nowrap;">
                        <br />
                        <webuijsf:button id="Save2" text="#{msgs['jbi.configuration.label.save']}" 
                            actionExpression="#{ConfigurationBean.saveConfigs}" primary="true"       
                            rendered="#{ConfigurationBean.renderButtons}" />
                        <webuijsf:button id="actionReset2" reset="true" text="#{msgs['jbi.configuration.label.reset']}" 
                            rendered="#{ConfigurationBean.renderButtons}"/>
                    </td>
                    </tr>
                        
                    </table>
                        
                    </webuijsf:form>


                </webuijsf:body>           
            </webuijsf:html>
        </webuijsf:page>
    </f:view>
    
</jsp:root>

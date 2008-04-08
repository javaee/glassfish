<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %> 
<%@taglib uri="http://www.sun.com/webui/webuijsf" prefix="webuijsf" %>

<f:view>
    <!--
      The contents of this file are subject to the terms
      of the Common Development and Distribution License
      (the License).  You may not use this file except in
      compliance with the License.
      
      You can obtain a copy of the license at
      https://woodstock.dev.java.net/public/CDDLv1.0.html.
      See the License for the specific language governing
      permissions and limitations under the License.
      
      When distributing Covered Code, include this CDDL
      Header Notice in each file and include the License file
      at https://woodstock.dev.java.net/public/CDDLv1.0.html.
      If applicable, add the following below the CDDL Header,
      with the fields enclosed by brackets [] replaced by
      you own identifying information:
      "Portions Copyrighted [year] [name of copyright owner]"
      
      Copyright 2007 Sun Microsystems, Inc. All rights reserved.
    -->
  <webuijsf:page>
    <webuijsf:head title="#{JavaHelpBean.tipsHeadTitle}" />
    <webuijsf:body styleClass="#{JavaHelpBean.bodyClassName}">
      <webuijsf:form id="tips">              
       <h2><webuijsf:staticText text="#{JavaHelpBean.tipsTitle}" /></h2>

<p><webuijsf:staticText text="#{JavaHelpBean.tipsImprove}" /></p>
<ul>
 <li><webuijsf:staticText text="#{JavaHelpBean.tipsImprove1}" /></li>

 <li><webuijsf:staticText text="#{JavaHelpBean.tipsImprove2}" /></li>
   
 <li><webuijsf:staticText text="#{JavaHelpBean.tipsImprove3}" /></li>
   
 <li><webuijsf:staticText text="#{JavaHelpBean.tipsImprove4}" /></li>
</ul>
<p>
<b><webuijsf:staticText text="#{JavaHelpBean.tipsNote}" /></b> <webuijsf:staticText text="#{JavaHelpBean.tipsNoteDetails}" />
</p>  
<p><webuijsf:staticText text="#{JavaHelpBean.tipsSearch}" /></p>

<ul>
  <li><webuijsf:staticText text="#{JavaHelpBean.tipsSearch1}" /></li>
  <li><webuijsf:staticText text="#{JavaHelpBean.tipsSearch2}" /></li>
  <li><webuijsf:staticText text="#{JavaHelpBean.tipsSearch3}" /></li>
  <li><webuijsf:staticText text="#{JavaHelpBean.tipsSearch4}" /></li>
</ul>
      </webuijsf:form>      
    </webuijsf:body> 
  </webuijsf:page>
</f:view>

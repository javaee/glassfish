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
    <webuijsf:head title="#{JavaHelpBean.buttonNavHeadTitle}" />      
    <webuijsf:body styleClass="#{JavaHelpBean.buttonBodyClassName}">
      <webuijsf:form id="helpButtonNavForm">
        <table border="0" cellspacing="0" cellpadding="0" width="100%">
          <tr>
            <td nowrap="nowrap">
              <webuijsf:markup tag="div" styleClass="#{JavaHelpBean.buttonClassName}">
                <webuijsf:imageHyperlink id="HelpBackButton" alt="#{JavaHelpBean.backButtonText}" icon="#{JavaHelpBean.backButtonIcon}" toolTip="#{JavaHelpBean.backButtonText}" onClick="javascript:window.parent.contentFrame.focus(); window.parent.contentFrame.history.back(); return false;" />
                <webuijsf:image id="Spacer1" icon="DOT" width="5" height="1" />
                <webuijsf:imageHyperlink id="HelpForwardButton" alt="#{JavaHelpBean.forwardButtonText}" icon="#{JavaHelpBean.forwardButtonIcon}" toolTip="#{JavaHelpBean.forwardButtonText}" onClick="javascript:window.parent.contentFrame.focus(); window.parent.contentFrame.history.forward(); return false;" />
                <webuijsf:image id="Spacer2" icon="DOT" width="10" height="1" />
                <webuijsf:imageHyperlink id="PrintButton" alt="#{JavaHelpBean.printButtonText}" icon="#{JavaHelpBean.printButtonIcon}" toolTip="#{JavaHelpBean.printButtonText}" onClick="window.parent.contentFrame.focus(); window.parent.contentFrame.print()" />
              </webuijsf:markup>
            </td>
          </tr>
        </table>
      </webuijsf:form>      
    </webuijsf:body> 
  </webuijsf:page>
</f:view>

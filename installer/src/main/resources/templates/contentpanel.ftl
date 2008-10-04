<#--
 # DO NOT ALTER OR REMOVE COPYRIGHT NOTICE OR THIS HEADER.
 #
 # Copyright 2006, 2007 Sun Microsystems, Inc. All rights reserved.
 # Use is subject to license terms.
 #
 # The contents of this file are subject to the terms of the Common Development
 # and Distribution License("CDDL") (the "License"). You may not use this file
 # except in compliance with the License.
 #
 # You can obtain a copy of the License at https://openinstaller.dev.java.net/license.html
 # or http://openinstaller.dev.java.net/license.txt . See the License for the
 # specific language governing permissions and limitations under the License.
 #
 # When distributing the Covered Code, include this CDDL Header Notice in each
 # file and include the License file at http://openinstaller.dev.java.net/license.txt .
 # If applicable, add the following below this CDDL Header, with the fields
 # enclosed by brackets [] replaced by your own identifying information:
 # "Portions Copyrighted [year] [name of copyright owner]"
 #
 # ident "@(#)%Name% %Revision% %Date% SMI"
 #
-->
<#list sections as theSection>
  <#if (sections?size = 1)>
    <scrollpane border="none" ${sizeAttr}="${contentpanel_size_m2}" name="gContentComponent" horizontalscrollbarpolicy="JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED" verticalscrollbarpolicy="JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED" Layout="GridBagLayout">
    <gridbagconstraints gridx="0" gridy="0" weightx="1" weighty="1" gridwidth="1" gridheight="1" fill="GridBagConstraints.BOTH" anchor="GridBagConstraints.CENTER" insets="0,0,0,0"/>
      <scrpanel border="none" background="FFFFFF" ${sizeAttr}="${contentpanel_size_m2}" name="gContents" Layout="GridBagLayout" >
      <gridbagconstraints gridx="0" gridy="0" weightx="1" weighty="1" gridwidth="1" gridheight="1" fill="GridBagConstraints.BOTH" anchor="GridBagConstraints.CENTER" insets="0,0,0,0"/>
          ${.vars["${theSection}"]}
    </scrpanel>
    </scrollpane>
  </#if>
</#list>
<#if (sections?size > 1)>
  <tabbedpane border="none" background="FFFFFF" ${sizeAttr}="${contentpanel_size_m2}" name="gContentComponent" selectedIndex="0">
    <gridbagconstraints gridx="0" gridy="0" weightx="1" weighty="1" gridwidth="1" gridheight="1" fill="GridBagConstraints.BOTH" anchor="GridBagConstraints.CENTER" insets="0,0,0,0"/>
    <#list sections as theSection>
      <scrollpane border="none" ${sizeAttr}="${contentpanel_size_m3}" name="${theSection}" horizontalscrollbarpolicy="JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED" verticalscrollbarpolicy="JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED">
      <scrpanel border="none" Layout="GridBagLayout" Autoscrolls="true" ${sizeAttr}="${contentpanel_size_m4}" name="${theSection}" background="FFFFFF" >
       <gridbagconstraints gridx="0" gridy="0" weightx="0" weighty="0" fill="GridBagConstraints.NONE" anchor="GridBagConstraints.NORTHWEST" />
        ${.vars["${theSection}"]}
      </scrpanel>
      </scrollpane>
    </#list>
  </tabbedpane>
</#if>


<#--
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 2008 Sun Microsystems, Inc. All rights reserved.
# 
# Use is subject to License Terms
#
--> 
<#list sections as theSection>
  <#if (sections?size = 1)>
    <scrollpane border="none" ${sizeAttr}="${contentpanel_size_m2}" name="gContentComponent" horizontalscrollbarpolicy="JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED" verticalscrollbarpolicy="JScrollPane.VERTICAL_SCROLLBAR_NEVER" Layout="GridBagLayout">
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


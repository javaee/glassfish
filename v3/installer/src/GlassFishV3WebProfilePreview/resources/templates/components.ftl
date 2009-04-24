<#--
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 2008 Sun Microsystems, Inc. All rights reserved.
# 
# Use is subject to License Terms
#
--> 
  <panel id="gMainPanel" name="gMainPanel" background="${mainpanel_bgcolor}" constraints="BorderLayout.CENTER" insets="12,12,12,12" Layout="GridBagLayout">

    <#--      L E F T   N A V I G A T I O N   P A N E L    -->
    <bgimagepanel id="gLeftPanelHolder" bgImage="${navigpanel_image}" Layout="GridBagLayout">
      <gridbagconstraints gridx="0" gridy="0" gridwidth="1" gridheight="4" weightx="0" weighty="1" fill="GridBagConstraints.VERTICAL" anchor="GridBagConstraints.NORTHWEST"/>
      <bgimagepanel name="topImagePanel"  bgImage="${top_left_image}" >
            <gridbagconstraints gridx="0" gridy="0" gridwidth="1" gridheight="1" weightx="0" weighty="0" insets="20,0,20,0" />
      </bgimagepanel>
      <#include "leftpanel.ftl">
      <bgimagepanel name="bottomImagePanel"  bgImage="${bottom_left_image}" >
            <gridbagconstraints gridx="0" gridy="3" gridwidth="1" gridheight="1" weightx="1" weighty="1" anchor="GridBagConstraints.SOUTH" insets="20,5,60,0" />
         </bgimagepanel>
    </bgimagepanel>

    <#--      T O P   L O G O   P A N E L    -->
    <bgimagepanel id="gLogoPanel" bgImage="${logopanel_image}" Layout="GridBagLayout">
      <gridbagconstraints gridx="1" gridy="0" gridwidth="1" gridheight="1" weightx="1" weighty="0" fill="GridBagConstraints.HORIZONTAL" anchor="GridBagConstraints.NORTHEAST"/>
      <smoothlabel id="gTitle1" Font="${title1_font}" Foreground="${title1_color}" text="${curPageTitle}">
        <gridbagconstraints gridx="1" gridy="0" weightx="1" weighty="1" anchor="GridBagConstraints.SOUTHWEST" insets="${contentpanel_insets}"/>
      </smoothlabel>
    </bgimagepanel>

    <#--      C O N T E N T   P A N E L    -->
    <shadowborderpanel id="gContentComponentHolder" name="gContentComponentHolder" <#if (sections?size = 1)> bordercolor="${shadow_border_color}" </#if> background="grey" minimumSize="${contentpanel_size}" preferredSize="${contentpanel_size}" Layout="GridBagLayout">
      <gridbagconstraints gridx="1" gridy="1" gridwidth="1" gridheight="2" weightx="1" weighty="1" fill="GridBagConstraints.BOTH" anchor="GridBagConstraints.EAST" insets="${contentpanel_insets}"/>
      <#include "contentpanel.ftl">
    </shadowborderpanel>

    <#--      B U T T O N S   P A N E L    -->
    <panel id="gNavButtonsPanelHolder" name="gNavButtonsPanelHolder" background="${btnpanel_bgcolor}" Layout="GridBagLayout">
      <gridbagconstraints gridx="1" gridy="3" gridwidth="1" gridheight="1" weightx="1" weighty="0" fill="GridBagConstraints.HORIZONTAL" anchor="GridBagConstraints.SOUTH" insets="${buttonpanel_insets}"/>
      <#include "buttonpanel.ftl">
    </panel>

  </panel>

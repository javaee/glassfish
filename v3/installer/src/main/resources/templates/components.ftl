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
-->
  <panel id="gMainPanel" name="gMainPanel" background="${mainpanel_bgcolor}" constraints="BorderLayout.CENTER" insets="0,0,0,0" Layout="GridBagLayout">

    <#--      L E F T   N A V I G A T I O N   P A N E L    -->
    <bgimagepanel id="gLeftPanelHolder" bgImage="${navigpanel_image}" Layout="GridBagLayout">
      <gridbagconstraints gridx="0" gridy="0" gridwidth="1" gridheight="3" weightx="0" weighty="1" fill="GridBagConstraints.VERTICAL" anchor="GridBagConstraints.NORTHWEST"/>
      <#include "leftpanel.ftl">
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
      <gridbagconstraints gridx="1" gridy="1" gridwidth="1" gridheight="1" weightx="1" weighty="1" fill="GridBagConstraints.BOTH" anchor="GridBagConstraints.EAST" insets="${contentpanel_insets}"/>
      <#include "contentpanel.ftl">
    </shadowborderpanel>

    <#--      B U T T O N S   P A N E L    -->
    <panel id="gNavButtonsPanelHolder" name="gNavButtonsPanelHolder" background="WHITE" Layout="GridBagLayout">
      <gridbagconstraints gridx="1" gridy="2" gridwidth="1" gridheight="1" weightx="1" weighty="0" fill="GridBagConstraints.HORIZONTAL" anchor="GridBagConstraints.SOUTH" insets="${buttonpanel_insets}"/>
      <#include "buttonpanel.ftl">
    </panel>

  </panel>

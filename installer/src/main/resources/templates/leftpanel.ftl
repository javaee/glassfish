<#--
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 2008 Sun Microsystems, Inc. All rights reserved.
# 
# Use is subject to License Terms
#
--> 
        <panel id="gLeftPanel" name="gLeftPanel" insets="0,0,0,0" Layout="GridBagLayout" opaque="false">
          <gridbagconstraints gridx="0" gridy="1" gridwidth="1" gridheight="2" weightx="1" weighty="1" fill="GridBagConstraints.BOTH" insets="${leftpanel_insets}"/>
         
          <buttongroup id="gBtnGroup">
            <#list buttons as theButton>
              <hradiobutton id="${theButton}" name="${theButton}" VerticalAlignment="TOP" HorizontalAlignment="RIGHT" Font="${leftpanel_font}"
                TextColor="<#if buttons?seq_index_of(theButton) < activeIndex>${leftpanel_done_textcolor}<#elseif buttons?seq_index_of(theButton) = activeIndex>${leftpanel_active_textcolor}<#else>${leftpanel_remaining_textcolor}</#if>"
                Text="${theButton}" Enabled="false">
                <gridbagconstraints gridx="0" gridy="${buttons?seq_index_of(theButton)}" weightx="1" weighty="0" fill="GridBagConstraints.HORIZONTAL" anchor="GridBagConstraints.NORTHWEST" insets="${leftpanel_button_insets}"/>
              </hradiobutton>
            </#list>
          </buttongroup>
          <panel name="theLeftExpandablePanel" opaque="false">
            <gridbagconstraints gridx="0" gridy="${buttons?size}" gridwidth="1" gridheight="1" weightx="1" weighty="1" fill="GridBagConstraints.BOTH" />
          </panel>
          
        </panel>

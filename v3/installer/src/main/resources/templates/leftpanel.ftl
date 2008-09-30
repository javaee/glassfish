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
 # ident "@(#)%Name% %Revision% %Date% SMI" -->
        <panel id="gLeftPanel" name="gLeftPanel" insets="0,0,0,0" Layout="GridBagLayout" opaque="false">
          <gridbagconstraints gridx="0" gridy="0" gridwidth="1" gridheight="3" weightx="1" weighty="1" fill="GridBagConstraints.BOTH" insets="${leftpanel_insets}"/>
          
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

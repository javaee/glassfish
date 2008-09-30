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
        <panel id="gNavButtonsPanel" name="gNavButtonsPanel" background="${btnpanel_bgcolor}" constraints="BorderLayout.CENTER" insets="0,0,0,0" Layout="GridBagLayout">
          <gridbagconstraints gridx="0" gridy="0" ${btnPanel_constraints} weightx="1" weighty="1" fill="GridBagConstraints.BOTH" anchor="GridBagConstraints.WEST" />
          <button border="LineBorder(BLACK)" id="gBtnCancel" name="gBtnCancel" Font="${button_font}" text="${btnCancel_text}" ActionCommand="AC_CANCEL">
            <gridbagconstraints gridx="0" gridy="0" ${btnCancel_constraints} gridwidth="1" gridheight="1" weightx="0" weighty="0" fill="GridBagConstraints.NONE" anchor="GridBagConstraints.WEST"/>
          </button>

          <button border="LineBorder(BLACK)" id="gBtnHelp" name="gBtnHelp" Font="${button_font}" text="${btnHelp_text}" visible="false" ActionCommand="AC_HELP">
            <gridbagconstraints gridx="1" gridy="0" ${btnHelp_constraints} gridwidth="1" gridheight="1" weightx="0" weighty="0" fill="GridBagConstraints.NONE" anchor="GridBagConstraints.WEST"/>
          </button>

          <panel name="theButtonsExpandablePanel" background="WHITE">
            <gridbagconstraints gridx="2" gridy="0" gridwidth="1" gridheight="1" weightx="1" weighty="1" fill="GridBagConstraints.BOTH" />
          </panel>

            <button border="LineBorder(BLACK)" id="gBtnBack" name="gBtnBack" Font="${button_font}" text="${btnBack_text}" ActionCommand="AC_BACK">
              <gridbagconstraints gridx="3" gridy="0" ${btnBack_constraints} gridwidth="1" gridheight="1" weightx="0" weighty="0" fill="GridBagConstraints.NONE" anchor="GridBagConstraints.EAST"/>
            </button>

          <#if showNext>
            <button border="LineBorder(BLACK)" id="gBtnNext" name="gBtnNext" Font="${button_font}" text="${btnNext_text}" ActionCommand="AC_NEXT">
              <gridbagconstraints gridx="4" gridy="0" ${btnNext_constraints} gridwidth="1" gridheight="1" weightx="0" weighty="0" fill="GridBagConstraints.NONE" anchor="GridBagConstraints.EAST"/>
            </button>
          <#else>
            <button border="LineBorder(BLACK)" id="gBtnExit" name="gBtnExit" Font="${button_font}" text="${btnExit_text}" ActionCommand="AC_EXIT">
              <gridbagconstraints gridx="4" gridy="0" ${btnNext_constraints} gridwidth="1" gridheight="1" weightx="0" weighty="0" fill="GridBagConstraints.NONE" anchor="GridBagConstraints.EAST"/>
            </button>
          </#if>

        </panel>

<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 
 The contents of this file are subject to the terms of either the GNU
 General Public License Version 2 only ("GPL") or the Common Development
 and Distribution License("CDDL") (collectively, the "License").  You
 may not use this file except in compliance with the License. You can obtain
 a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 language governing permissions and limitations under the License.
 
 When distributing the software, include this License Header Notice in each
 file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 Sun designates this particular file as subject to the "Classpath" exception
 as provided by Sun in the GPL Version 2 section of the License file that
 accompanied this code.  If applicable, add the following below the License
 Header, with the fields enclosed by brackets [] replaced by your own
 identifying information: "Portions Copyrighted [year]
 [name of copyright owner]"
 
 Contributor(s):
 
 If you wish your version of this file to be governed by only the CDDL or
 only the GPL Version 2, indicate your decision by adding "[Contributor]
 elects to include this software in this distribution under the [CDDL or GPL
 Version 2] license."  If you don't indicate a single choice of license, a
 recipient has the option to distribute your version of this file under
 either the CDDL, the GPL Version 2 or to extend the choice of license to
 its licensees as provided above.  However, if you add GPL Version 2 code
 and therefore, elected the GPL Version 2 license, then the option applies
 only if the new code is made subject to such option by the copyright
 holder.
-->
<!-- appserver/instancesExtraButtons.inc -->


<!define name="extraButton2">
   <sun:button id="button2" text="$resource{i18n.button.Start} (TBD)" disabled="#{true}" primary="#{false}"
        onClick="if ( getConfirm(this,'$resource{i18ncs.msg.JS.confirmStartInstances}') )
            { return submitAndDisable(this, '$resource{i18n.button.Processing}');}
            else {return false;} " >
        <!command
            getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
            //3.1_TODO: selectedInstanceAction(start="$boolean{true}" selectedRows="${selectedRows}");
            gf.redirect(page="#{pageSession.listLink}");
        />
    </sun:button>
</define>

<!define name="extraButton3">
    <sun:button id="button3" text="$resource{i18n.button.Stop} (TBD)" disabled="#{true}" primary="#{false}"
            onClick="if ( getConfirm(this,'$resource{i18ncs.msg.JS.confirmStopInstances}') )
                    { return submitAndDisable(this, '$resource{i18n.button.Processing}');}
                    else {return false;} " >
        <!command
            getUIComponent(clientId="$pageSession{tableRowGroupId}", component=>$attribute{tableRowGroup});
            getSelectedSingleMapRows(TableRowGroup="${tableRowGroup}" selectedRows=>$attribute{selectedRows});
            //3.1_TODO: selectedInstanceAction(start="$boolean{false}" selectedRows="${selectedRows}");
            gf.redirect(page="#{pageSession.listLink}");
         />
    </sun:button>
</define>



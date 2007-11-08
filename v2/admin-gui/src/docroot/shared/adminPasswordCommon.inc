<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 
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
<!-- Page Title -->
<sun:title id="propertyContentPage" title="$resource{i18n.server.AdminPasswordTitle}" 
    helpText="$resource{i18n.server.AdminPasswordPageHelp}">
<!-- Buttons  -->                    
<!facet pageButtonsTop>
<sun:panelGroup id="topButtons">
    <sun:button id="saveButton" text="$resource{i18n.button.Save}" 
        onClick=" if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')){
                    if  ( checkPSW() ){
                        return submitAndDisable(this, '$resource{i18n.button.Processing}');
                    }
                  }
                  return false; " >
                  
       <!command
        prepareSuccessfulMsg();
        saveUser(
            ConfigName="#{configName}"
            Realm="admin-realm"
            UserId="#{userId}"
            GroupList="#{groupList}"
            Password="#{password}"
            );
        />                        
    </sun:button>
</sun:panelGroup>
</facet>                     
</sun:title>
<!-- PropertySheet .... -->
<sun:propertySheet id="propertySheet">			 
    <!-- Text Field section -->               
    <sun:propertySheetSection id="propertSectionTextField">
        <sun:property id="userIdProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.server.Userid}">
            <sun:staticText id="UserId" text="#{userId}"/> 
        </sun:property>                       
        <sun:property id="groupListProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.server.GroupList}">                           
            <sun:staticText id="GroupList" text="#{groupList}"/>  
        </sun:property>                  
        <sun:property id="newPasswordProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.server.NewPassword}">                           
            <sun:passwordField id="NewPassword" styleClass="required" columns="$int{25}" text="#{requestScope.password}" required="#{true}" >
                 <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{ps1});
                />
            </sun:passwordField>
        </sun:property>  
        <sun:property id="confirmPasswordProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n.server.ConfirmNewPassword}">                           
            <sun:passwordField id="ConfirmPassword" styleClass="required" columns="$int{25}" text="#{requestScope.confirmPassword}" required="#{true}"  >
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{ps2});
                />
            </sun:passwordField>     
        </sun:property>                                              
    "<br /><br />
    </sun:propertySheetSection>

</sun:propertySheet>    
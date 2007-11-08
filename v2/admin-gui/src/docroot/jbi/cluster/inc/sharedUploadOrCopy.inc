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
<!-- jbi/cluster/inc/sharedUploadOrCopy.inc -->

<!beforeCreate
   setPageSessionAttribute(key="isValid", value="$boolean{true}")
/>

#include "jbi/cluster/inc/alertBox.inc"

<sun:title id="stepTitle" title='#{sessionScope["step1Title"]}' helpText='#{sessionScope["step1Summary"]}'>
    <!facet pageButtonsTop>
        <sun:panelGroup id="topButtons">
        
            <sun:button id="nextStep" 
                primary="$boolean{true}" 
                text="$resource{i18n.jbi.wizard.next.button}">
            <!command
getUIComponent(clientId="#{sessionScope.cpTxtFieldId}", component=>$attribute{cpPath});
getUIComponent(clientId="#{sessionScope.uploadRadioButtonId}", component=>$attribute{uploadRadio});
setAttribute(key="cpValue", value="#{cpPath.text}")
getUploadedFileLocation(file="#{uploadedFile}", uploadedLoc=>$attribute{uploadedPath});
getArchivePath(filePath="#{uploadRadio.checked ? uploadedPath : cpValue}", uploadRadio="#{uploadRadio.checked}", archiveFilePath=>$attribute{archiveFile});
isValidArchive(archivePath="#{archiveFile}", compType="#{sessionScope.compType}", navValid="#{navigateValid}", navInvalid="#{navigateInvalid}", isValid=>$session{isValid}, isAlertNeeded=>$session{isJbiAlertNeeded}, alertSummaryMsg=>$session{jbiAlertSummary}, alertDetailMsg=>$session{jbiAlertDetails}, navLoc=>$attribute{navPage});
deleteInvalidatedUploadedArchive(archiveStatus="#{sessionScope.isValid}", archivePath="#{archiveFile}", uploadSelected="#{uploadRadio.checked}")
jbiIncrementAlertCountIfNeeded(isAlertNeeded='$session{isJbiAlertNeeded}')
navigate(page="#{navPage}");
                />
            </sun:button>
            <sun:button id="cancelStep" 
                        primary="$boolean{false}" 
                        text="$resource{i18n.jbi.wizard.cancel.button}">
                <!command
                    redirect(page="#{sessionScope.newSharedCancelUrl}");
                />
            </sun:button>
        </sun:panelGroup>
    </facet>
    "<br />
</sun:title>

"<br />
"<br />

"<table>
    "<tr>
        "<td>
            <sun:label id="locationLbl" text="$resource{i18n.jbi.upload.or.copy.location.label}"/>
            <sun:radioButton id="rdBtn1" label="$resource{i18n.jbi.wizard.upload.appserver.text}" name="cpOrUpload" selected="$boolean{true}"  onClick="javascript: 
                disableComponent('#{txtFld2Id}','text');
                disableBtnComponent('#{filSelectBtnId}');
                enableComponent('#{uploadCompId}','file')"/>
        "</td>
    "</tr>
    "<tr>
        "<td>
            <sun:upload id="uploadComp" uploadedFile="#{requestScope.uploadedFile}" label="$resource{i18n.jbi.upload.or.copy.upload.label}" labelLevel="$int{3}" style="margin-left: 90px">
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{uploadCompId});
                />
            </sun:upload>
        "</td>
    "</tr>
    "<tr>
        "<td>
            <sun:radioButton id="rdBtn2" label="$resource{i18n.jbi.wizard.copy.appserver.text}" name="cpOrUpload"  style="margin-left: 60px" onClick="javascript:
                enableComponent('#{txtFld2Id}','text');
                enableBtnComponent('#{filSelectBtnId}');
                disableComponent('#{uploadCompId}','file')"/>
        "</td>
    "</tr>
    "<tr>
        "<td>
            <sun:label id="copy" for="txtFld2" text="$resource{i18n.jbi.upload.or.copy.file.label}" labelLevel="$int{3}" style="margin-left: 90px"/>
            <sun:textField id="txtFld2" columns="$int{50}" style="margin-left: 20px">
                <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{txtFld2Id});
                />
            </sun:textField>
            <sun:button id="filSelectBtn" 
		 text="$resource{i18n.button.BrowseFile}" 
		 disabled="#{true}"
                 primary="#{false}" 
		 onClick="javascript: var win = window.open('../../../jbi/cluster/fileChooser.jsf?txtFld2Id=#{txtFld2Id}', 'fileChooserPopup', 'height=580, width=650, top='+((screen.height - (screen.height/1.618))-(500/2))+',left='+((screen.width-650)/2)+', scrollbars, resizable');win.focus(); return false" >
                 <!afterCreate
                    getClientId(component="$this{component}" clientId=>$page{filSelectBtnId});
                  />
            </sun:button>
            "&nbsp;
        "</td>
    "</tr>
"</table>

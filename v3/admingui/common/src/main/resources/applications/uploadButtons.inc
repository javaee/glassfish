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
<!-- applications/uploadButtons.inc -->

<sun:button id="uploadButton" text="$resource{i18n.button.OK}" 
    onClick="javascript:
        if(getTextElement('#{pageSession.radioChoosenId}').value=='client') {
            result = (admingui.deploy.checkFileInputRequired('#{fileuploadId}', '#{reqMsg}'));
        } else {
            result = (checkRequired('#{dirPathId}', '#{reqMsg}'));
        }
        if (result == true) {
	    if (guiValidate('#{reqMsg}','#{reqInt}','#{reqPort}')){
		disableAllButtons();
		return submitAndDisable(this, '$resource{i18n.button.Processing}', '_self');
	    }
        }
        return false;"
     >
    <!command
        compare(obj1="#{uploadRdBtn}" obj2="client" objEqual=>$attribute{needUpload});
        if (${needUpload}){
            uploadFileToTempDir(file="#{uploadedFile}" uploadedTempFile=>$attribute{filePath}, origPath=>$attribute{origPath});
        }
        if (! ${needUpload}){
            setAttribute(key="origPath", value="#{dirPath}");
            setAttribute(key="filePath", value="#{dirPath}");
        }
<!--
        getUIComponent(clientId="$pageSession{propertyTableRowGroupId}", component=>$attribute{tableRowGroup});
        getAllSingleMapRows(TableRowGroup="$attribute{tableRowGroup}",  Rows=>$attribute{newList});
-->
        deploy(filePath="$attribute{filePath}"
              origPath="$attribute{origPath}"
              allMaps = "#{pageSession.deployMap}"
              appType = "#{pageSession.appType}"
              propertyList="$attribute{newList}"
        );
	gf.redirect(page="#{request.contextPath}/common/removeFrame.jsf?#{request.contextPath}/common/applications/applications.jsf&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}&bare=true");
    />
</sun:button>

<sun:button id="cancelButton" text="$resource{i18n.button.Cancel}" primary="#{false}" immediate="true"
    onClick="top.admingui.ajax.loadPage({url:'#{request.contextPath}/#{cancelPage}?name=$pageSession{target}'}); return false;">
//    <!command
//        println("cancel");
//        gf.navigate(page="#{request.contextPath}/#{cancelPage}?name=$pageSession{target}");
//    />
</sun:button>

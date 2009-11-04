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
<!-- regReminder.inc -->
#Text: # ffffff (white)
#Text Link: # b9d2e9
#Background: # 5382a1 

<sun:markup rendered="#{askRegistration}" tag="div" id="popup" style="height: 286px; width: 423px; z-index: 9999; background-image: url('#{request.contextPath}/resource/images/regReminderBackground.png'); background-color: #FFFFFF; background-repeat: no-repeat; position: absolute; top: 100px; margin: auto;  display: none; font-family:Arial, Helvetica, sans-serif;">
    <!beforeCreate
        setAttribute(key="contextRoot" value="#{request.contextPath}");
    />
    <!afterCreate
        setAttribute(key="popupId" value="$this{clientId}");
    />
    <f:verbatim>
        <div style="padding-left: 135px; font-size: 1.2em;">
		    <br />
		    <div class="please" >$resource{i18nc.reg.pleaseReg}</div>
		    <div id="why" class="why">
		        <a href="https://reg.sun.com/whyregister?program=sdn" target="_blank" >$resource{i18nc.reg.whyRegister}</a>
		    </div>
		    <div class="bulletList">
		        $resource{i18nc.reg.whyRegisterList}
		    </div>
	    </div>

		<div style="text-align: center;">
    </f:verbatim>
        <h:commandButton value="$resource{i18n.button.register}" styleClass="Btn1_sun4"
            onclick="document.getElementById('${popupId}').style.visibility='hidden'; admingui.ajax.loadPage({url:'#{request.contextPath}/common/sysnet/#{registrationNodePage}.jsf'}); return false;"
            onkeypress="javascript: return false;">
        </h:commandButton>
        <h:commandButton value="$resource{i18n.button.regRemind}" styleClass="Btn2_sun4"
            onclick="document.getElementById('${popupId}').style.visibility='hidden';" primary="#{false}"
            onkeypress="javascript: return true;">
            <!command
                setRegistrationRemindLater();
                setCookie(key="appServerRegistrationRemindLaterCookie" value="true" maxAge="1296000");
                if(! $session{supportCluster}){
                    gf.redirect("#{request.contextPath}/common/commonTask.jsf");
                }
                if($session{supportCluster}){
                    gf.redirect("clusterCommonTask.jsf");
                }
            />
        </h:commandButton>
        <h:commandButton value="$resource{i18n.button.regNever}" styleClass="Btn2_sun4" onclick="document.getElementById('${popupId}').style.visibility='hidden';" primary1="#{false}" onkeypress="javascript: return true;">
            <!command
                setRegistrationDontAsk();
                if(! $session{supportCluster}){
                    gf.redirect("#{request.contextPath}/common/commonTask.jsf");
                }
                if($session{supportCluster}){
                    gf.redirect("clusterCommonTask.jsf");
                }
            />
        </h:commandButton>
	<h:commandButton value="$resource{i18n.button.Cancel}" styleClass="Btn2_sun4" onclick="document.getElementById('${popupId}').style.visibility='hidden'; " primary1="#{false}"  onkeypress="javascript: return true;">
	    <!command
		setSessionAttribute(key="dontAskRegistrationInThisSession" value="true");
		if(! $session{supportCluster}){
                    gf.redirect("#{request.contextPath}/common/commonTask.jsf");
                }
                if($session{supportCluster}){
                    gf.redirect("clusterCommonTask.jsf");
                }
	    />
	    </h:commandButton>
	"</div>

    <sun:script>
	<f:verbatim>
	    function initReminderBox() {
		// Call the old onLoad (1 time)
//		window.oldRegOnload();
		window.oldRegOnload = function() {};

		// Center it
		var popup = document.getElementById('${popupId}');
                if (popup != null) {
                    var width = document.getElementById('content').offsetWidth;
                    popup.style.left = ((width - 423) / 2 + "px");

                    // Show it
                    popup.style.display = 'block';
                }
	    }
	    window.onresize = initReminderBox;
	    window.oldRegOnload = window.onload;
	    //window.onload = initReminderBox;
	    initReminderBox();
	</f:verbatim>
    </sun:script>
</sun:markup>

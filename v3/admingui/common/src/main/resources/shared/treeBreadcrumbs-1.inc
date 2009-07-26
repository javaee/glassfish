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

"<div id="breadcrumbs" style="border: 1px solid #E5E9ED; background-color: #E5E9ED">
<!-- Make sure we have the scripts loaded -->
<sun:script url="/resource/common/js/adminjsf.js" />
<!-- start the breadcrumbs -->
<sun:breadcrumbs id="boom" pages="#{pageSession.breadCrumbs}"style="background-color: #E5E9ED">
    <!beforeCreate
        dummyHyperlinkArray(links=>$pageSession{breadCrumbs});
    />
    <!afterCreate
	setPageSessionAttribute(key="boom" value="$this{clientId}");
        setPageSessionAttribute(key="hasPageURL", value="#{false}");
        if ("#{pageSession.pageURLForBreadCrumb}"){
            setPageSessionAttribute(key="pageURL", value="#{pageSession.pageURLForBreadCrumb}");
            setPageSessionAttribute(key="hasPageURL", value="#{true}");
        }
        if ("!#{pageSession.hasPageURL}"){
            setPageSessionAttribute(key="pageURL" value="#{facesContext.externalContext.request.queryString}");
            if ("#{pageSession.pageURL}") {
                setPageSessionAttribute(key="pageURL" value="#{facesContext.externalContext.requestServletPath}#{facesContext.externalContext.requestPathInfo}?#{pageSession.pageURL}");
            }
            if ("!#{pageSession.pageURL}") {
                setPageSessionAttribute(key="pageURL" value="#{facesContext.externalContext.requestServletPath}#{facesContext.externalContext.requestPathInfo}");
            }
        }
    />
</sun:breadcrumbs>

<!-- Make an invisible button to handle the Ajax action -->
<sun:button id="button" visible="#{false}">
    <!afterCreate
	setPageSessionAttribute(key="ajaxButton" value="$this{clientId}");
    />
    <!command
	createHyperlinkArray(links=>$pageSession{breadCrumbs});
    />
</sun:button>

<!-- Fire this during the onload of the page -->
<f:verbatim>
<script type="text/javascript">
    var myonload = new Object();
    myonload.oldonload = window.onload;
    myonload.newonload = function() {
	if ('#{pageSession.pageURL}' != '') {
	    admingui.nav.selectTreeNodeWithURL('#{pageSession.pageURL}');
	}
	admingui.nav.calculateBreadCrumbs(document.getElementById('#{pageSession.ajaxButton}'), '#{pageSession.boom}', 0);
	if (myonload.oldonload) {
	    myonload.oldonload();
	}
    };
    window.onload = myonload.newonload;
</script>
</f:verbatim>

"</div>

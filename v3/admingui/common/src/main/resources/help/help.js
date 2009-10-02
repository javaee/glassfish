/*
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 
 Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 
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
*/

if (typeof(admingui) === "undefined") {
    admingui = {};
}

admingui.help = {
    showHelpPage: function(url, targetNode) {
	if (targetNode) {
	    if (targetNode.toLowerCase) {
		// We have a String
		targetNode = document.getElementById(targetNode);
	    }
	}
	if (targetNode) {
	    var req = admingui.help.getXMLHttpRequestObject();
	    if (req) {
		req.onreadystatechange =
		    function() {
			if (req.readyState == 4) {
			    // Make a tempoary elemnt to contain the help content
			    var tmpDiv = document.createElement("div");
			    tmpDiv.innerHTML = req.responseText;

			    // Fix URLs in the help content...
			    admingui.help.fixHelpURLs(url, tmpDiv);

			    // Show the help content...
			    targetNode.innerHTML = tmpDiv.innerHTML;
			}
		    };
		req.open("GET", url, true);
		req.send("");
	    }
	}
    },

    fixHelpURLs: function(baseURL, node) {
	// Walk the DOM looking for "A" nodes, repair their URLs
	if ((node.nodeType == 1) && (node.nodeName == "A")) {
	    var relPath = node.getAttribute("href");
	    if (relPath) {
		node.href = "javascript:admingui.help.showHelpPage('"
		    + baseURL + "/../" + relPath + "', 'helpContent');";
	    }
	} else {
	    // Not a href, so walk its children
	    for (var idx=node.childNodes.length-1; idx>-1; idx--) {
		admingui.help.fixHelpURLs(baseURL, node.childNodes[idx]);
	    }
	}
    },

    getXMLHttpRequestObject: function() {
	var reqObj = null;
	if (window.XMLHttpRequest && !(window.ActiveXObject)) {
	    reqObj = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
	    try {
		reqObj = new ActiveXObject("Msxml2.XMLHTTP");
	    } catch (ex) {
		reqObj = new ActiveXObject("Microsoft.XMLHTTP");
	    }
	}
	return reqObj;
    }
}

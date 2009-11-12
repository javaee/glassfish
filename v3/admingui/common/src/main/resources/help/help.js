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
	    if (typeof(targetNode) === 'string') {
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

    fixTreeOnclick: function(node) {
	if ((node.nodeType == 1) && (node.nodeName == "A")) {
	    if (node.href) {
		node.oldonclick = null;
		if (node.onclick) {
		    node.oldonclick = node.onclick;
		}
		node.onclick = function () { if (this.oldonclick != null) { this.oldonclick(); } admingui.help.showHelpPage(this.href, 'helpContent'); return false; };
	    }
	} else {
	    // Not a href, so walk its children
	    for (var idx=node.childNodes.length-1; idx>-1; idx--) {
		admingui.help.fixTreeOnclick(node.childNodes[idx]);
	    }
	}
    },

    fixHelpURLs: function(baseURL, node) {
	// Walk the DOM looking for "A" nodes, repair their URLs
	if ((node.nodeType == 1) && (node.nodeName == "A")) {
	    var relPath = node.getAttribute("href");
	    if (relPath) {
		if (relPath.indexOf("#") == 0) {
		    // In-page link...
		    return;
		}
		if (relPath.indexOf("://") !== -1) {
		    // Full URL or IE7...
		    if (relPath.indexOf(window.location.href) == 0) {
			// Same Path...
			if (relPath.indexOf("#") == -1) {
			    // Not an in-page link, make it one...
			    node.href = "#";
			}

			// Nothing to do here...
			return;
		    }
		    var idx = relPath.indexOf("/common/help/");
		    if (idx != -1) {
			// IE7 does not give the real value, but instead tranlates it
			// all urls will be relative to "/common/help/" in this case,
			// so strip it off...
			relPath = relPath.substring(idx+13);
		    } else {
			if (relPath.indexOf(window.location.hostname) != -1) {
			    // From same host... Assume IE7 messed up URL
			    idx = relPath.indexOf('/', relPath.indexOf('://') + 3);
			    relPath = "../../../" + relPath.substring(idx+1);
			} else {
			    // Must be a real external URL...
			    if ((node.target == null)
				    || (node.target == "")
				    || (typeof(node.target) === "undefined")) {
				// Default external targets to _blank
				node.target = "_blank";
			    }
			    return;
			}
		    }
		    if ((idx = relPath.indexOf('#')) != -1) {
			// Remove '#' from IE Ajax URLs b/c IE can't handle it!!
			relPath = relPath.substring(0, idx);
		    }
		}
		// Take filename off baseURL
		baseURL = baseURL.substring(0, baseURL.lastIndexOf('/'));

		// Remove leading ../'s
		while (relPath.indexOf("../") != -1) {
		    relPath = relPath.substring(3);
		    var idx = baseURL.lastIndexOf("/");
		    if (idx != 0) {
			baseURL = baseURL.substring(0, idx);
		    }
		}

		// Fix href...
		node.href = baseURL + "/" + relPath;
		node.setAttribute("onclick", "admingui.help.showHelpPage('" + node.href + "', 'helpContent'); return false;");
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
	if (window.XMLHttpRequest) {
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
};

/*
 *  The following functions are utility functions.
 */
admingui.util = {
    /**
     *	This function finds the Woodstock node which has the getProps
     *	function and returns the requested property.  If it does not exist
     *	on the given object, it will look at the parent.
     */
    getWoodstockProp: function(node, propName) {
	if (node == null) {
	    return;
	}
	if (node.getProps != null) {
	    return node.getProps()[propName];
	}
	return admingui.util.getWoodstockProp(node.parentNode, propName);
    },

    /**
     *	This function finds an Array[] of nodes matching the (checkFunc),
     *	which is a JS function that takes two arguments: the HTML node object
     *	to check, and an optional "argument" (arg) that is passed through.
     */
    findNodes: function(node, checkFunc, arg) {
        var results = new Array();
        if (node == null) {
            return null;
        }

        // Check for match
        if (checkFunc(node, arg)) {
            results[results.length] = node;
        }

        // Not what we want, walk its children if any
        var nodeList = node.childNodes;
        if (nodeList && (nodeList.length > 0)) {
            var moreResults;

            // Look for more matches...
            for (var count = 0; count<nodeList.length; count++) {
                // Recurse
                moreResults = admingui.util.findNodes(nodeList[count], checkFunc, arg);
                if (moreResults) {
                    // Append the results
                    results = results.concat(moreResults);
                }
            }
        }

        // Make sure we found something...
        if (results.length == 0) {
            results = null;
        }

        // Return what we found (if anything)
        return results;
    },

    /**
     *	This function sets the <code>key</code> / <code>value</code> pair as
     *	a persistent preference in the <code>root</code> path.  The root path
     *	will automatically prefix "glassfish/" to the given String.
     */
    setPreference: function(root, key, value) {
	root = 'glassfish/' + root;
	admingui.ajax.invoke("setPreference", {root:root, key:key, value:value});
    },

    log : function(msg) {
        if (!(typeof(console) === 'undefined') && (typeof(console.log) === 'function')) {
            console.log((new Date()).toString() + ":  " + msg);
        }
    }
}

/*
 *  The following functions provide tree functionality.
 */
admingui.help.nav = {
    TREE_ID: "tocTree",
    lastTreeNodeSelected: null,
    
    /**
     *	This function selects a treeNode matching the given URL.
     */
    selectTreeNodeWithURL: function(url) {
        var tree = document.getElementById(admingui.help.nav.TREE_ID);
        var matches = admingui.util.findNodes(tree, admingui.help.nav.matchURL, url);
        if (matches) {
            // FIXME: Find "best" match... this will be needed if the URL
            // is ambiguous, which may happen if post requests occur which
            // leave off QUERY_STRING data that is needed to identify the
            // URL.  It's probably best to leave the highlighting alone in
            // many of these cases... perhaps search for the nearest match
            // to the currently selected node.  Anyway, for now I will
            // ignore this until we need to fix it...
	    // FIXME: This really should highlight the selected node.
            admingui.help.nav.selectTreeNode(document.getElementById(matches[0].id));
        } 
    },

    /**
     *	This function selects the given treeNode.
     */
    selectTreeNode: function(treeNode) {
        var tree = document.getElementById(admingui.help.nav.TREE_ID);// admingui.help.nav.getTree(treeNode);
        if (tree) {
            try {
                this.expandNode(treeNode);
            } catch (err) {
                //console.log(err);
            }
        }
    },

    expandNode: function(treeNode) {
        var id = treeNode.id;
        var index = id.lastIndexOf(":");
        while (index > -1) {
            id = id.substring(0, index);
	    var toSetStyle = document.getElementById(id+"_children");
	    if (toSetStyle) {
		toSetStyle.style.display = "block";
	    }
            index = id.lastIndexOf(":");
        }
    },

    /**=
     *	This function selects the given treeNode.
     */
    selectTreeNodeById: function(treeNodeId) {
        var tree = document.getElementById(admingui.help.nav.TREE_ID);
        //admingui.help.nav.getTreeFrameElementById(treeNodeId));
        if (tree) {
            tree.selectTreeNode(treeNodeId);
        }
    },

    /**
     *	This function looks for an "A" node with a url equal to the url
     *	passed in.
     */
    matchURL: function(node, url) {
        var result = null;
        if ((node.nodeType == 1) && (node.nodeName == "A") && 
            (node.href.indexOf(url) > -1) & (node.id.indexOf("link") > -1)) {
            result = node;
        }
        return result;
    },

    /**
     *	This function attempts to obtain the tree frame's tree object and
     *	return its selected Tree node.  It will return null if unable to do
     *	this.  It will <b>not</b> wait for the tree frame to load if it is not
     *	already loaded.
     */
    getSelectedTreeNode: function() {
        var tree = document.getElementById(admingui.help.nav.TREE_ID);
        if (tree && tree.getSelectedTreeNode) {
            return tree.getSelectedTreeNode(tree.id);
        }
    },

    /**
     *	This function provides access to DOM objects in the tree window.
     */
    getTreeFrameElementById: function(id) {
	return document.getElementById(id);
    },

    /**
     *	This function returns the parent TreeNode for the given TreeNode.
     */
    getParentTreeNode: function(treeNode) {
        return document.getElementById(admingui.help.nav.TREE_ID).getParentTreeNode(treeNode);
    },

    getContainingTreeNode: function(href) {
        var node =  document.getElementById(admingui.help.nav.TREE_ID).findContainingTreeNode(href);
        return node;
    },

    getTree: function(treeNode) {
        if (treeNode) {
            var node = document.getElementById(admingui.help.nav.TREE_ID);
            return node.getTree(treeNode);
        }
        return null;
    }
};

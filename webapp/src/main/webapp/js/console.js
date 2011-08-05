/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU 
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You 
 * may not use this file except in compliance with the License.  You can 
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL 
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
if (typeof Console == 'undefined') {
    Console = { };
    $(document).ready(function () { Console.Ajax.processLinks() });
}

Console.Ajax = {
    processLinks: function() {
        $('#ajaxBody > a').click(function() {
            var href = $(this).prop('href');
            console.debug('Modifying ' + $(this).prop('id'));
            if (href != '') {
                Console.Ajax.loadPage(href);
            }

            return false;
        });
    },
    
    loadPage: function (url) {
        document.getElementById('content').value = url;
        document.getElementById('contentButton').click();
        return false;
    },
    
    ajaxCallback: function(data) {
        if (data.status === 'success') {
//            alert('success!');
//            var context = {};
//            Console.Ajax.processElement(context, AUI().one("#ajaxBody"), true);
//            Console.Ajax.processScripts(context);
        } else if (data.status === 'error') {
            alert('error');
            Console.Ajax.loadPage('/domain.xhtml');
        }
    },
    
    processElement : function (context, node, queueScripts) {
        var recurse = true;
        if (node.nodeName == 'A') {
            /*
            // FIXME: For exteral URLs, we should not replace... however, we
            // FIXME: may want to ensure they have a _blank target.  May need
            // FIXME: to compare the host to see if a URL is an external URL
            // FIXME: b/c M$ makes it hard to determine relative URLs, and full
            // FIXME: URLs to the same host "might" want be valid for
            // FIXME: replacement.
            if (!admingui.ajax._isTreeNodeControl(node) && (node.target == '')) { //  && (typeof node.onclick != 'function'))
                var shouldReplace = true;
                if ((typeof node.onclick == 'function') && (node.id.indexOf("treeForm:tree") == -1)) {
                    //admingui.util.log("*NOT* replacing href for " + node.id);
                    shouldReplace = false;
                }
                if (shouldReplace) {
                    var url = node.href;
                    //node.href = "#";
                    var oldOnClick = node.onclick;
                    node.onclick = function() {
                        admingui.ajax.loadPage({
                            url : url,
                            target: document.getElementById('content'),
                            oldOnClickHandler: oldOnClick,
                            sourceNode: node
                        });
                        return false;
                    };
                }
            }
            */
        } else if (node.nodeName == 'IFRAME') {
            recurse = false;
        } else if (node.nodeName == 'INPUT') {
            /*
            if (((node.type == 'submit') || (node.type == 'image'))
                && ((node.onclick === null) || (typeof(node.onclick) === 'undefined') || (node.onclick == ''))) {
                // Submit button w/o any JS, make it a partial page submit
                node.onclick = function() {
                    var args = {};
                    args[node.id] = node.id;
                    admingui.ajax.postAjaxRequest(this, args);
                    return false;
                };
            }
            */
        /*
        } else if (node.nodeName == 'FORM') {
            admingui.util.log("***** form action:  " + node.action);
            if (node.target == '') {
                node.onsubmit = function () {
                    admingui.ajax.submitFormAjax(node);
                    return false;
                };
            }
	    */
        } else if (node.nodeName == 'TITLE') {
            // bareLayout.xhtml handles this for ajax requests...
            recurse = false;
        } else if (node.nodeName == 'SCRIPT') {
            recurse = false;  // don't walk scripts
            if (queueScripts) {
                // Queue it...
                if (typeof(context.scriptQueue) === "undefined") {
                    context.scriptQueue = new Array();
                }
                context.scriptQueue.push(node);
            }
        }

        // If recurse flag is true... recurse
        if (recurse && node.childNodes) {
            for (var i = 0; i < node.childNodes.length; i++) {
                admingui.ajax.processElement(context, node.childNodes[i], queueScripts);
            }
        }
    },

    _isTreeNodeControl : function (node) {
        return isTreeNodeControl = (node.id.indexOf("_turner") > -1); // probably needs some work.  This will do for now.
    },

    processScripts : function(context) {
        if (typeof(context.scriptQueue) === "undefined") {
            // Nothing to do...
            return;
        }
        globalEvalNextScript(context.scriptQueue);
    }
}

var globalEvalNextScript = function(scriptQueue) {
    if (typeof(scriptQueue) === "undefined") {
        // Nothing to do...
        return;
    }
    var node = scriptQueue.shift();
    if (typeof(node) == 'undefined') {
        // Nothing to do...
        return;
    }
    if (node.src === "") {
        // use text...
        globalEval(node.text);
        globalEvalNextScript(scriptQueue);
    } else {
        // Get via Ajax
        admingui.ajax.getResource(node.src, function(result) {
            globalEval(result.content);
            globalEvalNextScript(scriptQueue);
        } );
    // This gets a relative URL vs. a full URL with http://... needed
    // when we properly serve resources w/ rlubke's recent fix that
    // will be integrated soon.  We need to handle the response
    // differently also.
    //admingui.ajax.getResource(node.attributes['src'].value, function(result) { globalEval(result.content); globalEvalNextScript(scriptQueue);} );
    }
}

var globalEval = function(src) {
    if (window.execScript) {
        try {
            window.execScript(src);
        } catch (error) {
            if (console && console.log) {
                console.log(error);
            }
        }
        return;
    }
    var fn = function() {
        window.eval.call(window, src);
    };
    fn();
};

/*
if (!Array.prototype.forEach) {
    Array.prototype.forEach = function(fun )
    {
        var len = this.length;
        if (typeof fun != "function")
            throw new TypeError();

        var thisp = arguments[1];
        for (var i = 0; i < len; i++)
        {
            if (i in this)
                fun.call(thisp, this[i], i, this);
        }
    };
}

if (!HTMLCollection.prototype.forEach) {
    HTMLCollection.prototype.forEach = function(fun )
    {
        var len = this.length;
        if (typeof fun != "function")
            throw new TypeError();

        var thisp = arguments[1];
        for (var i = 0; i < len; i++)
        {
            if (i in this)
                fun.call(thisp, this[i], i, this);
        }
    };
}
*/

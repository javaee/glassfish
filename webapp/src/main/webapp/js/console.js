if (typeof Console == 'undefined') {
    Console = { };
}

if (!Array.prototype.forEach) {
    Array.prototype.forEach = function(fun /*, thisp*/)
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
    HTMLCollection.prototype.forEach = function(fun /*, thisp*/)
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

Console.Ajax = {
    processLinks: function() {
        AUI().all('a').each(function (instance, index, nodeList) {
            var href = instance.getAttribute('href');
            
            instance.setAttribute('href', 'javascript:');
            if (href != "") {
                AUI().on('click', function() {Console.Ajax.loadPage(href);return false;}, instance);
            }
        });
       /*
        var anchors = document.getElementsByTagName("a");
        
        anchors.forEach(
            function(el, index, array) {
                var href = el.href;
                var oldOnClick = el.onclick;
//                el.href = "#";
                el.onclick = function() {
                    alert(href);
                    if (typeof oldOnClick != 'undefined') {
                        oldOnClick();
                    }
                }
            }
            );
       */
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


Console.UI = {
    tabs: [ ],
    
    createTabs: function(selector) {
        /*
        $(selector).prepend('<ul/>');
        $(selector + ' > div').each(function (index, el) {
            $(selector + ' > ul').append('<li><a href="#' + el.id + '">' + el.title + '</a></li>');
        });
        $(selector).tabs();
         */
        //                var ul = A.one(selector + '_ul');
        //                var tabs = A.one(selector + '_div');
        //                tabs.get('children').each(function(el, obj) { 
        //                    if (el.get('tagName') == 'DIV') {
        //                        ul.append('<li><a href="#' + el.get('id') + '">' + el.get('title') + '</a></li>');
        //                    }
        ////                    console.debug(el);
        //                });
        //                var tabView = 
        
        AUI().use('node', function (A) {        
            if (Console.UI.tabs.length == 0) {
                A.on('domready', Console.UI.renderTabs)
            }
                
            Console.UI.tabs.push(selector);
        });
    },
    
    renderTabs: function() {
        AUI().use(
            'tabview',
            function(A) {
                for (var i = Console.UI.tabs.length -1 ; i>= 0; i--) {
                    new A.TabView({
                        srcNode: Console.UI.tabs[i]
                    }).render();
                    A.one(Console.UI.tabs[i]).setStyle('visibility', 'visible');
                }
        
                Console.UI.tabs = [];
            });
    },
    
    createTree: function(selector) {
        /*
        selector = selector + ' ul' + selector + '_top > li';
        var nodes = {
            identifier: 'id', 
            label: 'value',
            items: [ ]
        };
        
        this.processTreeNodes(dojo.query(selector)).forEach(
            function(el, index, array) {
                nodes.items.push(el); 
            });
        console.debug(nodes.items);
         */
        
        /*
        AUI().ready('aui-tree', 'substitute', 'node', function(A) {
            var treeView = new A.TreeView({
                boundingBox: selector
                ,
                contentBox: selector+'_ul'
            }
            )
//            .render()
            ;
            A.one(selector).setStyle('visibility', 'visible');
        });
        */
       $(selector).jstree({ "plugins" : [ "themes", "html_data" ] });
    },
    
    processTreeNodes: function (nodeList) {
        var nodes = [ ];
        var o = this;
        nodeList.forEach(function(node, index, arr){
            var anchor = dojo.query('#' + node.id + '_a')[0];
            var selector = '#' + node.id;
            var item =  {
                id: node.id,
                link: anchor.href,
                value: anchor.innerHTML,
                children: o.processTreeNodes(dojo.query(selector + ' ul' + selector + '_ul > li'))
            };
            
            nodes.push(item);
        });
        
        return nodes;
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

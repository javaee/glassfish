/*
 * Common utility
 */

/* To work around a timing issue where for Firefox 2.0.0.3 on Mac OS X
 * We need to put in a little delay before returning the var
 */
function getConfirm(theButton, msg){
    var oldOnFocus = theButton.onfocus;
    theButton.onfocus = "";
    var val=confirm(msg);
    theButton.onfocus = oldOnFocus;
    return val;
}

function showAlert(msg) {
    setTimeout("alert('" + msg + "')", 100);
    return false;
}


function submitAndDisable(button, msg, target) {
    button.className="Btn2Dis_sun4"; // the LH styleClass for disabled buttons.
    button.disabled=true;
    var oldaction = button.form.action;
    var sep = (button.form.action.indexOf("?") > -1) ? "&" : "?";
    button.form.action += sep + button.name + "=" + encodeURI(button.value); //bug# 6294035
    button.value=msg;
    if (target) {
	var oldtarget = button.form.target;
	button.form.target = target;
	if (target === "_top") {
	    // In this case we want the non-ajax behavior
	    button.form.submit();
	    button.form.target = oldtarget;
	    return false;
	}
    }
    admingui.ajax.submitFormAjax(button.form);
    button.form.action = oldaction;
    return false; 
}


function disableButton(id) {
    var button = document.getElementById(id);
    button.className='Btn1Dis_sun4'; // the LH styleClass for disabled buttons.
    button.disabled=true;
}

//To disable all buttons in the page.
//TODO: other components maybe of type "submit" even though it is not a button, need to fix this.
function disableAllButtons() {
    var inputs = document.getElementsByTagName("input");
    for ( i=0; i < inputs.length; i++) {
        component = inputs[i];
        if (component.type == "submit"){
            component.disabled=true; 
        }
    }
}

function getField(theForm, fieldName) {
    for (i=0; i < theForm.elements.length; i++) {
        var value = theForm.elements[i].name;
        if (value == null) {
            continue;
        }
        var pos = value.lastIndexOf(':');
        var helpKeyFieldName = value.substring(pos+1);
        if (helpKeyFieldName == fieldName) {
            return theForm.elements[i];
        }
    }
    return null;
}

// FIXME: suntheme should not be used -- prevents theme from changing
function getTextElement(componentName) {
    var el = webui.suntheme.field.getInputElement(componentName);
    if (el == null) {
        el = document.getElementById(componentName); // This may get too deep inside WS, but it should work as a fall back
    }
    return el;
}

function getSelectElement(componentName) {
    return webui.suntheme.dropDown.getSelectElement(componentName);
}

function getFileInputElement(componentName) {
    var el = webui.suntheme.upload.getInputElement(componentName);
    if (el == null) {
        el = document.getElementById(componentName+"_com.sun.webui.jsf.upload");
    }

    return el;
}

function disableComponent(componentName, type) {
    var component = null;
    if (type != null && type == 'file') {
        component = getFileInputElement(componentName);
    } else if(type != null && type == 'select') {
        component = getSelectElement(componentName);
    } else {
        component = getTextElement(componentName);
	if (component != null) {
	    component.value='';
	}
    }
    if (component != null) {
	if (typeof(component.setDisabled) === 'function') {
	    component.setDisabled(true);
	} else {
	    component.disabled=true;
	    component.className='TxtFldDis_sun4';
	}
    }
}


/*
 * was trying to see if we can set the timeout in the function itself, instead of
 * at the calling time, refer to update.jsf
 * but just can't get this working.
 * saving the code for now.
 
function delayDisableComponent(componentName, type, timeouted) {
    var func = disableComponent[type] || getTextElement;
    var component = func(componentName);
    if(component == null && !timeouted) {
    	window.setTimeout("disableComponent('" + componentName + "','" + type + "', true)", 10);
    }
    if (component == null){
        window.console.log('component is NULL' + componentName);
        window.console.debug('component is NULL' + componentName);
    }
    
    component.disabled = true;
    component.className='TxtFldDis_sun4';
    if(func == getTextElement) {
    	component.value = "";
    }
}
disableComponent.file = getFileInputElement;
disableComponent.select = getSelectElement;
*/


function disableBtnComponent(componentName) {
    var el = document.getElementById(componentName);
    if (typeof(el.setDisabled) === 'function') {
	el.setDisabled(true);
    } else if (el.setProps) {
	document.getElementById(componentName).setProps({disabled: true, className: 'Btn1Dis_sun4'});
    } else {
	el.disabled = true;
	el.className = 'Btn1Dis_sun4'; // Primary style
    }
}

function enableBtnComponent(componentName) {
    var el = document.getElementById(componentName);
    if (typeof(el.setDisabled) === 'function') {
	el.setDisabled(false);
    } else if (el.setProps) {
        document.getElementById(componentName).setProps({disabled: false, className: 'Btn1_sun4'});
    } else {
        el.disabled = false;
        el.className = 'Btn1_sun4';  // Primary style
    }
}

function enableComponent(componentName, type) {
    var component = null;
    if (type != null && type == 'file') {
        component = getFileInputElement(componentName);
    } else if(type != null && type == 'select') {
        component = getSelectElement(componentName);
    } else {
        component = getTextElement(componentName);
    }
    if (typeof(component.setDisabled) === 'function') {
	component.setDisabled(false);
    } else {
	component.className='TxtFld_sun4';
	component.disabled=false;
    }
}

function disableDOMComponent(componentName) {
    var el = document.getElementById(componentName);
    if (typeof(el.setDisabled) === 'function') {
	component.setDisabled(true);
    } else if (el.setProps) {
        document.getElementById(componentName).setProps({disabled: true, className: 'TxtFldDis_sun4', value: ' '});
    } else {
        //YAHOO.util.Dom.setStyle(el, 'disabled', 'true');
        el.disabled = true;
        el.className = 'TxtFldDis_sun4';
        el.value = ' ';
    }
}

function enableDOMComponent(componentName) {
    var el = document.getElementById(componentName);
    if (el.setProps) {
        document.getElementById(componentName).setProps({disabled: false, className: 'TxtFld_sun4'});
    } else {
        //YAHOO.util.Dom.setStyle(el, 'disabled', 'false');
        el.disabled = false;
        el.className = 'TxtFld_sun4';
    }
}

function isChecked (elementName) {
    var element = document.getElementById (elementName);
    if (element != null) {
        if (element.checked) {
            return true;
        } else {
            return false;
        }
    }
    return false;
}

function checkForValue(formField) { 
    if (!formField) {
	return false; // No field, so no value
    }
    var value = formField.value;
    if (formField.getProps) {
	// Use Woodstock's api to get correct value
	value = formField.getProps().value;
    }
    var result = (value != '') && (isWhitespace(value) == false); 
    if (!result) {
	formField.select();
    }
    return result; 
}

//==========================================================
// Set a cookie

function setCookie(c_name,value,expiredays)
{
    //alert( c_name + ',' + value + ',' + expiredays);
    var exdate=new Date()
    exdate.setDate(exdate.getDate()+expiredays)
    document.cookie=c_name+ "=" +escape(value)+((expiredays==null) ? "" : ";expires="+exdate.toGMTString())
}

function getCookie(name) {
    var cookies = document.cookie.split(";");
    var cookieValue = null;

    for (var i = 0; i < cookies.length; i++) {
        var current = cookies[i].split("=");
	var currentName = current[0];
	if (typeof(current[0].trim) === 'function') {
	    currentName = currentName.trim();
	}
        if (name == currentName) {
            if (current.length > 1) {
                cookieValue = unescape(current[1]);
                break;
            }
        }
    }

    return cookieValue;
}


//===========================================================

/** 
 * Relating to Deployment
 */

function setVisible(type) {
    var fixedValue = 'form:title:ps:psec:';
    if (type == "webApp") {
        var fields = new Array();
        fields[0] = fixedValue.concat("wsp");
        fields[1] = "form:title:ps:advancedSection";
        fields[2] = fixedValue.concat("threadpoolProp");
        fields[3] = fixedValue.concat("registryProp");
        setVisibleFields(fields, false);
        fields = new Array();
        fields[0] = fixedValue.concat("cxp");
        fields[1] = fixedValue.concat("vsp");
        fields[2] = fixedValue.concat("precmplProp");
        fields[3] = fixedValue.concat("librariesProp");
        fields[4] = fixedValue.concat("enableProp");
        setVisibleFields(fields, true);
        setTargetSection(true);
        setHaProp(fixedValue.concat("haProp"), true);
    }
    else if (type == "application") {
        var fields = new Array();
        fields[0] = fixedValue.concat("cxp");
        fields[1] = fixedValue.concat("threadpoolProp");
        fields[2] = fixedValue.concat("registryProp");
        setVisibleFields(fields, false);
        var fields = new Array();
        fields[0] = fixedValue.concat("vsp");
        fields[1] = fixedValue.concat("wsp");
        fields[2] = fixedValue.concat("precmplProp");
        fields[3] = "form:title:ps:advancedSection";
        fields[4] = fixedValue.concat("librariesProp");
        fields[5] = fixedValue.concat("enableProp");
        setVisibleFields(fields, true);
        setTargetSection(true);
        setHaProp(fixedValue.concat("haProp"),true);
    }
    else if (type == "ejbModule") {
        webui.suntheme.common.setVisible("form:title:ps:advancedSection", true);
        webui.suntheme.common.setVisible(fixedValue.concat("librariesProp"), true);
        webui.suntheme.common.setVisible(fixedValue.concat("enableProp"), true);
        var fields = new Array();
        fields[0] = fixedValue.concat("wsp");
        fields[1] = fixedValue.concat("cxp");
        fields[2] = fixedValue.concat("vsp");
        fields[3] = fixedValue.concat("precmplProp");
        fields[4] = fixedValue.concat("threadpoolProp");
        fields[5] = fixedValue.concat("registryProp");
        setVisibleFields(fields, false);
        setTargetSection(true);
        setHaProp(fixedValue.concat("haProp"),true);
    }
    else if (type == "appclient") {
        webui.suntheme.common.setVisible(fixedValue.concat("wsp"), true);
        webui.suntheme.common.setVisible("form:ps:advancedSection", true);

        var fields = new Array();
        fields[0] = fixedValue.concat("librariesProp");
        fields[1] = fixedValue.concat("enableProp");
        fields[2] = fixedValue.concat("precmplProp");
        fields[3] = fixedValue.concat("cxp");
        fields[4] = fixedValue.concat("vsp");
        fields[5] = fixedValue.concat("threadpoolProp");
        fields[6] = fixedValue.concat("registryProp");
        setVisibleFields(fields, false);
        setTargetSection(false);
        setHaProp(fixedValue.concat("haProp"),false);
    }
    else if (type == "connector") {
        webui.suntheme.common.setVisible("form:title:ps:advancedSection", false);
        var fields = new Array();
        fields[0] = fixedValue.concat("precmplProp");
        fields[1] = fixedValue.concat("cxp");
        fields[2] = fixedValue.concat("vsp");
        fields[3] = fixedValue.concat("wsp");
        fields[4] = fixedValue.concat("librariesProp");
        fields[5] = fixedValue.concat("targetSectionId");
        setVisibleFields(fields, false);

        fields = new Array();
        fields[0] = fixedValue.concat("threadpoolProp");
        fields[1] = fixedValue.concat("registryProp");
        setVisibleFields(fields, true);
        setTargetSection(true);
        setHaProp(fixedValue.concat("haProp"), false);
    }
}

function setVisibleFields(fields, value) {
    for (ctr=0; ctr < fields.length; ctr++) {
        webui.suntheme.common.setVisible(fields[ctr], value);
    }
}


function checkExtension(appType, extensionId, msg, reqdMsg){
    var extension = getTextElement(extensionId).value;
    //alert(appType + ',' + extension + ',' +  msg);
    if (extension.length <= 0) {
        return true;
    }
    if (((appType == "webApp") && (extension != ".war")) ||
        ((appType == "application") && (extension != ".ear")) ||
        ((appType == "ejbModule") && (extension != ".jar")) ||
        ((appType == "appclient") && (extension != ".jar")) ||
        ((appType == "connector") && (extension != ".rar")) ) {
        return showAlert(msg);
    }
    else
        return true;

}


function setTargetSection(value){
    var id = "form:title:ps:targetSectionId";
    component = document.getElementById(id);
    if (component != null)
        webui.suntheme.common.setVisible(id, value);
}

function setHaProp(id, value){
    var component = document.getElementById(id);
    if (component != null) {
        webui.suntheme.common.setVisible(id, value);
    }
}

function checkRedeployRequired(form, reqdMesg) {
    var uploadField = getField(form, "fileupload_com.sun.webui.jsf.upload");
    var dirField = getField(form, "dirPath_field");
    if (uploadField.value=='' && dirField.value=='') {
        return showAlert(reqdMesg);
    }
    return true;
}

function extractName(value) {
    var appName="";
    var len=-1;
    if ((len = value.lastIndexOf('/')) != -1) {
        appName = value.substring(len+1, value.length);
    }
    else {
        //For window platform, use backsplash
        len = value.lastIndexOf('\\');
        appName = value.substring(len+1, value.length);
    }
    return appName;
}


function getPrefix(fullName){
    index = fullName.lastIndexOf(".");
    if (index == -1)
        return fullName;
    else
        return fullName.substring(0, index);
}

function getSuffix(fullName){
    index = fullName.lastIndexOf(".");
    if (index == -1)
        return "";
    else
        return fullName.substring(index, fullName.length);
}

function setFieldValue(appNameId, value,  typeId, contextRootId, extensionId) {
    var appName = extractName(value);
    var pfex = getPrefix(appName);
    var sfex = getSuffix(appName);

    var component = getTextElement(extensionId);
    component.value=sfex;

    if (appNameId==null || appNameId.length <=0)
        return;
    component = getTextElement(appNameId);
    component.value=pfex

    //TODO: v3 may need to adjust for other supported type.
//    component = getSelectElement(typeId);
//    if (component.value == "webApp") {
//        component = getTextElement(contextRootId);
//        component.value = pfex
//    }
    if ( sfex == ".war"){
        component = getTextElement(contextRootId);
        if (component != null){
            component.value = pfex;
        }
    }
}

function populateDirAndAppName(fileChooserId, dirPathId, appNameId, typeId, ctxRootId, extensionId){ 
    var fc = document.getElementById(fileChooserId).getSelectionValue();
    window.opener.getTextElement(dirPathId).value = fc;

    var appName = extractName(fc);
    if (appNameId.length > 0) {
        window.opener.getTextElement(appNameId).value=getPrefix(appName);
    }
    if (extensionId.length > 0) {
        window.opener.getTextElement(extensionId).value=getSuffix(appName);
    }

//    if (typeId.length > 0) {
//        type = window.opener.getSelectElement(typeId).value;
//        if (type == "webApp") {
//            component = window.opener.getTextElement(ctxRootId);
//            component.value = getPrefix(appName);
//        }
//    }

    //TODO V3: may need to adjust other type.
    if (getSuffix(appName) == ".war"){
        component = window.opener.getTextElement(ctxRootId);
        if (component != null){
            component.value = getPrefix(appName);
        }
    }
    window.close();
}

function populatorDirAndAppName(fileChooserId, txtFld2Id){ 
    var fc = document.getElementById(fileChooserId).getSelectionValue();
 	window.opener.getTextElement(txtFld2Id).value = fc;
    window.close();
 }


function checkType(theButton, typeId, extensionId, msg){

    var appType = getSelectElement(typeId).value;
    var extension = getTextElement(extensionId).value;

    if (extension.length <= 0)
        return true;
    if (((appType == "webApp") && (extension != ".war")) ||
        ((appType == "application") && (extension != ".ear")) ||
        ((appType == "ejbModule") && (extension != ".jar")) ||
        ((appType == "appclient") && (extension != ".jar")) ||
        ((appType == "connector") && (extension != ".rar")) ) {
        return getConfirm(theButton, msg);
    }
    else
        return true;

}

// End of Deployment code

//===========================================================================

function findFrameRecursive( winOrFrame, frameName ) {
    // 1. CHECK THIS FRAME (winOrFrame)
    // home string is being checked to take care of the complex
    // frameset in PE homepage. Need to fix this in a Generic way later.
    if ( (winOrFrame.name && (winOrFrame.name == frameName)) ||
         winOrFrame.name == "home" )
        return winOrFrame;

    // 2. SEARCH SUBFRAMES.  note: when there are no sub-frames,
    // the frames array has 1 entry which is this frame,
    // hense this check for 2+ subframes
    if ( winOrFrame.frames.length < 2 )
        return null;

    // recurse
    for ( i= 0 ; i < winOrFrame.frames.length ; i++ ) {
        var x= findFrameRecursive( winOrFrame.frames[i], frameName );
        if ( x )
            return x;
    }
    return null;
}

function findFrame(frameName) {
    return findFrameRecursive(top, frameName);
}

var reasonsHidden = true;

function showRestartReasons() {
    var el = document.getElementById('restartReasons');
    var toggle = document.getElementById('toggle');
    if (reasonsHidden) {
        //toggle.src = "#{request.contextPath}/theme/woodstock4_3/suntheme/images/table/grouprow_expanded.gif";
        toggle.className = "expanded";
        el.style.visibility = "visible";
    } else {
        //toggle.src = "#{request.contextPath}/theme/woodstock4_3/suntheme/images/table/grouprow_collapsed.gif";
        toggle.className = "collapsed";
        el.style.visibility = "hidden";
    }   
    reasonsHidden = !reasonsHidden;
}   

//===========================================================================

if (typeof(admingui) === "undefined") {
    admingui = {};
}

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
admingui.nav = {
    TREE_ID: "treeForm:tree",
    lastTreeNodeSelected: null,
    
    refreshCluster: function(hasCluster){
        var node1 = admingui.nav.getTreeFrameElementById(admingui.nav.TREE_ID + ':clusters');
        var node2 = admingui.nav.getTreeFrameElementById(admingui.nav.TREE_ID + ':clusters2');
        var node3 = admingui.nav.getTreeFrameElementById(admingui.nav.TREE_ID + ':clusters2_children');
        var tree = admingui.nav.getTreeFrameElementById(admingui.nav.TREE_ID);
	// FIXME: This needs the viewId where clusters2 is defined
        admingui.nav.refreshTree(admingui.nav.TREE_ID + ':clusters2');
        if (hasCluster=='true' || hasCluster=='TRUE') {
            node1.style.display='none';
            node2.style.display='block';
            node3.style.display='block';
            tree.selectTreeNode(admingui.nav.TREE_ID + ':clusters2');
        } else {
	    //there is a problem in hiding clusters2,  it doesn' hide it, maybe because of the 
	    //dynamic treenode under it ? still need to figure this out.
            node3.style.display='none';
            node2.style.display='none';
            node1.style.display='block';
            tree.selectTreeNode(admingui.nav.TREE_ID + ':clusters');
        }
    },
    
    /**
     *	<p> This function allows you to provide a clientId of a TreeNode in the
     *	    navigation frame to be "refreshed".  This means that it and its
     *	    children will be deleted, recreated, and redisplayed.</p>
     *	<dl>
     *      <dt><b>Parameters:</b></dt>
     *      <dd>
     *          <code>refreshNodeId</code> - The clientId of the tree node to refresh
     *      </dd>
     *      <dd>
     *          <code>viewId</code> - The ID of the view in which the node is defined. This
     *          is not the same as the JSF view ID. Rather, it is, more or less, the name of
     *          the file on disk.  For example, for the applications page, the view Id would be
     *          '/common/peTree.inc'.  The view ID, then, follows this pattern:
     *          <code>/pluginId/&lt;path/relative/to/src/main/resources/fileName.ext&gt;</code>
     *      </dd>
     *      <dd>
     *          <code>relId</code> - The quasi-clientId of the component, relative to the root
     *          of the <code>viewId</code>.  This ID does <strong>not</strong> include <em>any</em>
     *          <code>NamingContainer</code>s that might be found in the final page.  The ID includes
     *          <em>only</em> those <code>NamingContainer</code>s defined in the page specified
     *          by the <code>viewId</code>.
     *      </dd>
     *  </dl>
     */
    refreshTree: function(refreshNodeId, viewId, relId) {
        admingui.util.log("Updating tree node " + refreshNodeId);
        if (!viewId) {
            // Supply best guess defaults...
            viewId = '/common/peTree.inc';
        }
        var refreshNode = null;
        if (refreshNodeId) {
            refreshNode = admingui.nav.getTreeFrameElementById(refreshNodeId);
            if (!refreshNode) {
                admingui.util.log('refreshNode not found:'+refreshNode);
            }
        } else {
            refreshNode = admingui.nav.getSelectedTreeNode();
            refreshNodeId = refreshNode.id;
        }
        if (!relId) {
            // Supply best guess defaults...
            relId = refreshNodeId;
        }
        var updateTreeButton = document.getElementById('treeForm:update');
        if (refreshNode && updateTreeButton) {
            admingui.nav.requestTreeUpdate(
                updateTreeButton,
                {type: 'click'},
                refreshNodeId,
                "updateTreeNode="+refreshNodeId+"&viewId="+viewId+"&relId="+relId,
                {
                    mainNode: document.getElementById(refreshNodeId),
                    childNodes: document.getElementById(refreshNodeId+"_children")
                }
            );
        }
        return false;
    },

    requestTreeUpdate: function(source, event, nodeId, params, previousState) {
            jsf.ajax.request(source, event, {
                execute: "treeForm treeForm:update",
                render: nodeId + " " + nodeId + "_children",
                onevent: function(data) {
                    admingui.nav.processUpdatedTreeNode(data, nodeId, previousState);
                },
                params: 'treeForm:update=&' + params
            });
    },

    processUpdatedTreeNode: function(data, nodeId, previousState) {
        if (data.status == 'success') {
            var text = data.responseXML.childNodes[0].childNodes[0].childNodes[0].childNodes[0].data;
            var parserElement = document.createElement('div');
            parserElement.innerHTML = text;
            var mainNode = document.getElementById(nodeId);
            var childNodes = document.getElementById(nodeId+"_children");

            mainNode.innerHTML = parserElement.childNodes[0].innerHTML;

            try {
                var oldNode = previousState.mainNode;
                mainNode.className = oldNode.className;
                mainNode.style["display"] = oldNode.style["display"];
                try {
                    // Copy image src value to correct visual state of the node turner
                    mainNode.childNodes[0].childNodes[0].childNodes[0].src = oldNode.childNodes[0].childNodes[0].childNodes[0].src;
                }catch (err1) {
                    
                }

                if (childNodes) {
                    childNodes.innerHTML = parserElement.childNodes[1].innerHTML;
                    oldNode = previouesState.childNodes;
                    childNodes.className = oldNode.className;
                    childNodes.style["display"] = oldNode.style["display"];
                    admingui.nav.copyStyleAndClass(childNodes, oldNode);
                }
            } catch (err) {

            }
            
            admingui.ajax.processElement(window, document.getElementById(nodeId), true);
            admingui.ajax.processElement(window, document.getElementById(nodeId+"_children"), true);
        }
    },

    /**
     *
     */
    copyStyleAndClass: function(src, dest) {
        if (!src || !dest || !src.childNodes || !dest.childNodes) {
            return;
        }
        var name = null;
        for (var idx=0; idx<src.childNodes.length; idx++) {
            name = src.childNodes[idx].id;
            if (name) {
                for (var cnt=0; cnt<dest.childNodes.length; cnt++) {
                    if (name == dest.childNodes[cnt].id) {
                        dest.childNodes[cnt].style["display"] = src.childNodes[idx].style["display"];
                        dest.childNodes[cnt].className = src.childNodes[idx].className;
                        admingui.nav.copyStyleAndClass(src.childNodes[idx], dest.childNodes[cnt]);
                    }
                }
            }
        }
    },

    /**
     *	This function clears all treeNode selections.
     */
    clearTreeSelection: function(treeId) {
        var tree = admingui.nav.getTreeFrameElementById(treeId);
        if (tree) {
            tree.clearAllHighlight(treeId);
        }
    },

    /**
     *	This function selects a treeNode matching the given URL.
     */
    selectTreeNodeWithURL: function(url) {
        var tree = document.getElementById(admingui.nav.TREE_ID);
        var matches = admingui.util.findNodes(tree, admingui.nav.matchURL, url);
        if (matches) {
            // FIXME: Find "best" match... this will be needed if the URL
            // is ambiguous, which may happen if post requests occur which
            // leave off QUERY_STRING data that is needed to identify the
            // URL.  It's probably best to leave the highlighting alone in
            // many of these cases... perhaps search for the nearest match
            // to the currently selected node.  Anyway, for now I will
            // ignore this until we need to fix it...
            admingui.nav.selectTreeNode(admingui.nav.getContainingTreeNode(matches[0]));
        } else {
            admingui.nav.selectTreeNode(document.getElementById(getCookie('admingui.nav.lastTreeNodeSelected')));
        }
    },

    /**
     *	This function selects the given treeNode.
     */
    selectTreeNode: function(treeNode) {
        var tree = document.getElementById(admingui.nav.TREE_ID);// admingui.nav.getTree(treeNode);
        if (tree) {
            try {
                admingui.nav.clearTreeSelection(admingui.nav.TREE_ID);
                tree.clearAllHighlight(tree.id);
                tree.highlight(treeNode);
                this.expandNode(treeNode);
                setCookie('admingui.nav.lastTreeNodeSelected', treeNode.id);
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
            YAHOO.util.Dom.setStyle(id+"_children", "display", "block");
            index = id.lastIndexOf(":");
        }
    },

    /**=
     *	This function selects the given treeNode.
     */
    selectTreeNodeById: function(treeNodeId) {
        var tree = document.getElementById(admingui.nav.TREE_ID);
        //admingui.nav.getTreeFrameElementById(treeNodeId));
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
        var tree = document.getElementById(admingui.nav.TREE_ID);
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
        return document.getElementById(admingui.nav.TREE_ID).getParentTreeNode(treeNode);
    },

    getContainingTreeNode: function(href) {
        var node =  document.getElementById(admingui.nav.TREE_ID).findContainingTreeNode(href);
        return node;
    },

    getTree: function(treeNode) {
        if (treeNode) {
            var node = document.getElementById(admingui.nav.TREE_ID);
            return node.getTree(treeNode);
        }
        return null;
    }
};

admingui.help = {
    launchHelp: function(url) {
	var helpLink = "/common/help/help.jsf";
	var helpKeys = admingui.util.findNodes(document,
	    function(node, name) {
		if ((typeof(node.name) === "undefined") || (node.name == null)) {
		    return false;
		}
		var pos = node.name.lastIndexOf(':');
		var shortName = (pos > -1) ? node.name.substring(pos+1) : node.name;
		return (shortName == name);
	    },
	    "helpKey");
	if (!(helpKeys === null)) {
	    helpLink = helpLink + "?contextRef=" + helpKeys[0].value;
	}
	admingui.help.openHelpWindow(helpLink);
    },

    openHelpWindow: function (url) {
	win = window.open(url, "HelpWindow" , "width=800, height=530, resizable"); 
	win.focus();
    }
};

//============================================================
/**
  *   Validation functions
  */

function guiValidate(reqMsg, reqInt, reqPort) {
    var inputs = document.getElementsByTagName("input");
    var styleClass = null;
    var component = null;
    for ( i=0; i < inputs.length; i++) {
        component = inputs[i];
	// Find the styleClass for this input
	// styleClass = admingui.util.getWoodstockProp(inputs[i], "className");  This is the woodstock 4.4.0.1 style
        styleClass = component.className;
        if (styleClass == null || styleClass == '') {
            continue;
        }
        if (styleClass.match("require")) {
            if (component.value=='') {
                component.select();
                component.focus();
                return showAlert(reqMsg + ' ' + getLabel(component));
            }
        }
        
        if (styleClass.match("intAllowMinusOne")) {
            if (component.value =='' || component.value == '-1')
                return true;
            if (! checkForIntValue(component.value)) {
                component.select();
                component.focus();
                return showAlert(reqInt + ' ' + getLabel( component ));
            }
        }

        if (styleClass.match("intAllowMinus")) {
            var num = 0;
            if (component.value =='')  return true;
            if ((num + component.value) <=0) return true;
            if (! checkForIntValue(component.value)) {
                component.select();
                component.focus();
                return showAlert(reqInt + ' ' + getLabel( component ));
            }
        }


        if (styleClass.match("integer")) {
            if (! checkForIntValueOrEmpty(component.value)) {
                component.select();
                component.focus();
                return showAlert(reqInt + ' ' + getLabel( component ));
            }
        }


        if (styleClass.match("port")) {
            if (! checkForPortOrEmpty(component.value)) {
                component.select();
                component.focus();
                return showAlert(reqPort + ' ' + getLabel( component ));
            }
        }
    }
    return true;
}

// FIXME: We should combine guiValidate() and guiValidateWithDropDown() these
// FIXME: perform similar operations but b/c of testing reasons we
// FIXME: added two methods.   We should combine these in the future. 

function guiValidateWithDropDown(reqMsg,reqInt, reqPort, reqMsgSelect){
    var selectFields = document.getElementsByTagName("select");
    if (!guiValidate(reqMsg, reqInt, reqPort)) {
	return false;
    }
    var component = null;
    var styleClass = null;
    for (i=0; i < selectFields.length; i++) {
        component = selectFields[i];
	// Find the styleClass for this input
        // styleClass = admingui.util.getWoodstockProp(selectFields[i], "className");  This is the woodstock 4.4.0.1 style
        styleClass = component.className;
        if (styleClass == null || styleClass == '') {
            continue;
        }
        if (styleClass.match("require")) {
            if (component.value=='') {
                component.focus();
                return showAlert(reqMsgSelect + ' ' + getLabel(component));
            }
        }
    }
    return true;
}

function getLabel(component) {
    var id = component.id;
    var propId = id.substring(0,id.lastIndexOf(":"));
    var ss = propId.substring(propId.lastIndexOf(":")+1);
    var labelid=propId+':'+ss+'_label';
    var label = document.getElementById(labelid);
    var val = '';
    if (label != null) {
	//IE doesn't have textContent, need to use innerText;
	//firefox 2.0.0.1 doesn't have innerText, so need to test both.
	//val = label.textContent.substring(1);
	//val = label.innerText.substring(1);

	val = label.innerText;
	if (val ==null) {
	    val = label.textContent;
	}

	// Need to remove leading newline characters...
// FIXME: Consider using isWhitespace(val.charAt(0))
// FIXME: I didn't add it now b/c isWhitespace is defined in selectElements.js
// FIXME: and I don't have time to test that that file is included everywhere
// FIXME: that this function is called.
	while (val.charAt(0) == '\n') {
	    val = val.substring(1);
	}

	// Need to remove trailing newline characters...
// FIXME: Consider using isWhitespace(val.charAt(val.length-1))
// FIXME: I didn't add it now b/c isWhitespace is defined in selectElements.js
// FIXME: and I don't have time to test that that file is included everywhere
// FIXME: that this function is called.
	while ((val.charAt(val.length-1) == '\n') || (val.charAt(val.length-1) == ' ')) {
	    val = val.substring(0, val.length-1);
	}

	// Strip off the ':' so that it doesn't show in the alert.
	if (val.charAt(val.length-1) == ':') {
	    val = val.substring(0, val.length-1);
	}
    }
    return val;
}


function checkForIntValueOrEmpty(value) {
    if (value == '')
        return true;
    return checkForIntValue(value);
}

function checkForIntValue(value) {
    var result = (value != '') && isInCharSet(value, "0123456789");
    return result;
}

function checkForPortOrEmpty(value) {
    if (value == '')
        return true;
    return checkForPort(value);
}

function checkForPort(value) {
    if (value == '') return false;
    if (value.indexOf('${') == 0) return true;
    if (checkForIntValue(value) == false) return false;
    return checkNumbericRange(value, 1, 65535);
}

function checkNumbericRange(value, min, max) {
    var num = 0 + value;
    if (num < min || num > max)
        return false;
    return true;
}

function isInCharSet(str, charSet) {

    var i;
    for (i = 0; i < str.length; i++) {
        var c = str.charAt(i);
        if (charSet.indexOf(c) < 0) {
            return false;
        }
    }
    return true;
}

function checkForNumericValueOrEmpty(value) {
    if (value == '')
        return true;
    return checkForNumericValue(value);
}

function checkForNumericValue(value) {
    var result = (value != '') && isInCharSet(value, "0123456789.");
    //if (result == false) {
		//This comment is by Senthil on Apr 11 2007. I think this is an
		//existing bug in this API.
		//formField isn't defined, or passed to this method, so just return the
		//result for now. Fixing this API now might involve lots of other changes, at this release time, so decided to live with this bug for now.
        //formField.select();
    //}
    return result;
}




//Special check for StatementTimeout for JDBC and connector connection pool

function checkPoolAttr(componentId, msg){
    var component = getTextElement(componentId);
    var value = component.value;
    if (value == '' || value == '-1' || checkForIntValue(value))
        return true;
    showAlert(msg + ' ' + getLabel(component));
    component.focus();
    return false;

}

function checkRequired(componentId, reqMsg){
    //component = document.getElementById(componentId);
    //var value = component.getProps().value;
    var component = getTextElement(componentId);
    var value = component.value;
    var result = (value != '') && (isWhitespace(value) == false); 
    if (result == false) {
        if (reqMsg == '') {
            showAlert(getLabel(component) + ' is a required field.');
        } else {
            showAlert(reqMsg + ' ' + getLabel(component));
        }
        component.select();
        component.focus();
    }
    return result;
}

function isWhitespace(s) {
    var i; 
    var whitespace = " \t\n\r"; 
    // Search through string's characters one by one 
    // until we find a non-whitespace character. 
    // When we do, return false; if we don't, return true. 
    
    for (i = 0; i < s.length; i++) { 
        // Check that current character isn't whitespace. 
        var c = s.charAt(i); 
        if (whitespace.indexOf(c) == -1) return false; 
    } 

    // All characters are whitespace. 
    return true; 
}

function compareDate(beginDate, endDate, pattern) {
	var endDateSet = false;
	var formatNumber = getDateFormat(pattern);
	var returnValue = true;
	if(beginDate == '') {
		return false;
	}
	if(endDate == '') {
		endDate = new Date();
		endDateSet = true;
	}
	beginDate = getUSDateFormat(beginDate, formatNumber);
	var endDateArr;
	var endDateValue;
	if(!endDateSet) {
		endDate = getUSDateFormat(endDate, formatNumber);
		endDateArr = endDate.split('/');
		if(endDateArr[2].length == 2) {
			endDateArr[2] = '20' + endDateArr[2];
		}
		endDateValue = new Date(endDateArr[2], endDateArr[0], endDateArr[1]);
	}
	if(endDateSet) {
		endDateValue = endDate;
	}
	var beginDateArr = beginDate.split('/');
	if(beginDateArr[2].length == 2) {
		//make sure this is in YYYY format
		beginDateArr[2] = '20' + beginDateArr[2];
	}
	var beginDateValue = new Date(beginDateArr[2], beginDateArr[0]-1, beginDateArr[1]);
	if(beginDateValue > endDateValue) {
		returnValue = false;
	}
	return returnValue;
}

function checkDatePattern(date, pattern, delim) {
	var separatorChar;
	var format = new Array();
	var regExp = new RegExp(/\s+/);

	if(delim == '') {
		separatorChar = new Array("/", "-", ":", " ");
	}
	else {
		separatorChar = delim;
	}
	
	if(pattern != '') {
		for(i = 0; i < separatorChar.length; i++) {
			if(pattern.indexOf(separatorChar[i]) != -1) {
				if(separatorChar[i] == ' ') {
					//split any number of whitespaces
					separatorChar[i] = regExp;
				}
				delim = '/';
				format = pattern.split(separatorChar[i]);
				dateArr = date.split(separatorChar[i]);
				if(format.length != dateArr.length) {
					return false;
				}
				pattern = '';
				break;
			}
		}
		for(i = 0; i < format.length; i++) {
			if(pattern.length > 0) {
				pattern += delim;
			}
			if(format[i].toLowerCase == "yy") {
				format[i] += format[i];
			} 
			pattern += format[i]; 
		}
	}	
	formatNumber = getDateFormat(pattern);
	if(!checkForValidDate(date, formatNumber, '')) {
		return false;
	}
	return true;
}

//This API returns the format number for the given date pattern
function getDateFormat(pattern) {
	if(pattern == '') {
		return 1; //default mm/dd/yyyy pattern
	}
	pattern = pattern.toLowerCase();
	format = new Array("mm/dd/yyyy", "dd/mm/yyyy", "mm/yyyy/dd",
						"dd/yyyy/mm", "yyyy/mm/dd", "yyyy/dd/mm" );

	for(i=0; i < format.length; i++) {
		if(format[i] == pattern) {
			return i+1;
		}
	}
	//default mm/dd/yyyy pattern
	return 1;

}

//format defines whether mm/dd/yyyy format, or dd/mm/yyyy format.
//We support only two formats for now

function checkDateRanges(startComponent, endComponent, format, separatorChar) {
	start = getTextElement(startComponent);
	end = getTextElement(endComponent);

	startDate = start.value;
	endDate = end.value;

	if(startDate != '') {
		if(!checkForValidDate(startDate, format, separatorChar)){
			start.focus;
			return false;
		}
	}
	if(endDate != '') {
		if(!checkForValidDate(endDate, format, separatorChar)){
			end.focus;
			return false;
		}
	}
	return true;
}

function getUSDateFormat(date, format) {
	if(format == '' || format == 1 || date == '' || date.length < 3) {
		//In US Date format already, no need to convert
		return date;
	}
	else if(format == 2) {
		// We received date in dd//mm/yyyy format
		// Our API always treats in mm/dd/yyyy format, so shuffle accordingly.
		tmp = date[0];
		date[0] = date[1];
		date[1] = tmp;
	}
	else if(format == 3) {
		// We received date in mm/yyyy/dd format
		// Our API always treats in mm/dd/yyyy format, so shuffle accordingly.
		tmp = date[1];
		date[1] = date[2];
		date[2] = tmp;
	}
	else if(format == 4) {
		// We received date in dd/yyyy/mm format
		// Our API always treats in mm/dd/yyyy format, so shuffle accordingly.
		tmp = date[1];
		date[1] = date[0];
		date[0] = date[2];
		date[2] = tmp;
	}
	else if(format == 5) {
		// We received date in yyyy/mm/dd format
		// Our API always treats in mm/dd/yyyy format, so shuffle accordingly.
		tmp = date[1];
		date[0] = date[1];
		date[1] = date[2];
		date[2] = tmp;
	}
	else if(format == 6) {
		// We received date in yyyy/dd/mm format
		// Our API always treats in mm/dd/yyyy format, so shuffle accordingly.
		tmp = date[2];
		date[0] = date[2];
		date[2] = tmp;
	}
	return date;
}

function checkForValidDate(date, format, delim) {
	var dateValue;
	var splitChar;
	var separatorChar;
	var regExp = new RegExp(/\s+/);

	if(delim == '') {
		separatorChar = new Array("/", "-", ":", " ");
	}
	else {
		separatorChar = delim;
	}
	var dateFound = false;

	if(format == '') {
		//default format mm/dd/yyyy
		format = 1;
	}

	for(i = 0; i < separatorChar.length; i++) {
		if(date.indexOf(separatorChar[i]) != -1) {
			if(separatorChar[i] == ' ') {
				//split any number of whitespaces
				separatorChar[i] = regExp;
			}
			dateValue = date.split(separatorChar[i]);
			dateFound = true;
			break;
		}
	}

	if(dateValue == '' || dateFound == false || dateValue.length != 3) {
		return false;
	}

	if(format > 1) {
		// We received date in non-us format
		// Our API always treats in mm/dd/yyyy format, so shuffle accordingly.
		dateValue = getUSDateFormat(dateValue, format);
	}

	if(dateValue[2].length == 2) {
		//make sure this is in YYYY format
		dateValue[2] = '20' + dateValue[2];
	}
	else {
		if(dateValue[2].length != 4) {
			return false;
		}
	}

	var range = new Array(3);
	range[0] = new Array(1, 12);
	range[1] = new Array(1, 31);
	range[2] = new Array(2000, 2100);

	for(i=0; i < 3; i++) {
		if(!checkForNumericValue(dateValue[i])) {
			return false;
		}

		if(!checkNumbericRange(dateValue[i], range[i][0], range[i][1])) {
			return false;
		}
	}
	if(!checkForAllowedDays(dateValue[0], dateValue[1], dateValue[2])) {
		return false;
	}
	return true;
}

function checkForAllowedDays(month, day, year) {
	if(day < 1) {
		return false;
	}
	if((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 ||
		month == 10 || month == 12) && (day > 31 )) {
			return false;
	}
	if((month == 4 || month == 6 || month == 9 || month == 11) &&
		(day > 30)) {
			return false;
	}
	if(month == 2) {
		if(leapYear(year) && (day > 29)) {
			return false;
		}
		else {
			if(day > 28) {
				return false;
			}
		}
	}
	return true;
}

function leapYear(year) {
	if((year % 4 == 0) && !(year % 100 == 0 || year % 400 == 0)) {
		return true;
	}
	return false;
}
/*
function checkObjectName(componentId) {
    var val = document.getElementById(componentId);
    var val = formField.value;
    var result = (val != '') && 
        isInCharSet(val, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-.");
    if (result == false){
        formField.select();
    }
    return result;
}

*/

var lastSelectedIndex = 0;

function initLastSelected (objId) {
    var obj=document.getElementById(objId);
    setLastSelected (obj.selectedIndex);
}

function setLastSelected (value) {
    lastSelectedIndex = value;
}

function disableUnselect(obj) {
    if (obj.selectedIndex == -1) {
        obj.selectedIndex = lastSelectedIndex;
    }
    lastSelectedIndex = obj.selectedIndex;
}

function setSelectOption(index,obj,value) {
    obj.options[index].selected = value;
    lastSelectedIndex = index;
}


function setAllOptions(obj,value) {
    if (!hasOptions(obj)) {
        return;
    }
    for (var i=0;i<obj.options.length;i++) {
        setSelectOption(i,obj,value);
    }
}


function findSelectOptionIndex(obj,value) {
    if (!hasOptions(obj)) {
        return;
    }
    for (var i=0;i<obj.options.length;i++) {
        var optionValue = obj.options[i].text;
        if (optionValue == value) {
            return i;
        }
    }
    return -1;
}


function hasOptions(obj) {
    if (obj!=null && obj.options!=null) {
        return true;
    }
    return false;
}


function toggleSelectAll(checkbox,optionListId,dropDownId) {
    var optionList=document.getElementById(optionListId);
    var dropDownObj=document.getElementById(dropDownId);
    var dropDownSelectedValue = dropDownObj.value;
    setAllOptions (optionList, checkbox.checked);
    index = findSelectOptionIndex(optionList,dropDownSelectedValue);
    if (index > -1) {
        setSelectOption(index,optionList,true);
    }
}

/*
 * This functions submits the form when user hits enter
 */

function submitenter(e, id, msg) {
	var keyCode;
	if(window.event) {
		keyCode = window.event.keyCode;
	}
	else if(e) {
		keyCode = e.which;
	}
	else {
		return true;
	}
	if(keyCode == 13) {
    		button = document.getElementById(id);
		submitAndDisable(button, msg);
		return false;
	}
	else {
		return true;
	}
}

function getSelectedValue(field) {
    var theForm = document.forms[0];
    var selectedValue;
    for(i = 0; i < theForm.elements.length; i++) {
        var value = theForm.elements[i].name;
	if(value == null) {
            continue;
	}
	var extnsn = value.lastIndexOf(".");
	var name = value.substr(extnsn+1);
	var fieldName = theForm.elements[i];
	if(name == field && fieldName.checked) {
            selectedValue = fieldName.value;
	    break;
	}
    }
    return selectedValue;
}

function getSelectedValueFromForm(theForm, field) {
    var selectedValue = null;
    var testField = null;
    var name = null;
    for (var i = 0; i < theForm.elements.length; i++) {
        testField = theForm.elements[i];
        name = testField.name;
        if (name == null) {
            continue;
        }  
        name = name.substr(name.lastIndexOf(".")+1);
        if ((name == field) && testField.checked) {
            selectedValue = testField.value;
            break;
        }
    }
    return selectedValue;
}

function checkForSelectedValue(fieldId) { 
    var field = document.getElementById(fieldId);  
    if (field.value == '' || isWhitespace(field.value)) { 
        return false; 
    } 
    return true; 
}

function synchronizeRestartRequired(currentRestartStatus, oldRestartStatus) {
    if (currentRestartStatus != oldRestartStatus) {
        reloadHeaderFrame();
    }
    return true;
}

function reloadHeaderFrame() {
// FIXME: We no longer use frames
       parent.parent.frames["header"].location.reload();
}

admingui.deploy = {
    uploadInit: function(dirPathId, oldRestartFlag, newRestartFlag, dirSelectBtnId, filSelectBtnId, fileuploadId) {
            //
            //We need to set a timeout to delay the call to getTextElement inside disable component.
            //otherwise getTextElement will always return null, causing JS error.
            //disableComponent(dirPathId, 'text');
            window.setTimeout("disableComponent('" + dirPathId+ "', 'text')", 1);
            synchronizeRestartRequired(newRestartFlag, oldRestartFlag);
            if(getSelectedValueFromForm(document.forms['form'], 'uploadRdBtn')=='serverSide'){
                enableDOMComponent(dirPathId);
                enableBtnComponent(dirSelectBtnId);
                enableBtnComponent(filSelectBtnId);
                disableComponent(fileuploadId, 'file');
            }
    },

    uploadRdBtnAction : function(dirPathId, dirSelectBtnId, filSelectBtnId, fileuploadId, radioChoosenId) {
        //disableDOMComponent(dirPathId);
        window.setTimeout("disableComponent('" + dirPathId + "', 'text')", 1);
        disableBtnComponent(dirSelectBtnId);
        disableBtnComponent(filSelectBtnId);
        enableComponent(fileuploadId, 'file');
        comp = getTextElement(radioChoosenId);
        comp.value='client';
    },

    fileChooseAction : function(dirPathId, dirSelectBtnId, filSelectBtnId, fileuploadId, radioChoosenId) {
        enableDOMComponent(dirPathId);
        enableBtnComponent(dirSelectBtnId);
        enableBtnComponent(filSelectBtnId);
        disableComponent(fileuploadId, 'file');
        comp = getTextElement(radioChoosenId);
        comp.value='local';
    },

    showPropertySheet : function(propSheetId, obj, appNameId, contextRootId, appTypeString, appName){
        var cc = null;
        var comp = null;

        var sheets = appTypeString.split(',');
        if (propSheetId.length <=0){
            for( ix=0; ix < sheets.length; ix++){
                comp = document.getElementById('form:' + sheets[ix]);
                if (comp != null)
                    comp.style.display='none';
            }
        }else{
            for (i=0; i < sheets.length; i++){
                cc = document.getElementById('form:'+sheets[i]);
                if (cc == null){
                    continue;
                }
                if (propSheetId == sheets[i]){
                     cc.style.display='block';
                }else{
                    cc.style.display='none';
                }
            }
        }

        if (typeof(appName) != 'undefined' ) {
            admingui.deploy.setAppName(appNameId, appName, obj, appTypeString);
            //may as well set context root if it exist.
            //component = obj.getTextElement(contextRootId);
            var component = document.getElementById(contextRootId);
            if (component != null){
                component.value = getPrefix(appName);
            }
         }
    },

    setAppName : function (appNameId, appName, obj, appTypeString){

        var pfex = getPrefix(appName);
        var sfex = getSuffix(appName);

        var sfex2 = sfex.substr(1);   //remove the '.'
        // Fill in application name
        if (appNameId==null || appNameId.length <=0){
            // shouldn't be.
        }else{
            var ix = appNameId.indexOf(":");
            var ix2 = appNameId.substr(ix+1).indexOf(":");
            var str3 = appNameId.substr(ix+1+ix2);
            var sheets = appTypeString.split(',');
            for( idx=0; idx < sheets.length; idx++){
                //var comp = obj.getTextElement('form:'+sheets[idx]+str3);
                var comp = document.getElementById('form:'+sheets[idx]+str3);
                if (comp != null){
                    comp.value=pfex;
                }
            }
        }
    },

    setFieldValue : function(appNameId, value, dropDownProp, typeId, contextRootId, extensionId, obj, appTypeString) {
        var appName = extractName(value);
        //var pfex = getPrefix(appName);
        var sfex = getSuffix(appName);
        var sfex2 = '';

        //obj.getTextElement(extensionId).value=sfex;
        document.getElementById(extensionId).value = sfex;
        var appTypes = ','+appTypeString+',';

        //If no extension for file choosen, or no plugin for that extension, show dropDown type and don't fill in anything, then return;
        if (sfex != null && sfex.length > 0){
            sfex2 = sfex.substr(1);
            var tests = ','+sfex2+',';
            var inx = appTypes.indexOf(tests) ;
            if (inx == -1){
                sfex2 = '';
            }
        }
        //obj.getSelectElement(typeId).value = sfex2;
        document.getElementById(typeId).value = sfex2;
        document.getElementById(dropDownProp).style.display = 'block';
        admingui.deploy.showPropertySheet(sfex2, obj, appNameId, contextRootId, appTypeString, appName);
    },

    populateDirAndAppName : function(fileChooserId, dirPathId, appNameId, typeId, dropDownProp, contextRootId, extensionId){
        var fc = document.getElementById(fileChooserId).getSelectionValue();
        window.opener.getTextElement(dirPathId).value = fc;
        //for redeploy, there is no dropdown for app type, there is no need to fill in any field.
        if (dropDownProp != ""){
            admingui.deploy.setFieldValue(appNameId, fc, dropDownProp, typeId, contextRootId, extensionId, window.opener);
        }
    },

    checkFileInputRequired : function (componentId, reqMsg){
        var component = getFileInputElement(componentId);
        var value = component.value;
        var result = (value != '') && (isWhitespace(value) == false);
        if (result == false) {
            if (reqMsg == '') {
                showAlert(getLabel(component) + ' is a required field.');
            } else {
                showAlert(reqMsg + ' ' + getLabel(component));
            }
            component.select();
            component.focus();
        }
        return result;
    }
}

admingui.table = {
    changeOneTableButton : function(topActionGroup, tableId){
        var buttons = new Array();
        buttons[0] = topActionGroup.concat(":button1");
        admingui.table.changeButtons(buttons,tableId);
    },

    changeThreeTableButtons : function(topActionGroup, tableId){
        var buttons = new Array();
        buttons[0] = topActionGroup.concat(":button1");
        buttons[1] = topActionGroup.concat(":button2");
        buttons[2] = topActionGroup.concat(":button3");
        admingui.table.changeButtons(buttons,tableId);
    },

    changeButtons : function (buttons,tableId){
        try {
            var table = document.getElementById(tableId);// + ":_table");
            var selections =
                table.getAllSelectedRowsCount();
            var disabled = (selections > 0) ? false : true;
            for (count=0; count < buttons.length; count++) {
                var element = document.getElementById(buttons[count]);
                if (element) {
                   element.disabled = disabled;
                   element.className = disabled ? "Btn2Dis_sun4" : "Btn1_sun4";
                }
            }
        } catch (err) {
            alert(err);
        }
    },

    /*
    getAllSelectedRowsCount : function (table) {
        inputs = admingui.util.findNodes(table, function (el) {
            var hit = (el instanceof HTMLInputElement) && (el.type=="checkbox") && (el.id.indexOf(":select") == el.id.length -7) && (el.checked == true) ;
            return hit;
        });
        return (inputs) ? inputs.length : 0;
    },
    */

    initAllRows : function (tableId) {
        var table = document.getElementById(tableId);
        table.initAllRows();
    }
}

admingui.ajax = {
    lastPageLoaded : '',
    whitelist : ['onchange','onclick'],

    loadPage : function (args) {
        var url = admingui.ajax.modifyUrl(args.url);
//        if (url != admingui.ajax.lastPageLoaded) {
            admingui.util.log("Loading " + url + " via ajax.");
            var oldOnClick = args.oldOnClickHandler;

            var callback = {
                success : admingui.ajax.processPageAjax,
                failure : function(o) {
		    document.body.style.cursor = 'auto';
                    alert ("Error (" + o.status + ") loading " + url + ":  " + o.statusText);
                },
                argument : args
            };
	    // Make cursor spin...
	    document.body.style.cursor = 'wait';
            YAHOO.util.Connect.resetDefaultHeaders();
            YAHOO.util.Connect.asyncRequest('GET', url, callback, null);
            if (typeof oldOnClick == 'function') {
                admingui.util.log(oldOnClick);
//                oldOnClick();
            }
//        }
        return false;
    },

    processPageAjax : function (o) {
        admingui.ajax.updateCurrentPageLink(o.argument.url);
        var contentNode = o.argument.target;
        if (contentNode == null) {
            contentNode = document.getElementById("content");
        }
        contentNode.innerHTML = o.responseText;
	// FIXME: These 2 functions only need to be replaced after a FPR...
        webui.suntheme.hyperlink.submit = admingui.woodstock.hyperLinkSubmit;
        webui.suntheme.jumpDropDown.changed = admingui.woodstock.dropDownChanged;
        admingui.ajax.processElement(o, contentNode, true);
        admingui.ajax.processScripts(o);
	// Restore cursor
	document.body.style.cursor = 'auto';
        var node = o.argument.sourceNode;
        if (typeof node != 'undefined') {
            admingui.nav.selectTreeNodeById(node.parentNode.parentNode.id);
        }
    },

    submitFormAjax : function (form) {
        var url = admingui.ajax.modifyUrl(form.action);
        admingui.util.log ("***** Submitting form to:  " + url);
        admingui.ajax.updateCurrentPageLink(url);
        var callback = {
            success : admingui.ajax.processPageAjax,
            failure : function(o) {
                alert ("Error (" + o.status + ") loading " + url + ":  " + o.statusText);
            },
            upload  : admingui.ajax.processPageAjax,
            argument : {url: url, target: document.getElementById('content'), oldOnClickHandler: null}
        };
        YAHOO.util.Connect.setForm(form, admingui.ajax.uploadingFiles(form));
        YAHOO.util.Connect.asyncRequest('POST', url, callback);
    },

    updateCurrentPageLink : function (url) {
        admingui.ajax.lastPageLoaded = url;
        //document.getElementById("currentPageLink").href = url;
    },

    uploadingFiles : function(form) {
        var uploading = false;
        for (var i = 0; i < form.elements.length; i++) {
            if (form.elements[i].nodeName == 'INPUT') {
                if (form.elements[i].type == 'file') {
                    uploading = true;
                    break;
                }
            }
        }

        return uploading;
    },

    processElement : function (context, node, queueScripts) {
	var recurse = true;
        //console.log("nodeName = " + node.nodeName);
        if (node.nodeName == 'A') {
            if (!admingui.ajax._isTreeNodeControl(node) && (node.target == '')) { //  && (typeof node.onclick != 'function'))
                var shouldReplace = true;
                if ((typeof node.onclick == 'function') && (node.id.indexOf("treeForm:tree") == -1)) {
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
        } else if (node.nodeName == 'FORM') {
            admingui.util.log("***** form action:  " + node.action);
            if (node.target == '') {
                node.onsubmit = function () {
                    admingui.ajax.submitFormAjax(node);
                    return false;
                };
            }
        } else if (node.nodeName == 'TITLE') {
	    recurse = false;
            document.title = node.text;
	    // Node no longer needed, mark for removal
	    // Can't remove here, breaks tree traversal...
	    //node.parentNode.removeChild(node);
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
	if (recurse) {
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
    },

    modifyUrl : function (url) {
        // If the url does not start with 'http' (or 'https' by extension), calculate
        // the "base" URL based off of window.location
        if (url.substr(0,4) != 'http') {
            //http://localhost:4848/common/applications/applications.jsf
            //http://admin.foo.com/common/applications/applications.jsf
            var location = window.location;
            url = location.protocol + "//" + location.host + url
        }
        if (url.indexOf('bare') > -1) {
            return url;
        }
        
        var insert = '?bare=true';
        var changed = url;

        if (url.indexOf("?") > -1) {
            insert = "&bare=true"
        }
        var hash = url.indexOf("#");
        if (hash > 1) {
            changed = url.substr(0, hash) + insert + url.substr(hash);
        } else {
            changed = url + insert;
        }

        return changed;
    },

    /**
     *	handler - The name of the handler to invoke.
     *	args - An object containing properties / values for the parameters.
     *	callback - A JS function that should be notified.
     *	depth - the max depth of all return variables to be encoded in json
     *	async - false if a syncronous request is desired, default: true
     */
    invoke: function(handler, args, callback, depth, async) {
	if ((typeof(handler) === 'undefined') || (handler == '')) {
	    return;
	}
	if (typeof(callback) === 'undefined') {
	    callback = function() {};
	}
	var params = '';
	for (var param in args) {
	    // Create a String to represent all the parameters
	    // escape, this will prevent the server-side from (fully)
	    // urldecoding it.  Allowing me to first parse the commas, then
	    // decode the content.
	    params += param + ':' + escape(args[param]) + ',';
	}
	if (typeof(async) === 'undefined') {
	    async = true;
	}
	if (!(typeof(jsf) === 'undefined') && !(typeof(jsf.ajax) === 'undefined')) {
	    // Warp user's function to make easier to use
	    var func = function(data) {
		if (data.status === 'success') {
		    var respElt = document.getElementById('execResp');
		    if (typeof(respElt) !== 'undefined') {
			var result = '';
			if (respElt.value != '') {
			    result = '(' + respElt.value + ')';
			    result = eval(result).content;
			}
			callback(result, data);
		    }
		}
	    }
	    if (typeof(depth) === 'undefined') {
		depth = 3;
	    }
	    var src = document.getElementById('execButton');
	    if ((src == null) || (typeof(src) === 'undefined')) {
		alert("'execButton' not found!  Unable to submit JSF2 Ajax Request!");
	    } else {
		jsf.ajax.request(src, null,
		    {
			execute: 'execButton',
			render: 'execResp',
			execButton: 'execButton',
			h: handler,
			d: depth,
			a: params,
			onevent: func,
			// FIXME: async: false does not work in JSF 2 as of 10/21/2009
			async: async
		    });
	    }
	} else {
	    alert('JSF2+ Ajax Missing!');
	}
    },

    getResource: function(path, callback) {
	admingui.ajax.invoke("gf.serveResource", {path:path, content:content}, callback, 1, true);
    }
}

admingui.woodstock = {
    hyperLinkSubmit: function(hyperlink, formId, params) {
        //params are name value pairs but all one big string array
        //so params[0] and params[1] form the name and value of the first param
        var form = document.getElementById(formId);
        //var oldTarget = theForm.target;
        //var oldAction = theForm.action;
        //theForm.action += "&" + hyperlink.id + "_submittedField="+hyperlink.id;
	var oldaction = form.action;
        form.action = //admingui.ajax.lastPageLoaded +
            admingui.ajax.modifyUrl(form.action) + "&" + hyperlink.id + "_submittedField="+hyperlink.id;
        if (params != null) {
            for (var i = 0; i < params.length; i++) {
                form.action +="&" + params[i] + "=" + params[i+1];
                i++;
            }
        }
	var oldtarget = form.target;
        if (hyperlink.target != "") {
            form.target = hyperlink.target;
        }
        admingui.ajax.submitFormAjax(form);

	// Retore form action
	form.target = oldtarget;
	form.action = oldaction;

        return false;
    },

    dropDownChanged: function(elementId) {
        var jumpDropdown = webui.suntheme.dropDown.getSelectElement(elementId);
        var form = jumpDropdown;
        while(form != null) {
            form = form.parentNode;
            if(form.tagName == "FORM") {
                break;
            }
        }
        if(form != null) {
            var submitterFieldId = elementId + "_submitter";
            document.getElementById(submitterFieldId).value = "true";

            var listItem = jumpDropdown.options;
            for (var cntr=0; cntr < listItem.length; ++cntr) {
                if (listItem[cntr].className ==
                            webui.suntheme.props.jumpDropDown.optionSeparatorClassName
                        || listItem[cntr].className ==
                            webui.suntheme.props.jumpDropDown.optionGroupClassName) {
                    continue;
                } else if (listItem[cntr].disabled) {
                    // Regardless if the option is currently selected or not,
                    // the disabled option style should be used when the option
                    // is disabled. So, check for the disabled item first.
                    // See CR 6317842.
                    listItem[cntr].className = webui.suntheme.props.jumpDropDown.optionDisabledClassName;
                } else if (listItem[cntr].selected) {
                    listItem[cntr].className = webui.suntheme.props.jumpDropDown.optionSelectedClassName;
                } else {
                    listItem[cntr].className = webui.suntheme.props.jumpDropDown.optionClassName;
                }
            }
	    var oldaction = form.action;
            form.action = //admingui.ajax.lastPageLoaded;
                admingui.ajax.modifyUrl(form.action);
            admingui.ajax.submitFormAjax(form);
	    form.action = oldaction;
        }
        return false;
    },

    commonTaskHandler : function(treeNode, targetUrl) {
        admingui.ajax.loadPage({url: targetUrl});
        admingui.nav.selectTreeNodeById(treeNode);
        return false;
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
	    admingui.ajax.getResource(node.src, function(result) {globalEval(result);globalEvalNextScript(scriptQueue);} );
	    // This gets a relative URL vs. a full URL with http://... needed
	    // when we properly serve resources w/ rlubke's recent fix that
	    // will be integrated soon.  We need to handle the response
	    // differently also.
	    //admingui.ajax.getResource(node.attributes['src'].value, function(result) { globalEval(result); globalEvalNextScript(scriptQueue);} );
	}
    }

var globalEval = function(src) {
    if (window.execScript) {
        window.execScript(src);
        return;
    }
    var fn = function() {
        window.eval.call(window, src);
    };
    fn();
};

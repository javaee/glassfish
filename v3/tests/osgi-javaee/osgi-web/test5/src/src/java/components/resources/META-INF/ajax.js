/* Copyright 2005 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
 */

var g_jsfJspContexts = new Array();

var g_request = null;
var g_progressBars = new Array();

// make it so our ajaxOnloadHandler is called when the page has 
// finished loading.
var g_windowOnload = window.onload;

window.onload = ajaxOnloadHandler;

function ajaxOnloadHandler () {
    len = g_progressBars.length;
    for (i = 0; i < len; i++) {
	startPolling(g_progressBars[i]);
    }

    if (null != g_windowOnload) {
	g_windowOnload;
    }
}

function callPollTaskmaster(clientId) {
    return (function(){
        var context = initRequest(clientId);

        context.request.onreadystatechange = processPollRequest;
        context.request.send(context.params);        
    });
}


function processInitialRequest() {
    var request = getXMLHttpRequest();
    if (request.readyState == 4) {
        var clientId = "";
        if (request.status == 200) {
            var item = request.responseXML.getElementsByTagName("percentage")[0];
            var contextItem = request.responseXML.getElementsByTagName("clientId")[0];
            var message = item.firstChild.nodeValue;
            clientId = contextItem.firstChild.nodeValue;
            createProgressBar(clientId);
            showProgress(clientId, message);
            window.status = "";
        }
        var functRef = callPollTaskmaster(clientId);
        setTimeout(functRef, 2000);
    }
}

function processPollRequest() {
    var request = getXMLHttpRequest();
    if (request.readyState == 4) {
        if (request.status == 200) {
            var item = request.responseXML.getElementsByTagName("percentage")[0];
            var contextItem = request.responseXML.getElementsByTagName("clientId")[0];
            var message = item.firstChild.nodeValue;
            var clientId = contextItem.firstChild.nodeValue;
            showProgress(clientId, message);
            var context = getContext(clientId);
            context.messageHash = message;
            if (message < 100) {
               var functRef = callPollTaskmaster(clientId);
               setTimeout(functRef, 2000);
            } else {
               complete(clientId);
            }
        } else {
            window.status = "No Update";
        }
    }
}

function complete(clientId) {
    var idiv = window.document.getElementById(clientId);
    var cur = idiv;
    // PENDING(): find the form for this element.
    document.forms[0].submit();
    return false;
}

// create the progress bar
function createProgressBar(clientId) {
    var centerCellName;
    var tableText = "";
    var context = getContext(clientId);
    for (x = 0; x < context.size; x++) {
      tableText += "<td id=\"" + clientId + "_" + x + "\" width=\"10\" height=\"10\" bgcolor=\"blue\" border=\"0\" />";
      if (x == (context.size/2)) {
          centerCellName = clientId + "_" + x;
      }
    }
    var idiv = window.document.getElementById(clientId);
    idiv.innerHTML = "<table with=\"100\" border=\"1\" cellspacing=\"0\" cellpadding=\"0\"><tr>" + tableText + "</tr></table>";
    context.centerCell = window.document.getElementById(centerCellName);
}

// show the current percentage
function showProgress(clientId, percentage) {
    var percentageText = "";
    var context = getContext(clientId);
    if (percentage < 10) {
        percentageText = "&nbsp;" + percentage;
    } else {
        percentageText = percentage;
    }
    context.centerCell.innerHTML = "<font color=\"white\">" + percentageText + "%</font>";
    var tableText = "";
    for (x = 0; x < context.size; x++) {
      var cell = window.document.getElementById(clientId + "_" + x);
      if ((cell) && percentage/x < context.increment) {
        cell.style.backgroundColor = "blue";
      } else {
        cell.style.backgroundColor = "red";
      }      
    }
}

function getContext(clientId) {
    var result = g_jsfJspContexts[clientId];
    if (null == result) {
        result = new Array();
        result.size=40;
        result.increment = 100/result.size;
        result.messageHash = -1;
        result.centerCell = null;

        g_jsfJspContexts[clientId] = result;
    }
    return result;
}

function getXMLHttpRequest() {
  if (!g_request) {
    if (window.XMLHttpRequest) {
      g_request = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
      isIE = true;
      g_request = new ActiveXObject("Microsoft.XMLHTTP");
    }
  }
  return (g_request); 
}

function getPostbackURL() {
  return (window.document.forms[0].action); 
}

function getCurrentFormId() {
  return (window.document.forms[0].id);
}

function initRequest(clientId) {
  var stateName = "javax.faces.ViewState";
  var stateArray = window.document.getElementsByName(stateName);
  var stateValue = null;
  var params = "";

  if (null != stateArray && 0 < stateArray.length) {
      stateValue = stateArray[0].value;
      var encodedValue = encodeURI(stateValue);
      var re = new RegExp("\\+", "g");
      var encodedPlusValue = encodedValue.replace(re, "\%2B");
      params = stateName + "=" + encodedPlusValue + "&";
  }
  
  var formName = getCurrentFormId();
  params = params + formName + "=" + formName + "&" + clientId + "=ajax&" +
      "bpcatalog.abortPhase=4";
  var postbackURL = getPostbackURL();
  var request = getXMLHttpRequest();

  request.open("POST", postbackURL, true);
  request.setRequestHeader("Content-Type", 
                           "application/x-www-form-urlencoded");
  var result = new Array();
  result.request = request;
  result.params = params; 
  return result;
}


function startPolling(clientId) { 
  var context = initRequest(clientId);

  context.request.onreadystatechange = processInitialRequest;
  context.request.send(context.params);

  return false;
}

// This function is necessary to allow the addition of AJAX
// functionality to a non-AJAX component.  This function is installed on
// the onmouseover event handler of the component that wraps the
// component to be AJAXified.

function ajaxFocusIn(clientId, eventHook) {
    var ajaxValidatorNode = window.document.getElementById(clientId);
    var children = ajaxValidatorNode.childNodes;
    var inputChild = children[0];
    var inputName = inputChild.getAttribute("name");
    var inputEventHook = getEventHook(inputChild);

    // Don't take action if we've already ajaxified the component
    if (null != inputEventHook &&
	-1 != inputEventHook.indexOf("ajaxValidate")) {
	return false;
    }

    setEventHook(inputChild, eventHook, inputName, inputEventHook);

    return false;
}

function ajaxValidate(clientId) {
    // Uncomment to Clear out any existing validation messages for this
    // component.
    var inputNode = window.document.getElementsByName(clientId)[0];
    var ajaxValidatorNode = inputNode.parentNode;
    var targetId = ajaxValidatorNode.getAttribute("messageId");
    var targetSpan = window.document.getElementById(targetId);
    targetSpan.innerHTML = "";

    // prepare and submit the XMLHttpRequest
    var context = prepareValidatorRequest(clientId);
    context.request.onreadystatechange = processServerValidate;
    context.request.send(context.params);

    return false;
}

function prepareValidatorRequest(clientId) {
    var stateFieldName = "javax.faces.ViewState";
    var stateElements = window.document.getElementsByName(stateFieldName);
    // In the case of a page with multiple h:form tags, there will be
    // multiple instances of stateFieldName in the page.  Even so, they
    // all have the same value, so it's safe to use the 0th value.
    var stateValue = stateElements[0].value;
    // We must carefully encode the value of the state array to ensure
    // it is accurately transmitted to the server.  The implementation
    // of encodeURI() in mozilla doesn't properly encode the plus
    // character as %2B so we have to do this as an extra step.
    var uriEncodedState = encodeURI(stateValue);
    var rexp = new RegExp("\\+", "g");
    var encodedState = uriEncodedState.replace(rexp, "\%2B");
    // A truly robust implementation would discern the form number in
    // which the element named by "clientId" exists, and use that as the
    // index into the forms[] array.
    var formName = window.document.forms[0].id;
    // build up the post data
    var fieldValue = window.document.getElementsByName(clientId);
    var ajaxValidatorNode = fieldValue[0].parentNode;
    var ajaxValidatorId = ajaxValidatorNode.getAttribute("id");
    fieldValue = fieldValue[0].value;

    var params = stateFieldName + "=" + encodedState + "&" + formName + "=" + formName + "&" + clientId + "=" + fieldValue + "&" + ajaxValidatorId + "=ajax";
    // Again, this is safe to use the 0th form's action because each
    // form in the page has the same action.
    var formAction = window.document.forms[0].action;
    var request = getXMLHttpRequest();

    request.open("POST", formAction, true);
    request.setRequestHeader("Content-Type", 
			     "application/x-www-form-urlencoded");
    
    var result = new Array();
    result.request = request;
    result.params = params;

    return result;
}


function processServerValidate() {
    var request = getXMLHttpRequest();
    
    if (request.readyState == 4) {
	if (request.status == 200) {
	    // This element is the actual validation message.
	    var validationMessage = 
		request.responseXML.getElementsByTagName("validationMessage")[0];
	    validationMessage = validationMessage.firstChild.nodeValue;

	    // This element is the client Id of the component that is
	    // being validated.
	    var clientId = 
		request.responseXML.getElementsByTagName("clientId")[0];
	    clientId = clientId.firstChild.nodeValue;

	    // This element is the span that the page author provided
	    // for us into which we will output the message.
	    var targetId = 
		request.responseXML.getElementsByTagName("messageId")[0];
	    targetId = targetId.firstChild.nodeValue;
	    // get the actual span element.
	    var targetSpan = window.document.getElementById(targetId);
	    targetSpan.innerHTML = validationMessage;
	}
    }
}

function debugTrap() {
    var foo = null; 
    foo.text = 'ha'; 
    return false;
}

function setEventHook(obj, eventHook, clientId, oldEventHook) {
    var functRef = null;

    if (null != oldEventHook && 0 < oldEventHook.length()) {
	functRef = function() {
	    ajaxValidate(clientId);
	    oldEventHook();
	}
    }
    else {
	functRef = function() {
	    ajaxValidate(clientId);
	}
    }

    if (-1 != eventHook.indexOf("onclick")) {
	obj.onclick = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("ondblclick")) {
	obj.ondblclick = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("onmousedown")) {
	obj.onmousedown = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("onmouseup")) {
	obj.onmouseup = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("onmouseover")) {
	obj.onmouseover = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("onmousemove")) {
	obj.onmousemove = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("onmouseout")) {
	obj.onmouseout = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("onkeypress")) {
	obj.onkeypress = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("onkeydown")) {
	obj.onkeydown = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("onkeyup")) {
	obj.onkeyup = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("onblur")) {
	obj.onblur = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("onfocus")) {
	obj.onfocus = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("onreset")) {
	obj.onreset = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("onsubmit")) {
	obj.onsubmit = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("onchange")) {
	obj.onchange = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    else if (-1 != eventHook.indexOf("onselect")) {
	obj.onselect = functRef;
	obj.ajaxValidate = "ajaxValidate";
    }
    

    return false;
}

function getEventHook(obj) {
    var result = obj.ajaxValidate;

    return result;
}

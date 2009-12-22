/* Copyright 2005 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
 */

/* TODO: I still need to add code to track the requests in the requests object indexed by
   the targetName */
/* TODO: cache returned results for recent prefixes such that if users are backing
   up in the textfield we don't need to bother the server */
/* TODO: keyboard navigation, and automatically select best match. Insert this
   match in the textfield and select the untyped remainder */
var req;
var requests;
var target;

function getElementX(element){
    var targetLeft = 0;
    while (element) {
        if (element.offsetParent) {
            targetLeft += element.offsetLeft;
        } else if (element.x) {
            targetLeft += element.x;
        }
        element = element.offsetParent;
    }
    return targetLeft;
}


function getElementY(element){
    var targetTop = 0;
    while (element) {
        if (element.offsetParent) {
            targetTop += element.offsetTop;
        } else if (element.y) {
            targetTop += element.y;
        }
        element = element.offsetParent;
    }
    return targetTop;
}

function getWidth(element){
    if (element.clientWidth && element.offsetWidth && element.clientWidth <element.offsetWidth) {
        return element.clientWidth; /* some mozillas (like 1.4.1) return bogus clientWidth so ensure it's in range */
    } else if (element.offsetWidth) {
        return element.offsetWidth;
    } else if (element.width) {
        return element.width;
    } else {
        return 0;
    }
}

function initRequest(url) {
    if (window.XMLHttpRequest) {
        req = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        req = new ActiveXObject("Microsoft.XMLHTTP");
    }
}

function doCompletion(targetName, menuName, method, onchoose, ondisplay) {
    var target = document.getElementById(targetName);
    var menu = document.getElementById(menuName);
    menu.style.left = getElementX(target) + "px";
    menu.style.top = getElementY(target) + target.offsetHeight + 2 + "px";
    var width = getWidth(target);
    if (width > 0) {
        menu.style.width = width + "px";
    }
    var url = "faces/ajax-autocomplete?method=" + escape(method) + "&prefix=" + escape(target.value);
    initRequest(url);

    if (!requests) {
        requests = new Object();
    }
    requests.menu = menu;
    requests.onchoose = onchoose;
    requests.ondisplay = ondisplay;
    requests.targetName = targetName;

    req.onreadystatechange = processRequest;
    req.open("GET", url, true);
    req.send(null);
}

function chooseItem(targetName, item) {
    if (!requests.onchoose) {
        var target = document.getElementById(targetName);
        target.value = item;
    } else {
        requests.onchoose(item);
    }
}

function stopCompletion(menuName) {
    var menu = document.getElementById(menuName);
    if (menu != null) {
        clearItems(menu);
        menu.style.visibility = "hidden";
    }
}

/* Stop completion shortly.
   This is necessary because I want to stop completion from the blur
   (focus loss event) of the completion text field, but that will also
   happen, right BEFORE a link click in the completion dialog is processed.
   If this is done synchronously, the link is deleted before it is processed
   by stop completion. Therefore, I use the delayed variety which schedules
   stop completion instead such that the link is processed first.
*/
function stopCompletionDelayed(menuName) {
    /* Would like to shorten timeout but this seems to trip up Safari */
    setTimeout("stopCompletion('" + menuName + "')", 400);
}

function processRequest() {
    if (req.readyState == 4) {
        if (req.status == 200) {
          parseMessages(requests.menu);
        } else if (req.status == 204){
            clearItems(requests.menu);
        }
    }
}

function parseMessages(menu) {
    clearItems(menu);
    menu.style.visibility = "visible";
    var items = req.responseXML.getElementsByTagName("items")[0];
    for (loop = 0; loop < items.childNodes.length; loop++) {
        var item = items.childNodes[loop];
        appendItem(menu, item.childNodes[0].nodeValue);
    }
}

function clearItems(menu) {
    if (menu) {
      for (loop = menu.childNodes.length -1; loop >= 0 ; loop--) {
         menu.removeChild(menu.childNodes[loop]);
      }
    }
}

function appendItem(menu, name) {
    var item = document.createElement("div");
    menu.appendChild(item);
    var linkElement = document.createElement("a");
    linkElement.className = "popupItem";
    linkElement.href = "#";
    linkElement.onclick = function() {
        chooseItem(requests.targetName, name); 
        stopCompletion();
        return false;
    }
    var displayName = name;
    if (requests.ondisplay) {
        displayName = requests.ondisplay(name);
    }
    linkElement.appendChild(document.createTextNode(displayName));
    item.appendChild(linkElement);
}

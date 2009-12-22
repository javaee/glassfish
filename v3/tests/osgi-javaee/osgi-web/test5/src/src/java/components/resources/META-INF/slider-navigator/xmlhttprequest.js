/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.
 * You may not modify, use, reproduce, or distribute this software except
 * in compliance with the terms of the License at: 
 * http://developer.sun.com/berkeley_license.html
 * $Id: xmlhttprequest.js,v 1.1 2005/11/19 00:54:43 inder Exp $
 */
/*
 * XMLHttpRequest Object
 */
var XmlHttpR = function () {
  this.request = XmlHttpRequestFactory.create();
}

/*
 * Method definitions
 */
with (XmlHttpR) {
  /*
   * @param method 'GET' or 'POST'
   * @param url  the URL to send(must be URL-encoded)
   * @param func  callback function
   * @param postdata Postdata, in case of 'POST'. otherwise, "" or null(must be url-encoded)
   * @param xmlresponsetype 1 or true if responseXML is necessary
   */
  prototype.getResponse = function (method, url, func, postdata, xmlresponseType) {
    var req = this.request;
    req.onreadystatechange= function() {
      if (req.readyState == 4 && req.status == 200) {
          if (xmlresponseType) {
              func(req.responseXML);
          } else {
              func(req.responseText);
          }
      }
    };
    req.open(method, url, true);
    req.setRequestHeader("pragma", "no-cache");
    req.setRequestHeader("cache-control", "no-cache");
    if (method == 'GET') {
      req.send(null);
    } else if (method == 'POST') {
      req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
      req.send(postdata);
    }
  }
}

/* 
 * XMLHttpRequest Factory
 */
function XmlHttpRequestFactory() {
}
XmlHttpRequestFactory.create = function () {
  if (!window.XMLHttpRequest && window.ActiveXObject) {
    var ieArray = new Array(
      'Msxml2.XMLHTTP.5.0',
      'Msxml2.XMLHTTP.4.0',
      'Msxml2.XMLHTTP.3.0',
      'Msxml2.XMLHTTP',
      'Microsoft.XMLHTTP');
    for (var i = 0; i < ieArray.length; i++) {
      try {
        return new ActiveXObject(ieArray[i]);
      } catch (e) {
      }
    }
  } else if (window.XMLHttpRequest && !window.ActiveXObject) {
    return new XMLHttpRequest();
  } else {
    return null;
  }
}

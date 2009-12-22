/* Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * You may not modify, use, reproduce, or distribute this software except
 * in compliance with the terms of the License at: 
 * http://developer.sun.com/berkeley_license.html
 * $Id: xslt.js,v 1.1 2005/11/19 00:54:44 inder Exp $
 */
/*
 * XSLT generic object
 */
function Xslt(xsl) {
  this.stylesheet = xsl;
}

Xslt.create = function(xsl) {
  return new Xslt(xsl);
};

/*
 * Create Dom object without using xmlhttprequest
 */
Xslt.createLocal = function(url) {
  var xmlDoc;
  if (window.ActiveXObject)  {
    if (url.match(/\.xsl$/)) {
      xmlDoc = new ActiveXObject("Msxml2.FreeThreadedDOMDocument");
    } else {
      xmlDoc = new ActiveXObject("MSXML2.DOMDocument");
    }
    if (!xmlDoc) {
      xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
    }
    xmlDoc.async = false;
    xmlDoc.validateOnParse = true;
    if(xmlDoc.load(url)){
        return new Xslt(xmlDoc);
    }
  } else if ( document.implementation && 
              document.implementation.createDocument ) {
    xmlDoc = document.implementation.createDocument("","",null);
    //xmlDoc.onload = function (){ return xmlDoc;}
    xmlDoc.load(url);
    return new Xslt(xmlDoc);
  }
  return null;
};

/*
 * Transform XML with this.stylesheet.
 *
 * @param xm xml dom object
 * @param id element ID to append
 * @param key Key for XSL external parameter
 * @param value Value for XSL external parameter
 */
Xslt.prototype.transform = function(xm, id, key, value) {
  var elemResult = document.getElementById(id);
  var xslProc;
  elemResult.innerHTML = "";
  if (typeof XSLTProcessor != 'undefined') {
    xslProc = new XSLTProcessor();
    xslProc.importStylesheet(this.stylesheet);
    if (key) {
      xslProc.setParameter(null, key, value);
    }
    var fragment = xslProc.transformToFragment(xm, window.document);
    //elemResult.innerHTML = "";
    elemResult.appendChild(fragment);

  } else if (typeof xm.transformNode != "undefined") {
    var xslTemp = new ActiveXObject("Msxml2.XSLTemplate");
    xslTemp.stylesheet = this.stylesheet;
    xslProc = xslTemp.createProcessor();
    xslProc.input = xm;
    if (key) {
      xslProc.addParameter(key, value);
    }
    xslProc.transform();
    elemResult.innerHTML = xslProc.output;

  } else {
  }
};

/*
 * Fetch XML asynchronously - if XML is updated,
 * execute xslt.
 */
var lastModified = null;
var cachedXml = null;
/* cannot use XMLHttpR object
 *   - this needs "if-modified-since" (possibly rewrite XMLHttpR obj)
 */
function retrieveXml(url, id, element) {
  var req = XmlHttpRequestFactory.create();
  if (req) {
  req.onreadystatechange = function() {
    if (req.readyState == 4) {
      if (req.getAllResponseHeaders().match("Last-Modified"))
        lastModified = req.getResponseHeader("Last-Modified");
      
      if (req.status == 200) {
        cachedXml = req.responseXML;
        transform(id, req.responseXML, element);
      } else if (req.status == 304) {
        // if xml is not changed since the last time, use the cached one
        if (cachedXml != null) {
          transform(id, cachedXml, element);
        }
      }
    }
  };
  req.open('GET', url, true);
  if (lastModified != null) {
    req.setRequestHeader("If-Modified-Since", lastModified);
  }
  req.send(null);
  }
}

function transform(catid, xml, element) {
  userName = name;    // This might be the same value
  if (xml != null) {
    xsltObj.transform(xml, element, "id", catid);
  }
}



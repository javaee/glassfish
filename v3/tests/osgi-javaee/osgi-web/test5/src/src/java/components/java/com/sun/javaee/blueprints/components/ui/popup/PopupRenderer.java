/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.javaee.blueprints.components.ui.popup;

import java.io.IOException;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;
import javax.faces.context.ResponseWriter;
import javax.faces.context.ResponseStream;

import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * Renderer that converts the JSF tag to html and javascript
 *
 * @author basler
 */
public class PopupRenderer  extends Renderer {
    
    private boolean bDebug=false;
    private static final String RENDERED_SCRIPT_KEY = "bpcatalog-ajax-script-popup";

    /** Creates a new instance of PopupRenderer */
    public PopupRenderer() {
    }
    

    /**
     * <p>Encode the begining of the popup tag with the instance specific javascript and html/p>
     *
     * @param context   <code>FacesContext</code>for the current request
     * @param component <code>UIComponent</code> to be decoded
     */
    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        UIOutput outComp = (UIOutput)component;
        ResponseWriter writer = context.getResponseWriter();
        
        // set javascript main script file just once
        renderScriptOnce(writer, component, context);

        // get properties from UIOutput that were set by the specific taghandler
        String id=(String)outComp.getAttributes().get("id");
        String xmlHttpRequestURL=(String)outComp.getAttributes().get("xmlHttpRequestURL");
        String elementNamePairs=(String)outComp.getAttributes().get("elementNamePairs");
        if(bDebug) System.out.println("XXX Renderer Data - " + id + " - " +  xmlHttpRequestURL + " - " + elementNamePairs);

        // read in specific javascript template for token replacement
        URL sxURL = PopupRenderer.class.getResource("/META-INF/popup/PopupTemplateTop.txt");
        String sxTemplate=PopupUtil.readInFragmentAsString(sxURL);
        
        // create the specific parsing of the XMLHttpRequest return message
        String sxScript=createParseMessageScript(elementNamePairs);
        if(bDebug) System.out.println(" Parse Message Script " + sxScript);
        
        // populate hashmap with tokens and replacement values
        HashMap hmSub=new HashMap();
        hmSub.put("%%%ID%%%", id);
        hmSub.put("%%%REQUESTURL%%%", xmlHttpRequestURL);
        hmSub.put("%%%PARSE_MESSAGE_CODE%%%", sxScript);
        
        // parse page and put in substitutions, then write to output
        writer.write(PopupUtil.parseString(sxTemplate, hmSub, false));
                
    }
    
    
    /**
     * <p>Encode the end of the popup tag with the instance specific javascript and html/p>
     *
     * @param context   <code>FacesContext</code>for the current request
     * @param component <code>UIComponent</code> to be decoded
     */
    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        UIOutput outComp = (UIOutput)component;
        //String sxURL=getClass().getResource("PopupRenderer.class").toString();
        //String sxResource="/resources/PopupTemplateBottom.txt";
        URL sxURL = PopupRenderer.class.getResource("/META-INF/popup/PopupTemplateBottom.txt");
        String sxResource=PopupUtil.readInFragmentAsString(sxURL);        
        ResponseWriter writer = context.getResponseWriter();

        // read in bottom of popup
        /*
        // if tokens need to be changed, use this method
        String sxTemplate=PopupUtil.readInFragmentAsString(sxURL, sxResource);
        HashMap hmSub=new HashMap();
        hmSub.put("%%%id%%%", id);
        hmSub.put("%%%PARSE_MESSAGE_CODE%%%", sxScript);
        writer.write(PopupUtil.parseString(sxTemplate, hmSub, false));
        */
        
        // no tokens to replace in bottom, so just write it directly
        PopupUtil.readWriteCharUtil(sxURL, writer);        
    }
 
    
    /*
     * Method to parse the elementNamePairs attribute of the popup tag and build the javascript that inserts the values in the table
     * This mechanism works on the name value pairs specified in the "elementNamePairs" tag of the component.  The name represent 
     * the named element in the xml doc and the value is the actual id of the element in the html doc
     * 
     * @param fields The name/value pairs to map the xml to the html doc
     * @return String returns the resultant javascript fragment
     */
    private String createParseMessageScript(String fields) {
    
        StringBuffer sxBuff=new StringBuffer();
        String docKey="";
        // for simplicity, the response schema is expected have a "response" root element and all the return values should be child elements.
        /*
            <response>
                DATA
            </response>
        */
        sxBuff.append("var resultx = req.responseXML.getElementsByTagName(\"response\")[0];\n");

        // parse fields strings that should be in name-value pair
        StringTokenizer st=new StringTokenizer(fields, ",");
        String token;
        int iPos=0;
        while(st.hasMoreTokens()) {
            
            // should be in the format xmlKey=htmlDocKey
            token=st.nextToken();
        
            iPos=token.indexOf("=");
            docKey=token.substring(iPos+1);
            
            // see if there is img or image in the name, so "src" attribute can be populated instead of the innerHTML
            if(docKey.toLowerCase().indexOf("img") >= 0 || docKey.toLowerCase().indexOf("image") >= 0) {
                // is image so put data in "src" attribute
                sxBuff.append("document.getElementById(\"" + 
                    docKey +
                    "\").src=resultx.getElementsByTagName(\""+ 
                    token.substring(0, iPos) + 
                    "\")[0].childNodes[0].nodeValue;\n");
            } else {
            
                sxBuff.append("document.getElementById(\"" + 
                    docKey +
                    "\").firstChild.nodeValue=resultx.getElementsByTagName(\""+ 
                    token.substring(0, iPos) + 
                    "\")[0].childNodes[0].nodeValue;\n");
            }
        }
        return sxBuff.toString();
    }


    /** Render the &lt;script&gt; tag which contains supporting JavaScript
     * for this text field.  This is a common method among the AJAX components from the other samples.
     */
    private void renderScriptOnce(ResponseWriter writer, UIComponent component, FacesContext context)
        throws IOException {
        
        // Store attribute in request map when we've rendered the script such
        // that we only do this once per page
        Map requestMap=context.getExternalContext().getRequestMap();
        Boolean scriptRendered=(Boolean)requestMap.get(RENDERED_SCRIPT_KEY);

        if (scriptRendered == Boolean.TRUE) {
            return;
        }

        requestMap.put(RENDERED_SCRIPT_KEY, Boolean.TRUE);

        // these entries must be added only onces
        writer.write("\n");
        
        // CSS 
        writer.startElement("link", component);
        writer.writeAttribute("rel", "stylesheet", null);
        writer.writeAttribute("type", "text/css", null);
        writer.writeAttribute("href", "popup.css", null);
        writer.endElement("link");
        writer.write("\n");
        
        // JavaScript
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        // This relative URI assumes that it goes through the faces servlet.
        writer.writeAttribute("src", "popup.js", null);
        writer.endElement("script");
        writer.write("\n");
    }
}

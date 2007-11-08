/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.config.serverbeans.validation;

import java.io.Writer;
import org.xml.sax.helpers.DefaultHandler;
import com.sun.enterprise.util.LocalStringManager;
import java.util.ArrayList;
import java.io.IOException;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import java.util.logging.Logger;

class Localiser extends DefaultHandler
{
    private final LocalStringManager lsm;
    private final Writer out;
    private final Logger logger;
    private final String prefix;
    

    Localiser(final LocalStringManager lsm, final Writer out, final String prefix){
        this(lsm, out, null, prefix);
    }

    Localiser(final LocalStringManager lsm, final Writer out, final Logger logger){
        this(lsm, out, logger, null);
    }

    Localiser(final LocalStringManager lsm, final Writer out){
        this(lsm, out, null, null);
    }
    
    Localiser(final LocalStringManager lsm, final Writer out, final Logger logger, final String prefix){
        this.lsm = lsm;
        this.out = out;
        this.logger = logger;
        this.prefix = (prefix == null ? "" : prefix +".");
    }

    public void startElement(final String namespaceURI,
                         final String localName,
                         final String qName,
                         final Attributes atts)
        throws SAXException {
        if (localName.equals("location")){
            startLocation();
        } else if (localName.equals("message")){
            startMessage(atts);
        } else if (localName.equals("param")) {
            startParam(atts);
        }
    }
    public void characters(char[] ch,
                       int start,
                       int length)
        throws SAXException{
        if (null != textBuffer){
            textBuffer.append(ch, start, length);
        }
    }

    public void endElement(String namespaceURI,
                           String localName,
                           String qName)
        throws SAXException{
        if (localName.equals("location")){
            endLocation();
        } else if (localName.equals("message")){
            endMessage();
        } else if (localName.equals("param")) {
            endParam();
        }
    }

    private String message_id;
    private ArrayList params = new ArrayList(5);
    private StringBuffer textBuffer;

    private void startLocation(){
        textBuffer = new StringBuffer();
    }

    private void endLocation() throws SAXException {
        if (null != textBuffer && textBuffer.length() > 0){
            try {
                textBuffer.append(" ");
                out.write(textBuffer.toString());
            }
            catch (IOException e){
                throw new SAXException(e);
            }
        }
        textBuffer = null;
    }
    
    private void startMessage(final Attributes attr) throws SAXException {
        params.clear();
        final String id = attr.getValue("", "id");
        if (null == id || id.length() == 0){
            throw new SAXException("id attribute is either null or empty");
        }
        message_id = prefix + id;
    }

    private void startParam(final Attributes attr) {
        textBuffer = new StringBuffer();
    }
    
    private void endParam(){
        params.add(textBuffer.toString());
        textBuffer = null;
    }

    private void endMessage() throws SAXException {
        final String msg = lsm.getLocalString(null, message_id,
                           "Internal Error, message id  \""+message_id+ "\" not present in localisation file",
                           params.toArray());
        message_id = null;
        
        if (null != logger && msg.startsWith("Internal Error")){
            logger.severe(msg);
            return;
        }
        
        try {
            out.write(msg+"\n");
            out.flush();
        }
        catch (IOException e){
            throw new SAXException(e);
        }
        
            
    }
        
}

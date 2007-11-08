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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
   This class provides a variable expansion mechanism on a sax
   stream. Variable references are strings that begin with "${" and
   end with "}", with the variable name lying between the braces. For
   example "${foo}" is a reference to a variable called "foo". From
   some given set of definitions for these variables this class
   expands these variables on the fly. For example, if the variable
   foo is defined as having the value "27", then the following
   element:
   <pre>
   <element attr="${foo}"/>
   </pre>
   would be expanded to:
   <element attr="27"/>

   In addition to providing a simple variable expansion mechanism the
   class has to cope with the fact that the variable definitions to be
   used depend upon where the variable reference is found. One
   definition can come into scope for a while, and then go out of
   scope. The scoping mechanism is handled by the {@link Framer}
   class. 

 */

// This class operates by receiving SAX events from its parent
// XMLReader
class VariableExpander extends XMLFilterImpl
{

    Framer framer = new Framer();
    
    private static final String START="${";
    private static final String END="}";

      // Characters can come in chunks, so we need to store them. When
      // characters come in we can only eval them *after* the
      // endElement event, which has already popped the frame off the
      // stack - so while collecting characters we remember the frame
      // present when the characters were sent, and then use that
      // frame to eval the characters.
    private StringBuffer characters = new StringBuffer();
    private Frame frame;

    VariableExpander(){
        super();
    }

    
    VariableExpander(Framer f){
        framer = f;
//         super.setParent(f);
    }

    void setFramer(Framer f){
//         if (framer != null && framer.getParent() != null) {
//             f.setParent(framer.getParent());
//         }
//         super.setParent(f);
        framer = f;
    }
    
//     public void setParent(XMLReader parent){
//         framer.setParent(parent);
//     }
    
//     public XMLReader getParent(){
//         return framer.getParent();
//     }
    
    public void characters(char [] ch, int start, int len) throws SAXException {
        characters.append(ch, start, len);
//         frame = framer.currentFrame();
    }

    public void endDocument() throws SAXException {
        processCharacters();
        framer.endDocument();
        super.endDocument();
    }
    
    public void endElement(String namespaceURI, String localName, String qName)  throws SAXException {
        processCharacters();
        framer.endElement(namespaceURI, localName, qName);
        super.endElement(namespaceURI, localName, qName);
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)  throws SAXException {
        processCharacters();
        framer.startElement(namespaceURI, localName, qName, atts);
        final Attributes newAtts = processAttributes(atts);
        super.startElement(namespaceURI, localName, qName, newAtts);
    }
    private void processCharacters() throws SAXException {
        if (characters.length() > 0){
            final String chars = expandVariables(characters.toString());
            super.characters(chars.toCharArray(), 0, chars.length());
            characters.setLength(0);
//             frame = null;
        }
    }
    

    private String expandVariables(final String chars){
        return expandVariables(chars, framer.currentFrame());
    }

    private String expandVariables(final String chars, final Frame f){
        return ((chars.indexOf(START) < chars.indexOf(END)) ? eval(chars, f) : chars);
    }
    
    private Attributes processAttributes(final Attributes atts){
        final AttributesImpl newAtts = new AttributesImpl(atts);
        for (int i = 0; i < newAtts.getLength(); i++){
            newAtts.setValue(i, expandVariables(newAtts.getValue(i)));
        }
        return newAtts;
    }
    

    String eval(final String chars, final Frame frame){
        debug("expanding \""+chars+"\"");
        StringBuffer sb = new StringBuffer();
        int vs, ve = 0;         // variable start, end
          // loop invariant - i is at start of next part of string to
          // be examined. sb contains all previous parts of string,
          // including the translated portions
        for (int  i = 0; i < chars.length(); ){
            if (((vs = chars.indexOf(START, i)) != -1) && ((ve = chars.indexOf(END, i)) != -1)) {
                sb.append(chars.substring(i, vs)); // copy prefix
                sb.append(frame.lookup(chars.substring(vs+2, ve)));
                i = ve + 1;
            } else {            // No variable to be expanded
                sb.append(chars.substring(i));
                i = chars.length();
            }
        }
        return sb.toString();
    }

    private final boolean DEBUG = false;
    
    private final void debug(final String s){
       if (DEBUG) System.err.println(s);
    }
    
        
}

    

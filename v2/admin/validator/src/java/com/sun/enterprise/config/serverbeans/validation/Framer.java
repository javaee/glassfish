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

import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;
/**
   This class provides the basic mechanism for constructing Frames in
   which variable definitions can be recorded, and then used to
   dereference variable references.

   The idea is that it manages a collection of frames, held in a
   frameholder. (The frameholder can either be provided by clients, or
   a default one is provided). As SAX events come in frames are added
   to this frameholder, in the appropriate place.

*/
// This class only knows that frames begin and end on certain start
// and end elements, and that system property elements contain a name
// value pair that should be added to the current frame.
public class Framer extends DefaultHandler
{
    
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)  throws SAXException {
        if (isSystemPropertyElement(localName)){
            handleSystemPropertyEvent(atts);
        } else if (isConfigEvent(localName)) {
            handleStartConfigEvent(atts);
        } else if (isServerEvent(localName)) {
            handleStartServerEvent(atts);
        } else if (isClusterEvent(localName)) {
            handleStartClusterEvent(atts);
        } else if (isServerRefEvent(localName)) {
            handleStartServerRefEvent(atts);
        }
        super.startElement(namespaceURI, localName, qName, atts);
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (isConfigEvent(localName) || isServerEvent(localName) || isClusterEvent(localName)){
            frameStack.pop();
        }
        super.endElement(namespaceURI, localName, qName);
    }


    private boolean isClusterEvent(String n){
        return n.equals(CLUSTER);
    }

    private boolean isConfigEvent(String n){
        return n.equals(CONFIG);
    }
    private boolean isServerEvent(String n){
        return n.equals(SERVER);
    }
    private boolean isServerRefEvent(String n){
        return n.equals(SERVER_REF);
    }
    private  boolean isSystemPropertyElement(String n){
        return n.equals(SYSTEM_PROPERTY);
    }

    private void handleStartClusterEvent(Attributes atts){
        frameStack.push(getClusterFrame(atts));
    }

    private void handleStartConfigEvent(Attributes atts){
        frameStack.push(getConfigFrame(atts));
    }

    private void handleStartServerEvent(Attributes atts){
        frameStack.push(getServerFrame(atts));
    }
    protected void handleStartServerRefEvent(Attributes atts){}

    private void handleSystemPropertyEvent(Attributes atts){
        currentFrame().put(atts.getValue(NAMESPACE, NAME), atts.getValue(NAMESPACE, VALUE));
    }

    protected Frame getClusterFrame(Attributes atts){
        return frameHolder.getClusterFrame(getFrameName(atts));
    }
    
    protected Frame getConfigFrame(Attributes atts){
        return frameHolder.getConfigFrame(getFrameName(atts));
    }
    
    protected Frame getServerFrame(Attributes atts){
        return frameHolder.getServerFrame(getFrameName(atts));
    }


    private String getFrameName(Attributes atts){
        return atts.getValue(NAMESPACE, NAME);
    }
    
    Framer(){
        this(new FrameHolder());
    }

    Framer(FrameHolder fh){
        frameHolder = fh;
        frameStack.push(fh.getDomainFrame());
    }
    

    FrameHolder getFrameHolder(){
        return frameHolder;
    }

    Frame currentFrame(){
        return (Frame) frameStack.peek();
    }


    private Stack frameStack = new Stack();
    protected FrameHolder frameHolder = new FrameHolder();

    public static final String CLUSTER = "cluster";
    public static final String CONFIG = "config";
    public static final String CONFIG_REF = "config-ref";
    public static final String NAME = "name";
    public static final String NAMESPACE = "";
    public static final String SERVER = "server";
    public static final String SERVER_REF = "server-ref";
    public static final String SYSTEM_PROPERTY = "system-property";
    public static final String VALUE = "value";

}

/*
 * $Id: MapEntryRule.java,v 1.1 2005/09/20 21:11:34 edburns Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.faces.config.rules;


import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;
import com.sun.faces.config.beans.MapEntriesBean;
import com.sun.faces.config.beans.MapEntryBean;


/**
 * <p>Digester rule for the <code>&lt;map-entry&gt;</code> element.</p>
 */

public class MapEntryRule extends Rule {


    private static final String CLASS_NAME =
        "com.sun.faces.config.beans.MapEntryBean";


    // ------------------------------------------------------------ Rule Methods


    /**
     * <p>Create an empty instance of <code>MapEntryBean</code>
     * and push it on to the object stack.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     * @param attributes The attribute list of this element
     *
     * @exception IllegalStateException if the parent stack element is not
     *  of type MapEntryHolder
     */
    public void begin(String namespace, String name,
                      Attributes attributes) throws Exception {

        assert (digester.peek() instanceof MapEntriesBean);
        
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[MapEntryRule]{" +
                                       digester.getMatch() +
                                       "} Push " + CLASS_NAME);
        }
        Class clazz =
            digester.getClassLoader().loadClass(CLASS_NAME);
        MapEntryBean meb = (MapEntryBean) clazz.newInstance();
        digester.push(meb);

    }


    /**
     * <p>No body processing is requlred.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     * @param text The text of the body of this element
     */
    public void body(String namespace, String name,
                     String text) throws Exception {
    }


    /**
     * <p>Pop the <code>MapEntryBean</code> off the top of the stack,
     * and add the new entry.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     *
     * @exception IllegalStateException if the popped object is not
     *  of the correct type
     */
    public void end(String namespace, String name) throws Exception {

        MapEntryBean top = null;
        try {
            top = (MapEntryBean) digester.pop();
        } catch (Exception e) {
            throw new IllegalStateException("Popped object is not a " +
                                            CLASS_NAME + " instance");
        }
        MapEntriesBean mesb = (MapEntriesBean) digester.peek();
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[MapEntryRule]{" +
                                       digester.getMatch() +
                                       "} Add");
        }
        mesb.addMapEntry(top);

    }


    /**
     * <p>No finish processing is required.</p>
     *
     */
    public void finish() throws Exception {
    }


    // ---------------------------------------------------------- Public Methods


    public String toString() {

        StringBuffer sb = new StringBuffer("MapEntryRule[className=");
        sb.append(CLASS_NAME);
        sb.append("]");
        return (sb.toString());

    }


    // --------------------------------------------------------- Package Methods


}

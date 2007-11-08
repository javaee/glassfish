/*
 * $Id: DisplayNameRule.java,v 1.1 2005/09/20 21:11:32 edburns Exp $
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
import com.sun.faces.config.beans.DisplayNameBean;
import com.sun.faces.config.beans.FeatureBean;


/**
 * <p>Digester rule for the <code>&lt;display-name&gt;</code> element.</p>
 */

public class DisplayNameRule extends FeatureRule {


    private static final String CLASS_NAME =
        "com.sun.faces.config.beans.DisplayNameBean";


    // ------------------------------------------------------------ Rule Methods


    /**
     * <p>Create or retrieve an instance of <code>DisplayNameBean</code>
     * and push it on to the object statck.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     * @param attributes The attribute list of this element
     *
     * @exception IllegalStateException if the parent stack element is not
     *  of type FeatureBean
     */
    public void begin(String namespace, String name,
                      Attributes attributes) throws Exception {

        FeatureBean fb = null;
        try {
            fb = (FeatureBean) digester.peek();
        } catch (Exception e) {
            throw new IllegalStateException
                ("No parent FeatureBean on object stack");
        }
        String lang = attributes.getValue("lang");
        if (lang == null) {
            lang = attributes.getValue("xml:lang"); // If digester not ns-aware
        }
        if (lang == null) {
            lang = ""; // Avoid NPE errors on sorted map comparisons
        }
        DisplayNameBean dnb = fb.getDisplayName(lang);
        if (dnb == null) {
            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[DisplayNameRule]{" +
                                           digester.getMatch() +
                                           "} New (" + lang + ")");
            }
            Class clazz =
                digester.getClassLoader().loadClass(CLASS_NAME);
            dnb = (DisplayNameBean) clazz.newInstance();
            dnb.setLang(lang);
            fb.addDisplayName(dnb);
        } else {
            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[DisplayNameRule]{" +
                                           digester.getMatch() +
                                           "} Old (" + lang + ")");
            }
        }
        digester.push(dnb);

    }


    /**
     * <p>Save the body text of this element.</p>
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

        if (text == null) {
            return;
        }
        DisplayNameBean dnb = (DisplayNameBean) digester.peek();
        dnb.setDisplayName(text.trim());

    }


    /**
     * <p>Pop the <code>DisplayNameBean</code> off the top of the stack.</p>
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

        DisplayNameBean top = null;
        try {
            top = (DisplayNameBean) digester.pop();
        } catch (Exception e) {
            throw new IllegalStateException("Popped object is not a " +
                                            CLASS_NAME + " instance");
        }
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[DisplayNameRule]{" +
                                       digester.getMatch() +
                                       "} Pop (" + top.getLang() + ")");
        }


    }


    /**
     * <p>No finish processing is required.</p>
     *
     */
    public void finish() throws Exception {
    }


    // ---------------------------------------------------------- Public Methods


    public String toString() {

        StringBuffer sb = new StringBuffer("DisplayNameRule[className=");
        sb.append(CLASS_NAME);
        sb.append("]");
        return (sb.toString());

    }


    // --------------------------------------------------------- Package Methods


}

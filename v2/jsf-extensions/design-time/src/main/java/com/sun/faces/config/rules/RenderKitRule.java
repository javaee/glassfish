/*
 * $Id: RenderKitRule.java,v 1.1 2005/09/20 21:11:35 edburns Exp $
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
import com.sun.faces.config.beans.FacesConfigBean;
import com.sun.faces.config.beans.RenderKitBean;


/**
 * <p>Digester rule for the <code>&lt;renderKit&gt;</code> element.</p>
 */

public class RenderKitRule extends FeatureRule {


    private static final String CLASS_NAME =
        "com.sun.faces.config.beans.RenderKitBean";


    // ------------------------------------------------------------ Rule Methods


    /**
     * <p>Create an empty instance of <code>RenderKitBean</code>
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
     *  of type FacesConfigBean
     */
    public void begin(String namespace, String name,
                      Attributes attributes) throws Exception {

        assert (digester.peek() instanceof FacesConfigBean);
        
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[RenderKitRule]{" +
                                       digester.getMatch() +
                                       "} Push " + CLASS_NAME);
        }
        Class clazz =
            digester.getClassLoader().loadClass(CLASS_NAME);
        RenderKitBean rkb = (RenderKitBean) clazz.newInstance();
        digester.push(rkb);

    }


    /**
     * <p>No body processing is required.</p>
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
     * <p>Pop the <code>RenderKitBean</code> off the top of the stack,
     * and either add or merge it with previous information.</p>
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

        RenderKitBean top = null;
        try {
            top = (RenderKitBean) digester.pop();
        } catch (Exception e) {
            throw new IllegalStateException("Popped object is not a " +
                                            CLASS_NAME + " instance");
        }
        FacesConfigBean fcb = (FacesConfigBean) digester.peek();
        RenderKitBean old = fcb.getRenderKit(top.getRenderKitId());
        if (old == null) {
            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[RenderKitRule]{" +
                                           digester.getMatch() +
                                           "} New(" +
                                           top.getRenderKitId() +
                                           ")");
            }
            fcb.addRenderKit(top);
        } else {
            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[RenderKitRule]{" +
                                           digester.getMatch() +
                                           "} Merge(" +
                                           top.getRenderKitId() +
                                           ")");
            }
            mergeRenderKit(top, old);
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

        StringBuffer sb = new StringBuffer("RenderKitRule[className=");
        sb.append(CLASS_NAME);
        sb.append("]");
        return (sb.toString());

    }


    // --------------------------------------------------------- Package Methods


    // Merge "top" into "old"
    static void mergeRenderKit(RenderKitBean top, RenderKitBean old) {

        // Merge singleton properties
        if (top.getRenderKitClass() != null) {
            old.setRenderKitClass(top.getRenderKitClass());
        }

        // Merge common collections
        mergeFeatures(top, old);

        // Merge unique collections
        RendererRule.mergeRenderers(top, old);

    }


}
